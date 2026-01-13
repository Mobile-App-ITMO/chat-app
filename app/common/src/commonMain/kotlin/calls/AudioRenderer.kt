package io.ktor.chat.calls

import androidx.compose.runtime.Composable
import io.ktor.client.webrtc.WebRtcMedia

@Composable
expect fun AudioRenderer(
    audioTrack: WebRtcMedia.AudioTrack
)