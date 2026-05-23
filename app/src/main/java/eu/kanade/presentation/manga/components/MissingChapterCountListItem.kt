package eu.kanade.presentation.manga.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HikariSectionHeader
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.util.secondaryItemAlpha

@Composable
fun MissingChapterCountListItem(
    count: Int,
    modifier: Modifier = Modifier,
) {
    HikariSectionHeader(
        text = pluralStringResource(MR.plurals.missing_chapters, count = count, count),
        modifier = modifier.secondaryItemAlpha(),
    )
    Row(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.padding.medium)
            .secondaryItemAlpha(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    TachiyomiPreviewTheme {
        Surface {
            MissingChapterCountListItem(count = 42)
        }
    }
}
