package io.ktor.chat.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.ui.theme.Radius
import io.ktor.chat.ui.theme.Space

@Composable
fun BlackButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(Radius.xs),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = Space.lg)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
    }
}