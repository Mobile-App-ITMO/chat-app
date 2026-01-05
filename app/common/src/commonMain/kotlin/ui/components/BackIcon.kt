package io.ktor.chat.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.ui.theme.Radius
import io.ktor.chat.ui.theme.Space

@Composable
fun BackIcon(onBack: () -> Unit) {
    FilledIconButton(
        onClick = onBack,
        shape = RoundedCornerShape(Radius.xs),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        modifier = Modifier
            .padding(start = 20.dp, top = 45.dp)
            .offset(x = 0.dp)
            .border(
                width = 1.dp,
                color =  MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(Radius.xs))
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "Back",
            modifier = Modifier
                .offset(x = 5.dp)
        )
    }
}