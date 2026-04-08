package tachiyomi.presentation.core.util

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.intellij.lang.annotations.Language

@Language("AGSL")
private const val MELT_SHADER = """
    uniform shader content;
    uniform float progress;
    uniform float2 resolution;

    float hash(float2 p) {
        return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;

        float drift = hash(float2(uv.x * 10.0, 0.0)) * (1.0 - progress) * 0.1;
        float2 displacedUv = uv;
        displacedUv.y -= drift;

        float4 color = content.eval(displacedUv * resolution);

        return color * smoothstep(0.0, 0.1, progress);
    }
"""

/**
 * Applies a "Liquid Melting" entrance effect to a component.
 */
fun Modifier.liquidEntrance(
    visible: Boolean,
    durationMillis: Int = 800,
): Modifier = composed {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis),
        label = "liquidEntrance",
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && progress < 1f) {
        val shader = remember { RuntimeShader(MELT_SHADER) }
        this.graphicsLayer {
            shader.setFloatUniform("progress", progress)
            shader.setFloatUniform("resolution", size.width, size.height)
            renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
        }
    } else if (progress < 1f) {
        this.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 16.dp.toPx()
        }
    } else {
        this
    }
}
