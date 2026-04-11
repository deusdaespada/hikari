package eu.kanade.tachiyomi.ui.manga.util

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import tachiyomi.presentation.core.theme.SkinColors

/**
 * Extracts a [SkinColors] profile from a [Bitmap].
 */
object MangaColorExtractor {

    fun extract(bitmap: Bitmap, isDark: Boolean): SkinColors {
        val palette = Palette.from(bitmap).generate()

        val primary = palette.getVibrantColor(0)
            .takeIf { it != 0 }
            ?: palette.getDominantColor(0)
                .takeIf { it != 0 }
            ?: 0

        val accent = palette.getLightVibrantColor(0)
            .takeIf { it != 0 }
            ?: palette.getMutedColor(0)
                .takeIf { it != 0 }
            ?: primary

        return SkinColors(
            main = Color(primary),
            accent = Color(accent),
            background = Color(palette.getDarkMutedColor(0)),
            isDark = isDark,
        )
    }
}

@Composable
fun rememberSkinColors(
    bitmap: Bitmap?,
    isDark: Boolean,
): SkinColors? {
    var skinColors by remember(bitmap, isDark) { mutableStateOf<SkinColors?>(null) }

    LaunchedEffect(bitmap, isDark) {
        if (bitmap != null) {
            skinColors = MangaColorExtractor.extract(bitmap, isDark)
        }
    }

    return skinColors
}
