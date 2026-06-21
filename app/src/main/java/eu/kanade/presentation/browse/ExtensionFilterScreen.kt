package eu.kanade.presentation.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
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
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.history.components.ItemPosition
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionFilterState
import eu.kanade.tachiyomi.util.system.LocaleHelper
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HikariGroupedListItem
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen

@Composable
fun ExtensionFilterScreen(
    navigateUp: () -> Unit,
    state: ExtensionFilterState.Success,
    onClickToggle: (String) -> Unit,
    onClickSelectAll: (() -> Unit)? = null,
    onClickSelectInverse: (() -> Unit)? = null,
    onClickReset: (() -> Unit)? = null,
) {
    var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val filteredLanguages = remember(searchQuery, state.languages) {
        val query = searchQuery ?: ""
        if (query.isEmpty()) {
            state.languages
        } else {
            state.languages.filter { language ->
                val name = LocaleHelper.getSourceDisplayName(language, context)
                name.contains(query, ignoreCase = true) || language.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = { scrollBehavior ->
            SearchToolbar(
                searchQuery = searchQuery,
                onChangeSearchQuery = { searchQuery = it },
                placeholderText = stringResource(MR.strings.action_search_hint),
                titleContent = { Text(text = stringResource(MR.strings.label_extensions)) },
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
                stringRes = MR.strings.empty_screen,
                modifier = Modifier.padding(contentPadding),
            )
            return@Scaffold
        }
        ExtensionFilterContent(
            contentPadding = contentPadding,
            languages = filteredLanguages,
            enabledLanguages = state.enabledLanguages,
            onClickLang = onClickToggle,
        )
    }
}

@Composable
private fun ExtensionFilterContent(
    contentPadding: PaddingValues,
    languages: List<String>,
    enabledLanguages: Set<String>,
    onClickLang: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = contentPadding,
    ) {
        itemsIndexed(
            items = languages,
            key = { _, language -> language },
        ) { index, language ->
            val position = when {
                languages.size == 1 -> ItemPosition.Single
                index == 0 -> ItemPosition.First
                index == languages.lastIndex -> ItemPosition.Last
                else -> ItemPosition.Middle
            }
            ExtensionFilterItem(
                modifier = Modifier.animateItem(),
                language = language,
                enabled = language in enabledLanguages,
                position = position,
                onClickLang = onClickLang,
            )
        }
    }
}

@Composable
private fun ExtensionFilterItem(
    language: String,
    enabled: Boolean,
    position: ItemPosition,
    onClickLang: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    HikariGroupedListItem(
        modifier = modifier,
        position = position.toHikariListItemPosition(),
        onClick = { onClickLang(language) },
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
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = language.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))

            Column(
                modifier = Modifier
                    .weight(1f),
            ) {
                Text(
                    text = LocaleHelper.getSourceDisplayName(language, context),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = { onClickLang(language) },
            )
        }
    }
}

private fun ItemPosition.toHikariListItemPosition(): tachiyomi.presentation.core.components.HikariListItemPosition {
    return when (this) {
        ItemPosition.First -> tachiyomi.presentation.core.components.HikariListItemPosition.First
        ItemPosition.Middle -> tachiyomi.presentation.core.components.HikariListItemPosition.Middle
        ItemPosition.Last -> tachiyomi.presentation.core.components.HikariListItemPosition.Last
        ItemPosition.Single -> tachiyomi.presentation.core.components.HikariListItemPosition.Single
    }
}
