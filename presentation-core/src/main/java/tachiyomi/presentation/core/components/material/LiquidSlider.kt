package tachiyomi.presentation.core.components.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.theme.LIQUID_PROGRESS_SHADER
import tachiyomi.presentation.core.theme.SkinColors
import tachiyomi.presentation.core.util.LocalSkinColors

/**
 * A specialized Slider that uses a shader-animated liquid track.
 * Integrates with Hikari's skin system and falls back to a standard track on older Android versions.
 */
@Composable
fun LiquidSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: IntProgression = 0..1,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        track = { sliderState ->
            LiquidTrack(
                sliderState = sliderState,
                colors = colors,
                enabled = enabled,
            )
        },
    )
}

@Composable
private fun LiquidTrack(
    sliderState: SliderState,
    colors: SliderColors,
    enabled: Boolean,
) {
    // AGSL shaders require Android 13+
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        SliderDefaults.Track(sliderState = sliderState, colors = colors, enabled = enabled)
        return
    }

    val skinColors = LocalSkinColors.current ?: SkinColors(
        main = MaterialTheme.colorScheme.primary,
        accent = MaterialTheme.colorScheme.secondary,
        background = MaterialTheme.colorScheme.surface,
        isDark = true,
    )

    val infiniteTransition = rememberInfiniteTransition(label = "liquidTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "time",
    )

    val shader = remember { RuntimeShader(LIQUID_PROGRESS_SHADER) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
    ) {
        val rawProgress = (sliderState.value - sliderState.valueRange.start) /
            (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
        val progress = rawProgress.coerceIn(0f, 1f)

        shader.setFloatUniform("progress", progress)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform(
            "mainColor",
            skinColors.main.red,
            skinColors.main.green,
            skinColors.main.blue,
            skinColors.main.alpha,
        )
        shader.setFloatUniform(
            "accentColor",
            skinColors.accent.red,
            skinColors.accent.green,
            skinColors.accent.blue,
            skinColors.accent.alpha,
        )
        shader.setFloatUniform("resolution", size.width, size.height)

        drawRect(brush = ShaderBrush(shader))
    }
}
