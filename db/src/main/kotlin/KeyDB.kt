package io.ktor.chat

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient

object KeyDB {
    private val client = newClient(Endpoint("localhost", 6379))

    suspend fun saveMessage(message: Message) {
        val key = "room:${message.room}:messages"
        val value = "${message.id}|${message.room}|${message.author.id}|${message.text}"

        client.lpush(key, value)
        client.ltrim(key, 0, 100)
    }
}