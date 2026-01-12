package io.ktor.chat.emoml

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock.*
import kotlin.time.ExperimentalTime
import kotlinx.io.*
import io.ktor.util.date.getTimeMillis

class SentimentService(
    private val baseUrl: String,
    private val model: String = "mistral:7b-instruct"
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var blockedUntilMs: Long = 0L

    private val http: HttpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(this@SentimentService.json) }
        install(DefaultRequest) {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }

    @Serializable
    private data class OllamaGenerateRequest(
        val model: String,
        val prompt: String,
        val stream: Boolean = false
    )

    @Serializable
    private data class OllamaGenerateResponse(
        val response: String = "",
        val done: Boolean = true,
        @SerialName("done_reason") val doneReason: String? = null
    )

    @OptIn(ExperimentalTime::class)
    suspend fun analyze(text: String): EmotionOutput {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return monotone()
        if (looksLikeGibberish(trimmed)) return monotone()

        val now = getTimeMillis()
        if (now < blockedUntilMs) return EmotionOutput("EMO: pending", 0f, EmotionOutput.Tone.NEUTRAL)

        val prompt = buildPrompt(trimmed)

        val response = try {
            http.post("/api/generate") {
                setBody(OllamaGenerateRequest(model = model, prompt = prompt, stream = false))
            }
        } catch (e: EOFException) {
            blockShort()
            return EmotionOutput("EMO: pending", 0f, EmotionOutput.Tone.NEUTRAL)
        } catch (e: Throwable) {
            blockShort()
            return EmotionOutput("EMO: pending", 0f, EmotionOutput.Tone.NEUTRAL)
        }

        val bodyText = runCatching { response.bodyAsText() }.getOrDefault("")

        if (!response.status.isSuccess()) {
            val lower = bodyText.lowercase()
            if (lower.contains("requires more system memory") ||
                lower.contains("out of memory") ||
                lower.contains("cannot allocate memory")
            ) {
                blockLong()
                return monotone()
            }
            blockShort()
            throw IllegalStateException("Ollama error ${response.status.value}: $bodyText")
        }

        val contentType = response.headers[HttpHeaders.ContentType].orEmpty()
        val modelText = if (contentType.contains("ndjson", ignoreCase = true) || bodyText.indexOf('\n') >= 0) {
            extractOllamaResponseFromNdjson(bodyText)
        } else {
            runCatching { json.decodeFromString(OllamaGenerateResponse.serializer(), bodyText).response }
                .getOrDefault(bodyText)
        }.trim()

        return parseModelOutput(modelText)
    }

    private fun extractOllamaResponseFromNdjson(ndjson: String): String {
        val sb = StringBuilder()
        for (line in ndjson.lineSequence()) {
            val t = line.trim()
            if (t.isEmpty()) continue
            val chunk = runCatching { json.decodeFromString(OllamaGenerateResponse.serializer(), t) }.getOrNull() ?: continue
            if (chunk.response.isNotEmpty()) sb.append(chunk.response)
            if (chunk.done) break
        }
        return sb.toString()
    }

    private fun buildPrompt(text: String): String =
        """
        Ты строгий классификатор эмоции/тона сообщения в чате.

        ВАЖНО:
        1) Верни СТРОГО один JSON-объект. Никакого текста, никаких пояснений, никакого Markdown.
        2) "tone" должен быть ТОЛЬКО одним из значений:
           positive, neutral, negative, anger, sadness, joy, fear, disgust, surprise
        3) "label" должен быть ТОЛЬКО на русском и ТОЛЬКО из этого списка:
           Сарказм, Раздражение, Злость, Усталость, Грусть, Тревога, Радость, Отвращение, Удивление, Нейтрально, Монотонность
        4) Если сообщение в основном состоит из цифр/случайных символов/непонятных наборов букв
           или не несёт ясного смысла (например: "333", "asdasd", "5465gtdf") — выбери label="Монотонность" и tone="neutral".
        5) Если в сообщении есть ирония/сарказм (например "Ну конечно", "ага, конечно", "идеально сработало" в негативном контексте) —
           label="Сарказм" и tone="negative". НЕ выбирай neutral.

        Формат ответа:
        {"label":"...","confidence":0.0-1.0,"tone":"..."}

        Сообщение:
        ${text.trim()}
        """.trimIndent()

    private fun parseModelOutput(raw: String): EmotionOutput {
        val candidate = extractFirstJsonObject(raw) ?: raw
        val parsed = runCatching { json.decodeFromString(EmotionOutput.serializer(), candidate) }.getOrNull()
        if (parsed != null && parsed.label.isNotBlank()) return parsed
        return monotone()
    }

    private fun extractFirstJsonObject(s: String): String? {
        val start = s.indexOf('{')
        if (start < 0) return null
        var depth = 0
        for (i in start until s.length) {
            when (s[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return s.substring(start, i + 1)
                }
            }
        }
        return null
    }

    private fun looksLikeGibberish(s: String): Boolean {
        val t = s.trim()
        if (t.length < 3) return true
        var letters = 0
        var digits = 0
        var spaces = 0
        for (ch in t) {
            when {
                ch.isLetter() -> letters++
                ch.isDigit() -> digits++
                ch.isWhitespace() -> spaces++
            }
        }
        val other = t.length - letters - digits - spaces
        val letterRatio = letters.toFloat() / t.length.toFloat()
        return letterRatio < 0.35f && (digits + other) > letters
    }

    private fun monotone(): EmotionOutput =
        EmotionOutput(label = "Монотонность", confidence = 1f, tone = EmotionOutput.Tone.NEUTRAL)

    private fun blockShort() {
        blockedUntilMs = getTimeMillis() + 10_000
    }

    private fun blockLong() {
        blockedUntilMs = getTimeMillis() + 60_000
    }
}
