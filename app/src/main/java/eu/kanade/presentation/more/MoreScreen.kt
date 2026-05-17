package eu.kanade.presentation.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
) {
    Scaffold { contentPadding ->
        LazyColumn(
            modifier = Modifier.padding(contentPadding),
        ) {
            item {
                LogoHeader()
            }

            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column {
                        SwitchPreferenceWidget(
                            title = stringResource(MR.strings.label_downloaded_only),
                            subtitle = stringResource(MR.strings.downloaded_only_summary),
                            icon = Icons.Outlined.CloudOff,
                            checked = downloadedOnly,
                            onCheckedChanged = onDownloadedOnlyChange,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        )
                        SwitchPreferenceWidget(
                            title = stringResource(MR.strings.pref_incognito_mode),
                            subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                            icon = ImageVector.vectorResource(R.drawable.ic_glasses_24dp),
                            checked = incognitoMode,
                            onCheckedChanged = onIncognitoModeChange,
                        )
                    }
                }
            }

            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column {
                        val downloadQueueState = downloadQueueStateProvider()
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_download_queue),
                            subtitle = when (downloadQueueState) {
                                DownloadQueueState.Stopped -> null
                                is DownloadQueueState.Paused -> {
                                    val pending = downloadQueueState.pending
                                    if (pending == 0) {
                                        stringResource(MR.strings.paused)
                                    } else {
                                        "${stringResource(MR.strings.paused)} • ${
                                            pluralStringResource(
                                                MR.plurals.download_queue_summary,
                                                count = pending,
                                                pending,
                                            )
                                        }"
                                    }
                                }
                                is DownloadQueueState.Downloading -> {
                                    val pending = downloadQueueState.pending
                                    pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                                }
                            },
                            icon = Icons.Outlined.GetApp,
                            widget = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            },
                            onPreferenceClick = onClickDownloadQueue,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        )
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.categories),
                            icon = Icons.AutoMirrored.Outlined.Label,
                            widget = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            },
                            onPreferenceClick = onClickCategories,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        )
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_stats),
                            icon = Icons.Outlined.QueryStats,
                            widget = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            },
                            onPreferenceClick = onClickStats,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        )
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_data_storage),
                            icon = Icons.Outlined.Storage,
                            widget = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            },
                            onPreferenceClick = onClickDataAndStorage,
                        )
                    }
                }
            }

            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_settings),
                            icon = Icons.Outlined.Settings,
                            widget = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            },
                            onPreferenceClick = onClickSettings,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        )
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.pref_category_about),
                            icon = Icons.Outlined.Info,
                            widget = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            },
                            onPreferenceClick = onClickAbout,
                        )
                    }
                }
            }
        }
    }
}
