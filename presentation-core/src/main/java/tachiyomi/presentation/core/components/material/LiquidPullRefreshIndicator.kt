package tachiyomi.presentation.core.components.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import org.intellij.lang.annotations.Language

/**
 * A custom high-end "Liquid" Pull-to-Refresh indicator with Shader support.
 * Uses AGSL Metaballs on Android 13+ for an organic, stretching blob appearance.
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
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
        } else {
            pulseScale.snapTo(1f)
        }
    }

    val blobHeight by animateFloatAsState(
        targetValue = if (isRefreshing) 48f else (progress * 90f).coerceAtMost(140f),
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow,
        ),
        label = "blobHeight",
    )

    val shader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        remember { RuntimeShader(METABALL_SHADER) }
    } else {
        null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2
            val baseWidth = 42.dp.toPx()

            if (progress > 0f || isRefreshing) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shader != null) {
                    drawLiquidShader(
                        shader = shader,
                        centerX = centerX,
                        progress = progress,
                        isRefreshing = isRefreshing,
                        blobHeight = blobHeight,
                        baseWidth = baseWidth,
                        color = color,
                        pulseScale = pulseScale.value,
                    )
                } else {
                    drawLegacyLiquid(
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
}

@Language("AGSL")
private const val METABALL_SHADER = """
    uniform float2 iResolution;
    uniform float2 pTop;
    uniform float2 pBottom;
    uniform float2 pMid;
    uniform float rTop;
    uniform float rBottom;
    uniform float rMid;
    uniform float4 iColor;
    uniform float iThreshold;

    float metaball(float2 p, float2 center, float radius) {
        float d = distance(p, center);
        if (d < radius) return 1.1;
        return pow(radius / d, 3.0);
    }

    half4 main(float2 fragCoord) {
        float m = metaball(fragCoord, pTop, rTop) +
                  metaball(fragCoord, pBottom, rBottom) +
                  metaball(fragCoord, pMid, rMid);

        float alpha = smoothstep(iThreshold - 0.05, iThreshold + 0.05, m);
        return iColor * alpha;
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun DrawScope.drawLiquidShader(
    shader: RuntimeShader,
    centerX: Float,
    progress: Float,
    isRefreshing: Boolean,
    blobHeight: Float,
    baseWidth: Float,
    color: Color,
    pulseScale: Float,
) {
    val actualBlobHeight = if (isRefreshing) blobHeight * pulseScale else blobHeight
    val topY = 0f
    val bottomY = actualBlobHeight
    val midY = actualBlobHeight / 2f

    val rTop = baseWidth / 2f
    val rBottom = if (isRefreshing) (baseWidth / 2f) * pulseScale else {
        (baseWidth / 2.5f) * progress.coerceIn(0f, 1.4f)
    }
    val rMid = (baseWidth / 3.5f) * (1.2f - (progress * 0.2f).coerceIn(0f, 0.6f))

    shader.setFloatUniform("iResolution", size.width, size.height)
    shader.setFloatUniform("pTop", centerX, topY)
    shader.setFloatUniform("pBottom", centerX, bottomY)
    shader.setFloatUniform("pMid", centerX, midY)
    shader.setFloatUniform("rTop", rTop)
    shader.setFloatUniform("rBottom", rBottom)
    shader.setFloatUniform("rMid", rMid)
    shader.setFloatUniform("iColor", color.red, color.green, color.blue, color.alpha)
    shader.setFloatUniform("iThreshold", 1.0f)

    drawRect(
        brush = ShaderBrush(shader),
        topLeft = Offset(centerX - baseWidth * 2, -rTop), // Start slightly above to cover the top circle
        size = Size(baseWidth * 4, actualBlobHeight + rBottom * 2 + rTop),
    )
}

private fun DrawScope.drawLegacyLiquid(
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
        baseWidth * (1f - (progress * 0.15f).coerceAtMost(0.3f))
    }

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
