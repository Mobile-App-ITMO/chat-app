package io.ktor.chat.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.chat.Message
import io.ktor.chat.emoml.EmotionOutput
import io.ktor.chat.utils.shortened

@Composable
fun MessageListItem(
    message: Message,
    emotion: EmotionOutput? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.author.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = message.created.shortened(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }


                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )


                when {
                    emotion == null || emotion.label.startsWith("EMO: pending", ignoreCase = true) -> {
                        Text(
                            text = "EMO: pending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    emotion.label.startsWith("EMO:", ignoreCase = true) -> {
                        Text(
                            text = "EMO ERROR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }

                    else -> {
                        val emotionColor = when (emotion.label) {
                            "Сарказм" -> Color(0xFFB26A00)
                            "Раздражение", "Злость" -> Color(0xFFC62828)
                            "Усталость" -> Color(0xFF455A64)
                            "Грусть" -> Color(0xFF1565C0)
                            "Радость" -> Color(0xFF2E7D32)
                            "Тревога" -> Color(0xFF6A1B9A)
                            "Отвращение" -> Color(0xFF2F3B2F)
                            "Удивление" -> Color(0xFF00838F)
                            "Монотонность", "Нейтрально" -> Color(0xFF616161)
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Text(
                            text = emotion.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = emotionColor
                        )
                    }
                }
            }
        }
    }
}
