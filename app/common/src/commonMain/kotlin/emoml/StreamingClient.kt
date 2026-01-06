package io.ktor.chat.emoml

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.utils.io.*


class StreamingClient(
    private val http: HttpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(DefaultRequest) {
            url("http://localhost:11434")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    },
    private val model: String = "mistral:7b-instruct",
) {

    @Serializable
    private data class OllamaGenerateRequest(
        val model: String,
        val prompt: String,
        val stream: Boolean = true,
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
                val chunk = runCatching {
                    Json { ignoreUnknownKeys = true }.decodeFromString(OllamaStreamChunk.serializer(), line)
                }.getOrNull() ?: continue

                if (chunk.response.isNotEmpty()) emit(chunk.response)
                if (chunk.done) return@flow
            }
        }
    }
}
