package io.ktor.chat.emoml

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.request.*

class SentimentService(
    private val baseUrl: String,
    private val model: String = "mistral:7b-instruct"
) {

    private val http: HttpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
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
        val prompt = buildPrompt(text)

        val response = http.post("/api/generate") {
            setBody(
                OllamaGenerateRequest(
                    model = model,
                    prompt = prompt,
                    stream = false
                )
            )
            accept(ContentType.Application.Json)
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            throw IllegalStateException("Ollama error ${response.status.value}: $body")
        }

        val contentType = response.headers[HttpHeaders.ContentType].orEmpty()
        val bodyText = response.bodyAsText()

        val modelText = when {
            contentType.contains("ndjson", ignoreCase = true) || bodyText.contains("\n") -> {
                extractOllamaResponseFromNdjson(bodyText)
            }
            else -> {
                val parsed = Json { ignoreUnknownKeys = true }
                    .decodeFromString(OllamaGenerateResponse.serializer(), bodyText)
                parsed.response
            }
        }.trim()

        return parseModelOutput(modelText)
    }

    private fun extractOllamaResponseFromNdjson(ndjson: String): String {
        val json = Json { ignoreUnknownKeys = true }

        var acc = StringBuilder()
        ndjson.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { line ->
                val chunk = runCatching {
                    json.decodeFromString(OllamaGenerateResponse.serializer(), line)
                }.getOrNull() ?: return@forEach

                if (chunk.response.isNotEmpty()) acc.append(chunk.response)
                if (chunk.done) return acc.toString()
            }

        return acc.toString()
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
        val json = extractFirstJsonObject(raw) ?: raw
        return try {
            Json { ignoreUnknownKeys = true }
                .decodeFromString(EmotionOutput.serializer(), json)
        } catch (e: Throwable) {
            EmotionOutput(
                label = "Нейтрально",
                confidence = 0.2f,
                tone = EmotionOutput.Tone.NEUTRAL
            )
        }
    }

    private fun extractFirstJsonObject(s: String): String? {
        val start = s.indexOf('{')
        if (start == -1) return null

        var depth = 0
        for (i in start until s.length) {
            when (s[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return s.substring(start, i + 1)
                    }
                }
            }
        }
        return null
    }
}
