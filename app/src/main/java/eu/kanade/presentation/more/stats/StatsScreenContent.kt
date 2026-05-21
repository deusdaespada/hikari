package eu.kanade.presentation.more.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.more.stats.components.StatsItem
import eu.kanade.presentation.more.stats.components.StatsOverviewItem
import eu.kanade.presentation.more.stats.data.StatsData
import eu.kanade.presentation.util.toDurationString
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HikariCard
import tachiyomi.presentation.core.components.HikariCardDefaults
import tachiyomi.presentation.core.components.HikariSectionHeader
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun StatsScreenContent(
    state: StatsScreenState.Success,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
    ) {
        item {
            OverviewSection(state.overview)
        }
        item {
            HeatmapSection(state.heatmap)
        }
        item {
            TitlesStats(state.titles)
        }
        item {
            ChapterStats(state.chapters)
        }
        item {
            TrackerStats(state.trackers)
        }
    }
}

@Composable
private fun OverviewSection(
    data: StatsData.Overview,
) {
    val none = stringResource(MR.strings.none)
    val context = LocalContext.current
    val readDurationString = remember(data.totalReadDuration) {
        data.totalReadDuration
            .toDuration(DurationUnit.MILLISECONDS)
            .toDurationString(context, fallback = none)
    }
    StatsSectionCard(MR.strings.label_overview_section) {
        StatsMetricRow {
            StatsOverviewItem(
                title = data.libraryMangaCount.toString(),
                subtitle = stringResource(MR.strings.in_library),
                icon = Icons.Outlined.CollectionsBookmark,
                modifier = Modifier.weight(1f),
            )
            StatsVerticalDivider()
            StatsOverviewItem(
                title = readDurationString,
                subtitle = stringResource(MR.strings.label_read_duration),
                icon = Icons.Outlined.Schedule,
                modifier = Modifier.weight(1f),
            )
            StatsVerticalDivider()
            StatsOverviewItem(
                title = data.completedMangaCount.toString(),
                subtitle = stringResource(MR.strings.label_completed_titles),
                icon = Icons.Outlined.LocalLibrary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TitlesStats(
    data: StatsData.Titles,
) {
    StatsSectionCard(MR.strings.label_titles_section) {
        StatsMetricRow {
            StatsItem(
                data.globalUpdateItemCount.toString(),
                stringResource(MR.strings.label_titles_in_global_update),
                modifier = Modifier.weight(1f),
            )
            StatsVerticalDivider()
            StatsItem(
                data.startedMangaCount.toString(),
                stringResource(MR.strings.label_started),
                modifier = Modifier.weight(1f),
            )
            StatsVerticalDivider()
            StatsItem(
                data.localMangaCount.toString(),
                stringResource(MR.strings.label_local),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ChapterStats(
    data: StatsData.Chapters,
) {
    StatsSectionCard(MR.strings.chapters) {
        StatsMetricRow {
            StatsItem(
                data.totalChapterCount.toString(),
                stringResource(MR.strings.label_total_chapters),
                modifier = Modifier.weight(1f),
            )
            StatsVerticalDivider()
            StatsItem(
                data.readChapterCount.toString(),
                stringResource(MR.strings.label_read_chapters),
                modifier = Modifier.weight(1f),
            )
            StatsVerticalDivider()
            StatsItem(
                data.downloadCount.toString(),
                stringResource(MR.strings.label_downloaded),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TrackerStats(
    data: StatsData.Trackers,
) {
    val notApplicable = stringResource(MR.strings.not_applicable)
    val meanScoreStr = remember(data.trackedTitleCount, data.meanScore) {
        if (data.trackedTitleCount > 0 && !data.meanScore.isNaN()) {
            "%.2f ★".format(Locale.ENGLISH, data.meanScore)
        } else {
            notApplicable
        }
    }
    StatsSectionCard(MR.strings.label_tracker_section) {
        StatsMetricRow {
            StatsItem(
                data.trackedTitleCount.toString(),
                stringResource(MR.strings.label_tracked_titles),
                modifier = Modifier.weight(1f),
            )
            StatsVerticalDivider()
            StatsItem(
                meanScoreStr,
                stringResource(MR.strings.label_mean_score),
                modifier = Modifier.weight(1f),
            )
            StatsVerticalDivider()
            StatsItem(
                data.trackerCount.toString(),
                stringResource(MR.strings.label_used),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HeatmapSection(
    data: StatsData.HistoryHeatmap,
) {
    StatsSectionCard(MR.strings.label_heatmap_section) {
        var selectedDay by remember { mutableStateOf<Pair<Long, Int>?>(null) }
        val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

        val days = remember(data.history) {
            val list = mutableListOf<Pair<Long, Int>>()
            val cal = Calendar.getInstance()

            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            cal.add(Calendar.WEEK_OF_YEAR, -52)

            repeat(53 * 7) {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val time = cal.timeInMillis
                list.add(time to (data.history[time] ?: 0))
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            list
        }

        val primaryColor = MaterialTheme.colorScheme.primary
        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.padding.small),
        ) {
            val scrollState = rememberScrollState(Int.MAX_VALUE)
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = MaterialTheme.padding.medium),
            ) {
                days.chunked(7).forEach { week ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        week.forEach { (time, count) ->
                            val color = when {
                                count == 0 -> surfaceColor.copy(alpha = 0.2f)
                                count <= 3 -> primaryColor.copy(alpha = 0.3f)
                                count <= 7 -> primaryColor.copy(alpha = 0.5f)
                                count <= 12 -> primaryColor.copy(alpha = 0.8f)
                                else -> primaryColor
                            }

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(color)
                                    .clickable {
                                        selectedDay = if (selectedDay?.first == time) null else time to count
                                    },
                            ) {
                                if (selectedDay?.first == time) {
                                    Popup(
                                        alignment = Alignment.TopCenter,
                                        offset = IntOffset(0, -40),
                                        onDismissRequest = { selectedDay = null },
                                        properties = PopupProperties(focusable = false),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    MaterialTheme.shapes.medium,
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dateFormatter.format(time),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Text(
                                                    text = pluralStringResource(
                                                        MR.plurals.manga_num_chapters,
                                                        count,
                                                        count,
                                                    ),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSectionCard(
    titleRes: StringResource,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        HikariSectionHeader(text = stringResource(titleRes))
        HikariCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.padding.medium),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.padding.medium),
                content = content,
            )
        }
    }
}

@Composable
private fun StatsMetricRow(
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        content = content,
    )
}

@Composable
private fun StatsVerticalDivider() {
    VerticalDivider(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = MaterialTheme.padding.small),
        thickness = 0.5.dp,
        color = HikariCardDefaults.dividerColor(),
    )
}
