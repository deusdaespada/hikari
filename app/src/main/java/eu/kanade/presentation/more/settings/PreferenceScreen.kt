package eu.kanade.presentation.more.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import eu.kanade.presentation.more.settings.screen.SearchableSettings
import eu.kanade.presentation.more.settings.widget.PreferenceGroupHeader
import kotlinx.coroutines.delay
import tachiyomi.presentation.core.components.HikariCardGroup
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.padding
import kotlin.time.Duration.Companion.seconds

/**
 * Preference Screen composable which contains a list of [Preference] items
 * @param items [Preference] items which should be displayed on the preference screen. An item can be a single [PreferenceItem] or a group ([Preference.PreferenceGroup])
 * @param modifier [Modifier] to be applied to the preferenceScreen layout
 */
@Composable
fun PreferenceScreen(
    items: List<Preference>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val state = rememberLazyListState()
    val highlightKey = SearchableSettings.highlightKey
    if (highlightKey != null) {
        LaunchedEffect(Unit) {
            val i = items.findHighlightedIndex(highlightKey)
            if (i >= 0) {
                delay(0.5.seconds)
                state.animateScrollToItem(i)
            }
            SearchableSettings.highlightKey = null
        }
    }

    ScrollbarLazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
    ) {
        items.fastForEachIndexed { i, preference ->
            when (preference) {
                is Preference.PreferenceGroup -> {
                    if (!preference.enabled) return@fastForEachIndexed

                    item(key = "header_${preference.title}_$i") {
                        PreferenceGroupHeader(title = preference.title)
                    }
                    item(key = "card_${preference.title}_$i") {
                        HikariCardGroup {
                            Column {
                                preference.preferenceItems.fastForEachIndexed { index, item ->
                                    val showDivider = index < preference.preferenceItems.lastIndex
                                    CompositionLocalProvider(LocalPreferenceShowDivider provides showDivider) {
                                        PreferenceItem(
                                            item = item,
                                            highlightKey = highlightKey,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(key = "spacer_${preference.title}_$i") {
                        if (i < items.lastIndex) {
                            Spacer(modifier = Modifier.height(MaterialTheme.padding.medium))
                        }
                    }
                }

                is Preference.PreferenceItem<*, *> -> item {
                    PreferenceItem(
                        item = preference,
                        highlightKey = highlightKey,
                    )
                }
            }
        }
    }
}

private fun List<Preference>.findHighlightedIndex(highlightKey: String): Int {
    var lazyColumnIndex = 0
    forEach { preference ->
        when (preference) {
            is Preference.PreferenceGroup -> {
                if (!preference.enabled) return@forEach

                lazyColumnIndex++

                if (preference.preferenceItems.any { it.title == highlightKey }) {
                    return lazyColumnIndex
                }
                lazyColumnIndex++

                lazyColumnIndex++
            }

            is Preference.PreferenceItem<*, *> -> {
                if (!preference.enabled) return@forEach
                if (preference.title == highlightKey) {
                    return lazyColumnIndex
                }
                lazyColumnIndex++
            }
        }
    }
    return -1
}
