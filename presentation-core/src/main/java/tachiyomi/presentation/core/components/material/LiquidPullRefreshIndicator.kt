package tachiyomi.presentation.core.components.material

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * A custom high-end "Liquid" Pull-to-Refresh indicator.
 * It stretches like a blob as you pull and pulses when refreshing.
 */
@Composable
fun LiquidPullRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val progress = state.distanceFraction.coerceIn(0f, 2f)

    // Pulse animation for the "Refreshing" state
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pulseScale.animateTo(
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
        } else {
            pulseScale.snapTo(1f)
        }
    }

    val blobHeight by animateFloatAsState(
        targetValue = if (isRefreshing) 48f else (progress * 80f).coerceAtMost(120f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "blobHeight",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2
            val baseWidth = 48.dp.toPx()

            if (progress > 0f || isRefreshing) {
                drawLiquidBlob(
                    centerX = centerX,
                    progress = progress,
                    isRefreshing = isRefreshing,
                    blobHeight = blobHeight,
                    baseWidth = baseWidth,
                    color = color,
                    pulseScale = pulseScale.value,
                )
            }
        }
    }
}

private fun DrawScope.drawLiquidBlob(
    centerX: Float,
    progress: Float,
    isRefreshing: Boolean,
    blobHeight: Float,
    baseWidth: Float,
    color: Color,
    pulseScale: Float,
) {
    val actualBlobHeight = if (isRefreshing) blobHeight * pulseScale else blobHeight
    val actualBlobWidth = if (isRefreshing) baseWidth * pulseScale else {
        // Narrower as it stretches
        baseWidth * (1f - (progress * 0.2f).coerceAtMost(0.4f))
    }

    // Draw the "Liquid" stretching from the top
    drawRoundRect(
        color = color,
        topLeft = Offset(centerX - actualBlobWidth / 2, 0f),
        size = Size(actualBlobWidth, actualBlobHeight),
        cornerRadius = CornerRadius(actualBlobWidth / 2, actualBlobWidth / 2),
    )

    if (progress >= 1f && !isRefreshing) {
        val circleAlpha = (progress - 1f).coerceIn(0f, 1f)
        drawCircle(
            color = color.copy(alpha = circleAlpha),
            center = Offset(centerX, actualBlobHeight),
            radius = (actualBlobWidth / 2) * progress,
        )
    }
}
