package eu.kanade.presentation.track

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.util.system.openInBrowser
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HikariCard
import tachiyomi.presentation.core.components.HikariCardDefaults
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.plus
import tachiyomi.presentation.core.util.runOnEnterKeyPressed
import tachiyomi.presentation.core.util.secondaryItemAlpha

@Composable
fun TrackerSearch(
    state: TextFieldState,
    onDispatchQuery: () -> Unit,
    queryResult: Result<List<TrackSearch>>?,
    selected: TrackSearch?,
    onSelectedChange: (TrackSearch) -> Unit,
    onConfirmSelection: (private: Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    supportsPrivateTracking: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val dispatchQueryAndClearFocus: () -> Unit = {
        onDispatchQuery()
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.padding.medium,
                        top = MaterialTheme.padding.small,
                        end = MaterialTheme.padding.medium,
                        bottom = MaterialTheme.padding.small,
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = HikariCardDefaults.containerColor(HikariCardDefaults.nestedCardElevation),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.padding.small, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BasicTextField(
                        state = state,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .runOnEnterKeyPressed(action = dispatchQueryAndClearFocus),
                        textStyle = MaterialTheme.typography.bodyLarge
                            .copy(color = MaterialTheme.colorScheme.onSurface),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        onKeyboardAction = { dispatchQueryAndClearFocus() },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorator = {
                            if (state.text.isEmpty()) {
                                Text(
                                    text = stringResource(MR.strings.action_search_hint),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            it()
                        },
                    )
                    if (state.text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                state.clearText()
                                focusRequester.requestFocus()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = dispatchQueryAndClearFocus) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(MR.strings.action_search),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selected != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = slideOutVertically { it / 2 } + fadeOut(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(MaterialTheme.padding.small)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    Button(
                        onClick = { onConfirmSelection(false) },
                        modifier = Modifier.weight(1f),
                        elevation = ButtonDefaults.elevatedButtonElevation(),
                    ) {
                        Text(text = stringResource(MR.strings.action_track))
                    }
                    if (supportsPrivateTracking) {
                        Button(
                            onClick = { onConfirmSelection(true) },
                            elevation = ButtonDefaults.elevatedButtonElevation(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = stringResource(MR.strings.action_toggle_private_on),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        if (queryResult == null) {
            LoadingScreen(modifier = Modifier.padding(innerPadding))
        } else {
            val availableTracks = queryResult.getOrNull()
            if (availableTracks != null) {
                if (availableTracks.isEmpty()) {
                    EmptyScreen(
                        modifier = Modifier.padding(innerPadding),
                        stringRes = MR.strings.no_results_found,
                    )
                } else {
                    ScrollbarLazyColumn(
                        contentPadding = innerPadding + PaddingValues(MaterialTheme.padding.medium),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                    ) {
                        items(
                            items = availableTracks,
                            key = { it.hashCode() },
                        ) {
                            SearchResultItem(
                                trackSearch = it,
                                selected = it == selected,
                                onClick = { onSelectedChange(it) },
                            )
                        }
                    }
                }
            } else {
                EmptyScreen(
                    modifier = Modifier.padding(innerPadding),
                    message = queryResult.exceptionOrNull()?.message
                        ?: stringResource(MR.strings.unknown_error),
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    trackSearch: TrackSearch,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard: Clipboard = LocalClipboard.current
    val focusManager = LocalFocusManager.current
    val type = trackSearch.publishing_type.toLowerCase(Locale.current).capitalize(Locale.current)
    val status = trackSearch.publishing_status.toLowerCase(Locale.current).capitalize(Locale.current)
    val description = trackSearch.summary.trim()
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    HikariCard(
        modifier = Modifier
            .fillMaxWidth(),
        selected = selected,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onLongClick = { dropDownMenuExpanded = true },
                    onClick = {
                        focusManager.clearFocus()
                        onClick()
                    },
                )
                .padding(MaterialTheme.padding.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
            ) {
                MangaCover.Book(
                    data = trackSearch.cover_url,
                    modifier = Modifier.height(112.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (selected) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = stringResource(MR.strings.selected),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            Text(
                                text = trackSearch.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (trackSearch.authors.isNotEmpty() || trackSearch.artists.isNotEmpty()) {
                                Text(
                                    text = (trackSearch.authors + trackSearch.artists).distinct().joinToString(),
                                    modifier = Modifier.secondaryItemAlpha(),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        SearchResultItemDropDownMenuButton(
                            expanded = dropDownMenuExpanded,
                            onExpand = { dropDownMenuExpanded = true },
                            onCollapseMenu = { dropDownMenuExpanded = false },
                            onCopyName = {
                                scope.launch {
                                    val clipEntry = ClipData.newPlainText(
                                        trackSearch.title,
                                        trackSearch.title,
                                    ).toClipEntry()
                                    clipboard.setClipEntry(clipEntry)
                                }
                            },
                            onOpenInBrowser = {
                                val url = trackSearch.tracking_url
                                if (url.isNotBlank()) {
                                    context.openInBrowser(url)
                                }
                            },
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                    ) {
                        if (type.isNotBlank()) {
                            SearchResultMetaChip(
                                title = stringResource(MR.strings.track_type),
                                text = type,
                            )
                        }
                        if (status.isNotBlank()) {
                            SearchResultMetaChip(
                                title = stringResource(MR.strings.track_status),
                                text = status,
                            )
                        }
                        if (trackSearch.start_date.isNotBlank()) {
                            SearchResultMetaChip(
                                title = stringResource(MR.strings.label_started),
                                text = trackSearch.start_date,
                            )
                        }
                        if (trackSearch.score != -1.0) {
                            SearchResultMetaChip(
                                title = stringResource(MR.strings.score),
                                text = trackSearch.score.toString(),
                            )
                        }
                    }
                }
            }

            if (description.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = HikariCardDefaults.containerColor(HikariCardDefaults.nestedCardElevation),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.padding.medium,
                            vertical = MaterialTheme.padding.small,
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = description,
                            modifier = Modifier.secondaryItemAlpha(),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItemDropDownMenuButton(
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapseMenu: () -> Unit,
    onCopyName: () -> Unit,
    onOpenInBrowser: () -> Unit,
) {
    Box {
        IconButton(onClick = onExpand) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(MR.strings.label_more),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onCollapseMenu,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(MR.strings.action_copy_to_clipboard)) },
                onClick = {
                    onCopyName()
                    onCollapseMenu()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(MR.strings.action_open_in_browser)) },
                onClick = {
                    onOpenInBrowser()
                    onCollapseMenu()
                },
            )
        }
    }
}

@Composable
private fun SearchResultMetaChip(
    title: String,
    text: String,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = HikariCardDefaults.containerColor(HikariCardDefaults.nestedCardElevation),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TrackerSearchPreviews(
    @PreviewParameter(TrackerSearchPreviewProvider::class)
    content: @Composable () -> Unit,
) {
    TachiyomiPreviewTheme { content() }
}
