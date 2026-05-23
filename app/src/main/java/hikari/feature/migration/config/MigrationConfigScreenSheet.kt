package hikari.feature.migration.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEach
import eu.kanade.core.util.fastFilterNot
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.presentation.components.AdaptiveSheet
import hikari.domain.migration.models.MigrationFlag
import hikari.feature.common.utils.getLabel
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.getAndSet
import tachiyomi.core.common.preference.toggle
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HikariCard
import tachiyomi.presentation.core.components.HikariCardDefaults
import tachiyomi.presentation.core.components.HikariCardGroup
import tachiyomi.presentation.core.components.material.Button
import tachiyomi.presentation.core.components.material.Switch
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.active
import tachiyomi.presentation.core.util.collectAsState

@Composable
fun MigrationConfigScreenSheet(
    preferences: SourcePreferences,
    onDismissRequest: () -> Unit,
    onStartMigration: (extraSearchQuery: String?) -> Unit,
) {
    var extraSearchQuery by rememberSaveable { mutableStateOf("") }
    val migrationFlags by preferences.migrationFlags.collectAsState()
    AdaptiveSheet(
        onDismissRequest = onDismissRequest,
        header = { MigrationSheetHeader() },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = MaterialTheme.padding.small,
                        bottom = MaterialTheme.padding.extraSmall,
                    ),
            ) {
                MigrationSheetDataSection(
                    migrationFlags = migrationFlags,
                    onToggleFlag = { flag ->
                        preferences.migrationFlags.getAndSet { currentFlags ->
                            if (flag in currentFlags) {
                                currentFlags - flag
                            } else {
                                currentFlags + flag
                            }
                        }
                    },
                    onToggleRemoveDownloads = {
                        preferences.migrationFlags.getAndSet { flags ->
                            if (MigrationFlag.REMOVE_DOWNLOAD in flags) {
                                flags - MigrationFlag.REMOVE_DOWNLOAD
                            } else {
                                flags + MigrationFlag.REMOVE_DOWNLOAD
                            }
                        }
                    },
                )

                MigrationSheetSearchSection(
                    extraSearchQuery = extraSearchQuery,
                    onExtraSearchQueryChange = { extraSearchQuery = it },
                    hideUnmatchedPreference = preferences.migrationHideUnmatched,
                    hideWithoutUpdatesPreference = preferences.migrationHideWithoutUpdates,
                )

                MigrationSheetAdvancedSection(
                    deepSearchModePreference = preferences.migrationDeepSearchMode,
                    prioritizeByChaptersPreference = preferences.migrationPrioritizeByChapters,
                )
            }
            HorizontalDivider()
            MigrationSheetActionBar {
                val cleanedExtraSearchQuery = extraSearchQuery.trim().ifBlank { null }
                onStartMigration(cleanedExtraSearchQuery)
            }
        }
    }
}

@Composable
private fun MigrationSheetHeader() {
    Text(
        text = stringResource(MR.strings.migrationConfigScreen_optionsTitle),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MaterialTheme.padding.medium,
                top = MaterialTheme.padding.small,
                end = MaterialTheme.padding.medium,
                bottom = MaterialTheme.padding.small,
            ),
    )
}

@Composable
private fun MigrationSheetDataSection(
    migrationFlags: Set<MigrationFlag>,
    onToggleFlag: (MigrationFlag) -> Unit,
    onToggleRemoveDownloads: () -> Unit,
) {
    MigrationSheetSection(
        title = stringResource(MR.strings.migrationConfigScreen_dataToMigrateHeader),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.padding.medium,
                    vertical = MaterialTheme.padding.extraSmall,
                ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
        ) {
            MigrationFlag.entries
                .fastFilterNot { it == MigrationFlag.REMOVE_DOWNLOAD }
                .fastForEach { flag ->
                    val selected = flag in migrationFlags
                    FilterChip(
                        selected = selected,
                        onClick = { onToggleFlag(flag) },
                        label = { Text(stringResource(flag.getLabel())) },
                        leadingIcon = {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium))
        MigrationSheetSwitchItem(
            title = stringResource(MR.strings.migrationConfigScreen_removeDownloadsTitle),
            subtitle = null,
            checked = MigrationFlag.REMOVE_DOWNLOAD in migrationFlags,
            onClick = onToggleRemoveDownloads,
        )
    }
}

@Composable
private fun MigrationSheetSearchSection(
    extraSearchQuery: String,
    onExtraSearchQueryChange: (String) -> Unit,
    hideUnmatchedPreference: Preference<Boolean>,
    hideWithoutUpdatesPreference: Preference<Boolean>,
) {
    MigrationSheetSection(
        title = stringResource(MR.strings.migrationConfigScreen_searchMatchingHeader),
    ) {
        OutlinedTextField(
            value = extraSearchQuery,
            onValueChange = onExtraSearchQueryChange,
            label = { Text(stringResource(MR.strings.migrationConfigScreen_additionalSearchQueryLabel)) },
            supportingText = {
                Text(stringResource(MR.strings.migrationConfigScreen_additionalSearchQuerySupportingText))
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.padding.medium,
                    vertical = MaterialTheme.padding.extraSmall,
                ),
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium))
        MigrationSheetSwitchItem(
            title = stringResource(MR.strings.migrationConfigScreen_hideUnmatchedTitle),
            subtitle = null,
            preference = hideUnmatchedPreference,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium))
        MigrationSheetSwitchItem(
            title = stringResource(MR.strings.migrationConfigScreen_hideWithoutUpdatesTitle),
            subtitle = stringResource(MR.strings.migrationConfigScreen_hideWithoutUpdatesSubtitle),
            preference = hideWithoutUpdatesPreference,
        )
    }
}

@Composable
private fun MigrationSheetAdvancedSection(
    deepSearchModePreference: Preference<Boolean>,
    prioritizeByChaptersPreference: Preference<Boolean>,
) {
    MigrationSheetSection(
        title = stringResource(MR.strings.migrationConfigScreen_advancedMatchingHeader),
    ) {
        MigrationSheetWarningItem(stringResource(MR.strings.migrationConfigScreen_enhancedOptionsWarning))
        HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium))
        MigrationSheetSwitchItem(
            title = stringResource(MR.strings.migrationConfigScreen_deepSearchModeTitle),
            subtitle = stringResource(MR.strings.migrationConfigScreen_deepSearchModeSubtitle),
            preference = deepSearchModePreference,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium))
        MigrationSheetSwitchItem(
            title = stringResource(MR.strings.migrationConfigScreen_prioritizeByChaptersTitle),
            subtitle = stringResource(MR.strings.migrationConfigScreen_prioritizeByChaptersSubtitle),
            preference = prioritizeByChaptersPreference,
        )
    }
}

@Composable
private fun MigrationSheetSection(
    title: String,
    content: @Composable () -> Unit,
) {
    HikariCardGroup(
        verticalPadding = MaterialTheme.padding.extraSmall,
        containerColor = HikariCardDefaults.containerColor(HikariCardDefaults.nestedCardElevation),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.padding.medium,
                vertical = MaterialTheme.padding.small,
            ),
        )
        content()
    }
}

@Composable
private fun MigrationSheetActionBar(
    onContinue: () -> Unit,
) {
    Button(
        onClick = onContinue,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.padding.medium,
                vertical = MaterialTheme.padding.small,
            ),
    ) {
        Text(text = stringResource(MR.strings.migrationConfigScreen_continueButtonText))
    }
}

@Composable
private fun MigrationSheetSwitchItem(
    title: String,
    subtitle: String?,
    preference: Preference<Boolean>,
) {
    MigrationSheetSwitchItem(
        title = title,
        subtitle = subtitle,
        checked = preference.collectAsState().value,
        onClick = { preference.toggle() },
    )
}

@Composable
private fun MigrationSheetSwitchItem(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = subtitle?.let { { Text(text = subtitle) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun MigrationSheetWarningItem(
    text: String,
) {
    HikariCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.padding.medium,
                vertical = MaterialTheme.padding.extraSmall,
            ),
        containerColor = MaterialTheme.colorScheme.errorContainer,
        showBorder = false,
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.active,
                )
            },
            headlineContent = {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
