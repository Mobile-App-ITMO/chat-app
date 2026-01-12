package io.ktor.chat.messages

import androidx.compose.foundation.layout.*
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
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = message.created.shortened(),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }


                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )


                when {
                    emotion == null -> {
                        Text(
                            text = "EMO: pending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    emotion.label.startsWith("EMO", ignoreCase = true) -> {
                        // error/debug state
                        Text(
                            text = "EMO ERROR · ${emotion.tone.name} · ${(emotion.confidence * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Red
                        )
                    }

                    else -> {
                        val emotionColor = when (emotion.label) {
                            "Сарказм" -> Color(0xFFB26A00)
                            "Раздражение", "Злость" -> Color(0xFFC62828)
                            "Усталость" -> Color(0xFF546E7A)
                            "Грусть" -> Color(0xFF1565C0)
                            "Радость" -> Color(0xFF2E7D32)
                            "Тревога" -> Color(0xFF6A1B9A)
                            "Монотонность", "Нейтрально" -> Color(0xFF757575)
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
