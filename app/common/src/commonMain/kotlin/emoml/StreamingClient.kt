package io.ktor.chat.emoml

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.accept
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class StreamingClient(
    private val baseUrl: String,
    private val model: String = "mistral:7b-instruct"
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val http = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(this@StreamingClient.json) }
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
        val stream: Boolean = true
    )

    @Serializable
    private data class OllamaStreamChunk(
        val response: String = "",
        val done: Boolean = false,
        @SerialName("done_reason") val doneReason: String? = null
    )

    fun stream(prompt: String): Flow<String> = flow {
        val resp: HttpResponse = http.post("/api/generate") {
            setBody(OllamaGenerateRequest(model = model, prompt = prompt, stream = true))
        }

        if (!resp.status.isSuccess()) {
            val body = runCatching { resp.bodyAsText() }.getOrDefault("")
            throw RuntimeException("Ollama stream error ${resp.status.value}: $body")
        }

        val channel = resp.bodyAsChannel()
        val buf = StringBuilder()
        val tmp = ByteArray(4096)

        while (!channel.isClosedForRead) {
            val read = channel.readAvailable(tmp, 0, tmp.size)
            if (read <= 0) continue
            buf.append(tmp.decodeToString(0, read))

            while (true) {
                val nl = buf.indexOf("\n")
                if (nl == -1) break
                val line = buf.substring(0, nl).trim()
                buf.deleteRange(0, nl + 1)

                if (line.isBlank()) continue
                val chunk = runCatching { json.decodeFromString(OllamaStreamChunk.serializer(), line) }.getOrNull() ?: continue
                if (chunk.response.isNotEmpty()) emit(chunk.response)
                if (chunk.done) return@flow
            }
        }
    }
}
