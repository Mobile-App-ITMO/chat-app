package io.ktor.chat.emoml

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.accept
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.date.getTimeMillis
import kotlinx.io.EOFException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

    suspend fun analyze(text: String): EmotionOutput {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return monotone()
        if (looksLikeGibberish(trimmed)) return monotone()

        val now = getTimeMillis()
        if (now < blockedUntilMs) return pending()

        val prompt = buildPrompt(trimmed)

        val response = try {
            http.post("/api/generate") {
                setBody(OllamaGenerateRequest(model = model, prompt = prompt, stream = false))
            }
        } catch (e: EOFException) {
            blockShort()
            return pending()
        } catch (e: Throwable) {
            blockShort()
            return pending()
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
            return error()
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

    private fun parseModelOutput(raw: String): EmotionOutput {
        val candidate = extractFirstJsonObject(raw) ?: raw
        val parsed = runCatching { json.decodeFromString(EmotionOutput.serializer(), candidate) }.getOrNull()
        if (parsed != null && parsed.label.isNotBlank()) return normalize(parsed)
        return error()
    }

    private fun normalize(out: EmotionOutput): EmotionOutput {
        val label = out.label.trim()
        val tone = out.tone
        val conf = out.confidence.coerceIn(0f, 1f)

        val normalizedLabel = when (label.lowercase()) {
            "neutral", "норм", "норма", "нейтрал", "нейтрально" -> "Нейтрально"
            "tired", "устал", "усталость" -> "Усталость"
            else -> label.replaceFirstChar { it.uppercaseChar() }
        }

        return EmotionOutput(label = normalizedLabel, confidence = conf, tone = tone)
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
        if (t.length <= 2) return true

        var letters = 0
        var digits = 0
        var other = 0

        for (ch in t) {
            when {
                ch.isLetter() -> letters++
                ch.isDigit() -> digits++
                ch.isWhitespace() -> Unit
                ch in ".,!?;:()[]{}'\"—-_/\\@#%&*+=<>`~|" -> Unit
                else -> other++
            }
        }

        val signal = letters + digits + other
        if (signal == 0) return true

        val letterRatio = letters.toFloat() / signal.toFloat()

        if (letters >= 4) return false

        val junk = digits + other
        return letterRatio < 0.25f && junk >= letters + 2
    }

    private fun pending(): EmotionOutput =
        EmotionOutput(label = "EMO: pending", confidence = 0f, tone = EmotionOutput.Tone.NEUTRAL)

    private fun error(): EmotionOutput =
        EmotionOutput(label = "EMO: error", confidence = 0f, tone = EmotionOutput.Tone.NEUTRAL)

    private fun monotone(): EmotionOutput =
        EmotionOutput(label = "Монотонность", confidence = 1f, tone = EmotionOutput.Tone.NEUTRAL)

    private fun blockShort() {
        blockedUntilMs = getTimeMillis() + 10_000
    }

    private fun blockLong() {
        blockedUntilMs = getTimeMillis() + 60_000
    }

    private fun buildPrompt(text: String): String =
        """
        Ты классификатор эмоции/тона сообщения в чате.
        Верни строго один JSON-объект без Markdown и без текста вокруг.

        Допустимые label (строго на русском):
        Сарказм, Раздражение, Злость, Усталость, Грусть, Тревога, Радость, Отвращение, Удивление, Нейтрально, Монотонность

        Допустимые tone:
        positive, neutral, negative, anger, sadness, joy, fear, disgust, surprise

        Правила:
        - Если текст состоит в основном из цифр/шума/случайных наборов символов или бессмысленный: label="Монотонность", tone="neutral".
        - Если есть сарказм/ирония (например "ну конечно", "ага, конечно", "идеально сработало" в негативном контексте): label="Сарказм", tone="negative".

        Формат:
        {"label":"...","confidence":0.0-1.0,"tone":"..."}

        Сообщение:
        ${text.trim()}
        """.trimIndent()
}
