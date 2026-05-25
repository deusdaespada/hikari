package eu.kanade.presentation.track

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.track.components.TrackLogoIcon
import eu.kanade.tachiyomi.data.track.Tracker
import eu.kanade.tachiyomi.ui.manga.track.TrackItem
import eu.kanade.tachiyomi.util.lang.toLocalDate
import eu.kanade.tachiyomi.util.system.copyToClipboard
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HikariCard
import tachiyomi.presentation.core.components.HikariCardDefaults
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import java.time.format.DateTimeFormatter

@Composable
fun TrackInfoDialogHome(
    trackItems: List<TrackItem>,
    dateFormat: DateTimeFormatter,
    onStatusClick: (TrackItem) -> Unit,
    onChapterClick: (TrackItem) -> Unit,
    onScoreClick: (TrackItem) -> Unit,
    onStartDateEdit: (TrackItem) -> Unit,
    onEndDateEdit: (TrackItem) -> Unit,
    onNewSearch: (TrackItem) -> Unit,
    onOpenInBrowser: (TrackItem) -> Unit,
    onRemoved: (TrackItem) -> Unit,
    onCopyLink: (TrackItem) -> Unit,
    onTogglePrivate: (TrackItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = MaterialTheme.padding.medium,
            top = MaterialTheme.padding.small,
            end = MaterialTheme.padding.medium,
            bottom = MaterialTheme.padding.large,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
    ) {
        if (trackItems.isEmpty()) {
            item {
                EmptyTrackingState()
            }
        }

        itemsIndexed(
            items = trackItems,
            key = { _, item -> item.tracker.id },
        ) { index, item ->
            if (item.track != null) {
                TrackInfoItem(
                    title = item.track.title,
                    tracker = item.tracker,
                    status = item.tracker.getStatus(item.track.status),
                    onStatusClick = { onStatusClick(item) },
                    chapters = "${item.track.lastChapterRead.toInt()}".let {
                        val totalChapters = item.track.totalChapters
                        if (totalChapters > 0) "$it / $totalChapters" else it
                    },
                    onChaptersClick = { onChapterClick(item) },
                    score = item.tracker.displayScore(item.track)
                        .takeIf { item.tracker.getScoreList().isNotEmpty() && item.track.score != 0.0 },
                    onScoreClick = { onScoreClick(item) }
                        .takeIf { item.tracker.getScoreList().isNotEmpty() },
                    startDate = dateFormat.format(item.track.startDate.toLocalDate())
                        .takeIf { item.tracker.supportsReadingDates && item.track.startDate != 0L },
                    onStartDateClick = { onStartDateEdit(item) }
                        .takeIf { item.tracker.supportsReadingDates },
                    endDate = dateFormat.format(item.track.finishDate.toLocalDate())
                        .takeIf { item.tracker.supportsReadingDates && item.track.finishDate != 0L },
                    onEndDateClick = { onEndDateEdit(item) }
                        .takeIf { item.tracker.supportsReadingDates },
                    onNewSearch = { onNewSearch(item) },
                    onOpenInBrowser = { onOpenInBrowser(item) },
                    onRemoved = { onRemoved(item) },
                    onCopyLink = { onCopyLink(item) },
                    private = item.track.private,
                    onTogglePrivate = { onTogglePrivate(item) }
                        .takeIf { item.tracker.supportsPrivateTracking },
                )
            } else {
                TrackInfoItemEmpty(
                    tracker = item.tracker,
                    onNewSearch = { onNewSearch(item) },
                )
            }

            if (index != trackItems.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = MaterialTheme.padding.medium),
                    color = HikariCardDefaults.dividerColor(),
                )
            }
        }
    }
}

@Composable
private fun TrackInfoItem(
    title: String,
    tracker: Tracker,
    status: StringResource?,
    onStatusClick: () -> Unit,
    chapters: String,
    onChaptersClick: () -> Unit,
    score: String?,
    onScoreClick: (() -> Unit)?,
    startDate: String?,
    onStartDateClick: (() -> Unit)?,
    endDate: String?,
    onEndDateClick: (() -> Unit)?,
    onNewSearch: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onRemoved: () -> Unit,
    onCopyLink: () -> Unit,
    private: Boolean,
    onTogglePrivate: (() -> Unit)?,
) {
    val context = LocalContext.current

    HikariCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.padding.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
            ) {
                Box {
                    TrackLogoIcon(
                        tracker = tracker,
                        onClick = onOpenInBrowser,
                        onLongClick = onCopyLink,
                    )
                    if (private) {
                        Badge(
                            modifier = Modifier.align(Alignment.TopEnd),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = stringResource(MR.strings.tracked_privately),
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = onNewSearch,
                            onLongClick = { context.copyToClipboard(title, title) },
                        ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = tracker.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TrackInfoItemMenu(
                    onOpenInBrowser = onOpenInBrowser,
                    onRemoved = onRemoved,
                    onCopyLink = onCopyLink,
                    private = private,
                    onTogglePrivate = onTogglePrivate,
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            ) {
                TrackMetricChip(
                    label = stringResource(MR.strings.status),
                    value = status?.let { stringResource(it) } ?: "-",
                    onClick = onStatusClick,
                )
                TrackMetricChip(
                    label = stringResource(MR.strings.chapters),
                    value = chapters,
                    onClick = onChaptersClick,
                )
                if (onScoreClick != null) {
                    TrackMetricChip(
                        label = stringResource(MR.strings.score),
                        value = score ?: stringResource(MR.strings.score),
                        muted = score == null,
                        onClick = onScoreClick,
                    )
                }
                if (onStartDateClick != null) {
                    TrackMetricChip(
                        label = stringResource(MR.strings.track_started_reading_date),
                        value = startDate ?: stringResource(MR.strings.track_started_reading_date),
                        muted = startDate == null,
                        onClick = onStartDateClick,
                    )
                }
                if (onEndDateClick != null) {
                    TrackMetricChip(
                        label = stringResource(MR.strings.track_finished_reading_date),
                        value = endDate ?: stringResource(MR.strings.track_finished_reading_date),
                        muted = endDate == null,
                        onClick = onEndDateClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackMetricChip(
    label: String,
    value: String,
    onClick: () -> Unit,
    muted: Boolean = false,
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = HikariCardDefaults.containerColor(HikariCardDefaults.nestedCardElevation),
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 112.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (muted) 0.6f else 1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TrackInfoItemEmpty(
    tracker: Tracker,
    onNewSearch: () -> Unit,
) {
    HikariCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.padding.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
        ) {
            TrackLogoIcon(tracker)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tracker.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(MR.strings.add_tracking),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = onNewSearch,
                label = { Text(stringResource(MR.strings.add_tracking)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun EmptyTrackingState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = HikariCardDefaults.containerColor(HikariCardDefaults.cardElevation),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.padding.large, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Text(
                text = stringResource(MR.strings.manga_tracking_tab),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(MR.strings.add_tracking),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TrackInfoItemMenu(
    onOpenInBrowser: () -> Unit,
    onRemoved: () -> Unit,
    onCopyLink: () -> Unit,
    private: Boolean,
    onTogglePrivate: (() -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(MR.strings.label_more),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(MR.strings.action_open_in_browser)) },
                onClick = {
                    onOpenInBrowser()
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(MR.strings.action_copy_link)) },
                onClick = {
                    onCopyLink()
                    expanded = false
                },
            )
            if (onTogglePrivate != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(
                                if (private) MR.strings.action_toggle_private_off else MR.strings.action_toggle_private_on,
                            ),
                        )
                    },
                    onClick = {
                        onTogglePrivate()
                        expanded = false
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(MR.strings.action_remove)) },
                onClick = {
                    onRemoved()
                    expanded = false
                },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TrackInfoDialogHomePreviews(
    @PreviewParameter(TrackInfoDialogHomePreviewProvider::class)
    content: @Composable () -> Unit,
) {
    TachiyomiPreviewTheme {
        Surface {
            content()
        }
    }
}
