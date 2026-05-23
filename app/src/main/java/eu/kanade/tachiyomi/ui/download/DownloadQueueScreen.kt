package eu.kanade.tachiyomi.ui.download

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.components.NestedMenuItem
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.download.model.Download
import kotlinx.collections.immutable.persistentListOf
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.HikariCardDefaults
import tachiyomi.presentation.core.components.HikariGroupedListItem
import tachiyomi.presentation.core.components.HikariListItemPosition
import tachiyomi.presentation.core.components.Pill
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.util.shouldExpandFAB
import androidx.compose.material3.DropdownMenu as MaterialDropdownMenu

object DownloadQueueScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { DownloadQueueScreenModel() }
        val downloadSections by screenModel.state.collectAsState()
        val downloadCount by remember {
            derivedStateOf { downloadSections.sumOf { it.downloads.size } }
        }

        val lazyListState = rememberLazyListState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            topBar = {
                AppBar(
                    titleContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(MR.strings.label_download_queue),
                                maxLines = 1,
                                modifier = Modifier.weight(1f, false),
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (downloadCount > 0) {
                                val pillAlpha = if (isSystemInDarkTheme()) 0.12f else 0.08f
                                Pill(
                                    text = "$downloadCount",
                                    modifier = Modifier.padding(start = 4.dp),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = pillAlpha),
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    },
                    navigateUp = navigator::pop,
                    actions = {
                        if (downloadSections.isNotEmpty()) {
                            SortActions(screenModel)
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                val isRunning by screenModel.isDownloaderRunning.collectAsState()
                SmallExtendedFloatingActionButton(
                    text = {
                        val id = if (isRunning) {
                            MR.strings.action_pause
                        } else {
                            MR.strings.action_resume
                        }
                        Text(text = stringResource(id))
                    },
                    icon = {
                        val icon = if (isRunning) {
                            Icons.Outlined.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    },
                    onClick = {
                        if (isRunning) {
                            screenModel.pauseDownloads()
                        } else {
                            screenModel.startDownloads()
                        }
                    },
                    expanded = lazyListState.shouldExpandFAB(),
                    modifier = Modifier.animateFloatingActionButton(
                        visible = downloadSections.isNotEmpty(),
                        alignment = Alignment.BottomEnd,
                    ),
                )
            },
        ) { contentPadding ->
            if (downloadSections.isEmpty()) {
                EmptyScreen(
                    stringRes = MR.strings.information_no_downloads,
                    modifier = Modifier.padding(contentPadding),
                )
                return@Scaffold
            }

            val reorderableState = rememberReorderableLazyListState(lazyListState, contentPadding) { from, to ->
                val fromChapterId = from.key as? Long ?: return@rememberReorderableLazyListState
                val toChapterId = to.key as? Long ?: return@rememberReorderableLazyListState
                screenModel.moveWithinSource(fromChapterId, toChapterId)
            }

            FastScrollLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = contentPadding,
            ) {
                downloadSections.forEach { section ->
                    item(
                        key = "source-${section.sourceId}",
                        contentType = "download_source_header",
                    ) {
                        DownloadSourceHeader(
                            sourceName = section.sourceName,
                            count = section.downloads.size,
                            position = HikariListItemPosition.First,
                            modifier = Modifier.animateItem(),
                        )
                    }
                    itemsIndexed(
                        items = section.downloads,
                        key = { _, download -> download.chapter.id },
                        contentType = { _, _ -> "download_queue_item" },
                    ) { index, download ->
                        DownloadItemContainer(
                            download = download,
                            position = section.downloads.toHikariPositionWithHeader(index),
                            canMoveToTop = index > 0,
                            canMoveToBottom = index < section.downloads.lastIndex,
                            state = reorderableState,
                            screenModel = screenModel,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SortActions(screenModel: DownloadQueueScreenModel) {
        var sortExpanded by remember { mutableStateOf(false) }
        val onDismissRequest = { sortExpanded = false }
        DropdownMenu(
            expanded = sortExpanded,
            onDismissRequest = onDismissRequest,
        ) {
            NestedMenuItem(
                text = { Text(text = stringResource(MR.strings.action_order_by_upload_date)) },
                children = { closeMenu ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(MR.strings.action_newest)) },
                        onClick = {
                            screenModel.sortWithinSources(
                                selector = { it.chapter.dateUpload },
                                reverse = true,
                            )
                            closeMenu()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(MR.strings.action_oldest)) },
                        onClick = {
                            screenModel.sortWithinSources(
                                selector = { it.chapter.dateUpload },
                            )
                            closeMenu()
                        },
                    )
                },
            )
            NestedMenuItem(
                text = { Text(text = stringResource(MR.strings.action_order_by_chapter_number)) },
                children = { closeMenu ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(MR.strings.action_asc)) },
                        onClick = {
                            screenModel.sortWithinSources(
                                selector = { it.chapter.chapterNumber },
                            )
                            closeMenu()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(MR.strings.action_desc)) },
                        onClick = {
                            screenModel.sortWithinSources(
                                selector = { it.chapter.chapterNumber },
                                reverse = true,
                            )
                            closeMenu()
                        },
                    )
                },
            )
        }

        AppBarActions(
            persistentListOf(
                AppBar.Action(
                    title = stringResource(MR.strings.action_sort),
                    icon = Icons.AutoMirrored.Outlined.Sort,
                    onClick = { sortExpanded = true },
                ),
                AppBar.OverflowAction(
                    title = stringResource(MR.strings.action_cancel_all),
                    onClick = { screenModel.clearQueue() },
                ),
            ),
        )
    }

    @Composable
    private fun DownloadSourceHeader(
        sourceName: String,
        count: Int,
        position: HikariListItemPosition,
        modifier: Modifier = Modifier,
    ) {
        HikariGroupedListItem(
            position = position,
            bottomPadding = 0.dp,
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.padding.medium,
                        vertical = MaterialTheme.padding.small,
                    ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sourceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Pill(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    @Composable
    private fun LazyItemScope.DownloadItemContainer(
        download: Download,
        position: HikariListItemPosition,
        canMoveToTop: Boolean,
        canMoveToBottom: Boolean,
        state: ReorderableLazyListState,
        screenModel: DownloadQueueScreenModel,
    ) {
        ReorderableItem(
            state = state,
            key = download.chapter.id,
        ) { _ ->
            HikariGroupedListItem(
                modifier = Modifier.animateItem(),
                position = position,
                containerColor = HikariCardDefaults.containerColor(HikariCardDefaults.cardElevation),
            ) {
                DownloadQueueItem(
                    download = download,
                    canMoveToTop = canMoveToTop,
                    canMoveToBottom = canMoveToBottom,
                    scope = this@ReorderableItem,
                    screenModel = screenModel,
                )
            }
        }
    }

    @Composable
    private fun DownloadQueueItem(
        download: Download,
        canMoveToTop: Boolean,
        canMoveToBottom: Boolean,
        scope: ReorderableCollectionItemScope,
        screenModel: DownloadQueueScreenModel,
    ) {
        val status by download.statusFlow.collectAsState()
        val progress by download.progressFlow.collectAsState(download.progress)
        val pages = download.pages
        val progressText = pages?.let { "${download.downloadedImages}/${it.size}" }
        val statusPresentation = status.toPresentation(progressText)

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.DragHandle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = with(scope) {
                        Modifier.draggableHandle()
                    },
                )
            },
            headlineContent = {
                Text(
                    text = download.manga.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = download.chapter.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        statusPresentation.trailingText?.let { trailingText ->
                            Text(
                                text = trailingText,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusPresentation.tint,
                                maxLines = 1,
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = statusPresentation.icon,
                            contentDescription = null,
                            tint = statusPresentation.tint,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = statusPresentation.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusPresentation.tint,
                            maxLines = 1,
                        )
                    }
                    if (statusPresentation.showProgress) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = statusPresentation.tint,
                        )
                    }
                }
            },
            trailingContent = {
                DownloadItemMenu(
                    download = download,
                    canMoveToTop = canMoveToTop,
                    canMoveToBottom = canMoveToBottom,
                    screenModel = screenModel,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }

    @Composable
    private fun DownloadItemMenu(
        download: Download,
        canMoveToTop: Boolean,
        canMoveToBottom: Boolean,
        screenModel: DownloadQueueScreenModel,
    ) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(MR.strings.action_menu),
                )
            }
            MaterialDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                if (canMoveToTop) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(MR.strings.action_move_to_top)) },
                        onClick = {
                            expanded = false
                            screenModel.moveDownload(download, toTop = true)
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text(text = stringResource(MR.strings.action_move_to_top_all_for_series)) },
                    onClick = {
                        expanded = false
                        screenModel.moveSeries(download, toTop = true)
                    },
                )
                if (canMoveToBottom) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(MR.strings.action_move_to_bottom)) },
                        onClick = {
                            expanded = false
                            screenModel.moveDownload(download, toTop = false)
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text(text = stringResource(MR.strings.action_move_to_bottom_all_for_series)) },
                    onClick = {
                        expanded = false
                        screenModel.moveSeries(download, toTop = false)
                    },
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(MR.strings.action_cancel)) },
                    onClick = {
                        expanded = false
                        screenModel.cancelDownload(download)
                    },
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(MR.strings.cancel_all_for_series)) },
                    onClick = {
                        expanded = false
                        screenModel.cancelSeries(download)
                    },
                )
            }
        }
    }

    @Composable
    private fun Download.State.statusColor(): Color {
        return when (this) {
            Download.State.DOWNLOADING -> MaterialTheme.colorScheme.primary
            Download.State.DOWNLOADED -> MaterialTheme.colorScheme.tertiary
            Download.State.ERROR -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.outline
        }
    }

    @Composable
    private fun Download.State.toPresentation(progressText: String?): DownloadStatusPresentation {
        val tint = statusColor()
        return when (this) {
            Download.State.QUEUE -> DownloadStatusPresentation(
                label = stringResource(MR.strings.download_status_queued),
                trailingText = progressText,
                icon = Icons.Outlined.Schedule,
                tint = tint,
                showProgress = false,
            )
            Download.State.DOWNLOADING -> DownloadStatusPresentation(
                label = stringResource(MR.strings.download_status_downloading),
                trailingText = progressText,
                icon = Icons.Filled.PlayArrow,
                tint = tint,
                showProgress = true,
            )
            Download.State.DOWNLOADED -> DownloadStatusPresentation(
                label = stringResource(MR.strings.completed),
                trailingText = progressText,
                icon = Icons.Outlined.CheckCircle,
                tint = tint,
                showProgress = false,
            )
            Download.State.ERROR -> DownloadStatusPresentation(
                label = stringResource(MR.strings.chapter_error),
                trailingText = progressText,
                icon = Icons.Outlined.ErrorOutline,
                tint = tint,
                showProgress = false,
            )
            Download.State.NOT_DOWNLOADED -> DownloadStatusPresentation(
                label = stringResource(MR.strings.download_status_queued),
                trailingText = progressText,
                icon = Icons.Outlined.Schedule,
                tint = tint,
                showProgress = false,
            )
        }
    }

    private fun List<Download>.toHikariPositionWithHeader(index: Int): HikariListItemPosition {
        return when {
            index == lastIndex -> HikariListItemPosition.Last
            else -> HikariListItemPosition.Middle
        }
    }
}

private data class DownloadStatusPresentation(
    val label: String,
    val trailingText: String?,
    val icon: ImageVector,
    val tint: Color,
    val showProgress: Boolean,
)
