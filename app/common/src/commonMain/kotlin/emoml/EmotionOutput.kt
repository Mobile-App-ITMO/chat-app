package io.ktor.chat.emoml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmotionOutput(
    val label: String,
    val confidence: Float,
    val tone: Tone = Tone.NEUTRAL
) {
    @Serializable
    enum class Tone {
        @SerialName("positive") POSITIVE,
        @SerialName("neutral") NEUTRAL,
        @SerialName("negative") NEGATIVE,
        @SerialName("anger") ANGER,
        @SerialName("sadness") SADNESS,
        @SerialName("joy") JOY,
        @SerialName("fear") FEAR,
        @SerialName("disgust") DISGUST,
        @SerialName("surprise") SURPRISE
    }
}
