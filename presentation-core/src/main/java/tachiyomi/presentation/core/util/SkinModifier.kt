package tachiyomi.presentation.core.util

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import tachiyomi.presentation.core.theme.DefaultSkin
import tachiyomi.presentation.core.theme.Skin
import tachiyomi.presentation.core.theme.SkinColors

val LocalSkin = staticCompositionLocalOf<Skin> { DefaultSkin }
val LocalSkinColors = staticCompositionLocalOf<SkinColors?> { null }

@Composable
@ReadOnlyComposable
fun currentSkin(): Skin = LocalSkin.current

/**
 * Applies the current [LocalSkin] to the modifier chain.
 * Uses AGSL shaders to skin the component with high-performance materials.
 */
fun Modifier.skin(
    enabled: Boolean = true,
): Modifier = composed {
    if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return@composed Modifier
    }

    val skin = LocalSkin.current
    val colors = LocalSkinColors.current ?: SkinColors(
        main = MaterialTheme.colorScheme.primary,
        accent = MaterialTheme.colorScheme.secondary,
        background = MaterialTheme.colorScheme.surface,
        isDark = !MaterialTheme.colorScheme.surface.isLight(),
    )

    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                time = it / 1000f
            }
        }
    }

    val runtimeShader = remember(skin) {
        RuntimeShader(skin.shaderCode)
    }

    this.then(
        Modifier.graphicsLayer {
            skin.updateUniforms(runtimeShader, time, colors)
            renderEffect = RenderEffect.createRuntimeShaderEffect(
                runtimeShader,
                "content",
            ).asComposeRenderEffect()
        },
    )
}

/**
 * Provides a [Skin] to the local composition.
 */
@Composable
fun ProvideSkin(
    skin: Skin,
    colors: SkinColors? = null,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSkin provides skin,
        LocalSkinColors provides colors,
        content = content,
    )
}

private fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}
