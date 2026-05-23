package tachiyomi.presentation.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.material.TextButton

@Composable
fun HikariSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val visuals = snackbarData.visuals
    val actionLabel = visuals.actionLabel

    val tone = remember(visuals.message) {
        when {
            visuals.message.contains("error", ignoreCase = true) ||
                visuals.message.contains("fail", ignoreCase = true) -> SnackbarTone.Error

            visuals.message.contains("success", ignoreCase = true) ||
                visuals.message.contains("complete", ignoreCase = true) -> SnackbarTone.Success

            else -> SnackbarTone.Info
        }
    }
    val colors = tone.colors()

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .widthIn(max = 560.dp),
        shape = RoundedCornerShape(24.dp),
        color = HikariCardDefaults.containerColor(4.dp).copy(alpha = 0.98f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, HikariCardDefaults.dividerColor()),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 10.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colors.container, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tone.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.content,
                )
            }

            Text(
                text = visuals.message,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = if (actionLabel != null) 4.dp else 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (actionLabel != null) {
                TextButton(
                    onClick = { snackbarData.performAction() },
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.content,
                    )
                }
            }

            if (visuals.withDismissAction) {
                IconButton(
                    onClick = { snackbarData.dismiss() },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private enum class SnackbarTone(
    val icon: ImageVector,
) {
    Info(Icons.Outlined.Info),
    Success(Icons.Outlined.CheckCircle),
    Error(Icons.Outlined.ErrorOutline),
}

@Composable
private fun SnackbarTone.colors(): SnackbarToneColors {
    return when (this) {
        SnackbarTone.Info -> SnackbarToneColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        SnackbarTone.Success -> SnackbarToneColors(
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        SnackbarTone.Error -> SnackbarToneColors(
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

private data class SnackbarToneColors(
    val container: Color,
    val content: Color,
)
