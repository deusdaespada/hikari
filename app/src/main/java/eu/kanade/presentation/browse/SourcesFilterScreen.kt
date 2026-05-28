package eu.kanade.presentation.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.browse.components.BaseSourceItem
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.history.components.ItemPosition
import eu.kanade.presentation.util.animateItemFastScroll
import eu.kanade.tachiyomi.ui.browse.source.SourcesFilterScreenModel
import eu.kanade.tachiyomi.util.system.LocaleHelper
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.HikariGroupedListItem
import androidx.compose.material3.Icon
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen

@Composable
fun SourcesFilterScreen(
    navigateUp: () -> Unit,
    state: SourcesFilterScreenModel.State.Success,
    onClickLanguage: (String) -> Unit,
    onClickSource: (Source) -> Unit,
    onClickSelectAll: (() -> Unit)? = null,
    onClickSelectInverse: (() -> Unit)? = null,
    onClickReset: (() -> Unit)? = null,
) {
    var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val filteredItems = remember(searchQuery, state.items) {
        val query = searchQuery ?: ""
        if (query.isEmpty()) {
            state.items
        } else {
            state.items.mapValues { (language, sources) ->
                val langName = LocaleHelper.getSourceDisplayName(language, context)
                if (langName.contains(query, ignoreCase = true) || language.contains(query, ignoreCase = true)) {
                    sources
                } else {
                    sources.filter { it.name.contains(query, ignoreCase = true) }
                }
            }.filterValues { it.isNotEmpty() }.toSortedMap()
        }
    }

    Scaffold(
        topBar = { scrollBehavior ->
            SearchToolbar(
                searchQuery = searchQuery,
                onChangeSearchQuery = { searchQuery = it },
                placeholderText = stringResource(MR.strings.action_search_hint),
                titleContent = { Text(text = stringResource(MR.strings.label_sources)) },
                navigateUp = navigateUp,
                actions = {
                    if (onClickSelectAll != null) {
                        IconButton(onClick = onClickSelectAll) {
                            Icon(
                                imageVector = Icons.Outlined.SelectAll,
                                contentDescription = stringResource(MR.strings.action_select_all),
                            )
                        }
                    }
                    if (onClickSelectInverse != null) {
                        IconButton(onClick = onClickSelectInverse) {
                            Icon(
                                imageVector = Icons.Outlined.Deselect,
                                contentDescription = stringResource(MR.strings.action_select_inverse),
                            )
                        }
                    }
                    if (onClickReset != null) {
                        IconButton(onClick = onClickReset) {
                            Icon(
                                imageVector = Icons.Outlined.SettingsBackupRestore,
                                contentDescription = stringResource(MR.strings.action_reset),
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        if (state.isEmpty) {
            EmptyScreen(
                stringRes = MR.strings.source_filter_empty_screen,
                modifier = Modifier.padding(contentPadding),
            )
            return@Scaffold
        }
        SourcesFilterContent(
            contentPadding = contentPadding,
            items = filteredItems,
            enabledLanguages = state.enabledLanguages,
            disabledSources = state.disabledSources,
            onClickLanguage = onClickLanguage,
            onClickSource = onClickSource,
        )
    }
}

@Composable
private fun SourcesFilterContent(
    contentPadding: PaddingValues,
    items: Map<String, List<Source>>,
    enabledLanguages: Set<String>,
    disabledSources: Set<String>,
    onClickLanguage: (String) -> Unit,
    onClickSource: (Source) -> Unit,
) {
    FastScrollLazyColumn(
        contentPadding = contentPadding,
    ) {
        items.forEach { (language, sources) ->
            val enabled = language in enabledLanguages
            val enabledSourcesCount = sources.count { "${it.id}" !in disabledSources }
            val totalSourcesCount = sources.size
            val badgeText = if (enabled) "$enabledSourcesCount/$totalSourcesCount" else "$totalSourcesCount"

            item(
                key = language,
                contentType = "source-filter-header",
            ) {
                val headerPosition = if (enabled && sources.isNotEmpty()) ItemPosition.First else ItemPosition.Single
                SourcesFilterHeader(
                    modifier = Modifier.animateItemFastScroll(),
                    language = language,
                    enabled = enabled,
                    badgeText = badgeText,
                    position = headerPosition,
                    onClickItem = onClickLanguage,
                )
            }

            if (enabled) {
                itemsIndexed(
                    items = sources,
                    key = { _, source -> "source-filter-${source.key()}" },
                    contentType = { _, _ -> "source-filter-item" },
                ) { index, source ->
                    val itemPosition = if (index == sources.lastIndex) ItemPosition.Last else ItemPosition.Middle
                    SourcesFilterItem(
                        modifier = Modifier.animateItemFastScroll(),
                        source = source,
                        enabled = "${source.id}" !in disabledSources,
                        position = itemPosition,
                        onClickItem = onClickSource,
                    )
                }
            }
        }
    }
}

@Composable
private fun SourcesFilterHeader(
    language: String,
    enabled: Boolean,
    badgeText: String,
    position: ItemPosition,
    onClickItem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    HikariGroupedListItem(
        modifier = modifier,
        position = position.toHikariListItemPosition(),
        onClick = { onClickItem(language) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = language.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.padding.medium)
                    .weight(1f),
            ) {
                Text(
                    text = LocaleHelper.getSourceDisplayName(language, context),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = { onClickItem(language) },
            )
        }
    }
}

@Composable
private fun SourcesFilterItem(
    source: Source,
    enabled: Boolean,
    position: ItemPosition,
    onClickItem: (Source) -> Unit,
    modifier: Modifier = Modifier,
) {
    BaseSourceItem(
        modifier = modifier,
        source = source,
        position = position,
        showLanguageInContent = false,
        onClickItem = { onClickItem(source) },
        action = {
            Checkbox(checked = enabled, onCheckedChange = null)
        },
    )
}

private fun ItemPosition.toHikariListItemPosition(): tachiyomi.presentation.core.components.HikariListItemPosition {
    return when (this) {
        ItemPosition.First -> tachiyomi.presentation.core.components.HikariListItemPosition.First
        ItemPosition.Middle -> tachiyomi.presentation.core.components.HikariListItemPosition.Middle
        ItemPosition.Last -> tachiyomi.presentation.core.components.HikariListItemPosition.Last
        ItemPosition.Single -> tachiyomi.presentation.core.components.HikariListItemPosition.Single
    }
}
