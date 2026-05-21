package eu.kanade.presentation.more.settings.screen.about

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.LogoHeader
import eu.kanade.presentation.util.LocalBackPress
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.updater.AppUpdateChecker
import eu.kanade.tachiyomi.data.updater.RELEASE_URL
import eu.kanade.tachiyomi.ui.more.NewUpdateScreen
import eu.kanade.tachiyomi.util.lang.toDateTimestampString
import kotlinx.coroutines.launch
import logcat.LogPriority
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.lang.withUIContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.release.interactor.GetApplicationRelease
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HikariCard
import tachiyomi.presentation.core.components.HikariCardDefaults
import tachiyomi.presentation.core.components.HikariSectionHeader
import tachiyomi.presentation.core.components.HikariSnackbarHost
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object AboutScreen : Screen() {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val handleBack = LocalBackPress.current
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        var isCheckingUpdates by remember { mutableStateOf(false) }

        val packageManager = context.packageManager
        val packageInfo = remember(context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(context.packageName, 0)
                }
            } catch (_: Exception) {
                null
            }
        }

        val firstInstallTimeText = remember(packageInfo) {
            val installTime = packageInfo?.firstInstallTime ?: 0L
            if (installTime > 0L) {
                try {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(installTime),
                        ZoneId.systemDefault(),
                    ).toDateTimestampString(
                        UiPreferences.dateFormat(
                            Injekt.get<UiPreferences>().dateFormat.get(),
                        ),
                    )
                } catch (_: Exception) {
                    "-"
                }
            } else {
                "-"
            }
        }

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.pref_category_about),
                    navigateUp = if (handleBack != null) handleBack::invoke else null,
                    scrollBehavior = scrollBehavior,
                )
            },
            snackbarHost = { HikariSnackbarHost(hostState = snackbarHostState) },
        ) { contentPadding ->
            ScrollbarLazyColumn(
                contentPadding = contentPadding,
            ) {
                item {
                    LogoHeader(
                        versionName = getVersionName(withBuildDate = true),
                    )
                }

                item {
                    HikariSectionHeader(text = stringResource(MR.strings.pref_category_about))
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.padding.medium),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                        ) {
                            AboutGridCard(
                                title = stringResource(MR.strings.check_for_updates),
                                subtitle = stringResource(MR.strings.update_check_look_for_updates),
                                icon = Icons.Outlined.SystemUpdate,
                                onClick = {
                                    if (!isCheckingUpdates) {
                                        scope.launch {
                                            isCheckingUpdates = true
                                            checkVersion(
                                                context = context,
                                                onAvailableUpdate = { result ->
                                                    val updateScreen = NewUpdateScreen(
                                                        versionName = result.release.version,
                                                        changelogInfo = result.release.info,
                                                        releaseLink = result.release.releaseLink,
                                                        downloadLink = result.release.downloadLink,
                                                    )
                                                    navigator.push(updateScreen)
                                                },
                                                onFinish = { isCheckingUpdates = false },
                                                onError = { message ->
                                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                                },
                                                onNoUpdate = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            context.stringResource(
                                                                MR.strings.update_check_no_new_updates,
                                                            ),
                                                        )
                                                    }
                                                },
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                trailingContent = {
                                    AnimatedVisibility(visible = isCheckingUpdates) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                },
                            )

                            AboutGridCard(
                                title = stringResource(MR.strings.about_licenses_title),
                                subtitle = stringResource(MR.strings.about_open_source_licenses),
                                icon = Icons.Outlined.Gavel,
                                onClick = { navigator.push(OpenSourceLicensesScreen()) },
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                        ) {
                            AboutGridCard(
                                title = stringResource(MR.strings.whats_new),
                                subtitle = stringResource(MR.strings.about_view_changelog),
                                icon = Icons.Outlined.Info,
                                onClick = {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        RELEASE_URL.toUri(),
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                            )

                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.padding.medium))
                    HikariSectionHeader(text = stringResource(MR.strings.about_app_information))
                }

                item {
                    HikariCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.padding.medium),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column {
                            AppInfoRow(
                                label = stringResource(MR.strings.about_package_name),
                                value = context.packageName,
                                icon = Icons.Outlined.Code,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                                color = HikariCardDefaults.dividerColor(),
                            )
                            AppInfoRow(
                                label = stringResource(MR.strings.about_platform),
                                value = stringResource(MR.strings.about_platform_value),
                                icon = Icons.Outlined.Android,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                                color = HikariCardDefaults.dividerColor(),
                            )
                            AppInfoRow(
                                label = stringResource(MR.strings.about_installed_on),
                                value = firstInstallTimeText,
                                icon = Icons.Outlined.CalendarMonth,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                                color = HikariCardDefaults.dividerColor(),
                            )
                            AppInfoRow(
                                label = stringResource(MR.strings.about_version_code),
                                value = BuildConfig.VERSION_CODE.toString(),
                                icon = Icons.Outlined.Smartphone,
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.padding.medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(MR.strings.about_built_with_passion),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(MR.strings.about_copyright),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    @Composable
    private fun AboutGridCard(
        title: String,
        subtitle: String,
        icon: ImageVector,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        trailingContent: @Composable (() -> Unit)? = null,
    ) {
        HikariCard(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(MaterialTheme.padding.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                if (trailingContent != null) {
                    trailingContent()
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun AppInfoRow(
        label: String,
        value: String,
        icon: ImageVector,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.padding.small, horizontal = MaterialTheme.padding.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(MaterialTheme.padding.medium))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }

    /**
     * Checks version and shows a user prompt if an update is available.
     */
    private suspend fun checkVersion(
        context: Context,
        onAvailableUpdate: (GetApplicationRelease.Result.NewUpdate) -> Unit,
        onFinish: () -> Unit,
        onNoUpdate: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val updateChecker = AppUpdateChecker()
        withUIContext {
            try {
                when (val result = withIOContext { updateChecker.checkForUpdate(context, forceCheck = true) }) {
                    is GetApplicationRelease.Result.NewUpdate -> {
                        onAvailableUpdate(result)
                    }

                    is GetApplicationRelease.Result.NoNewUpdate -> {
                        onNoUpdate()
                    }

                    is GetApplicationRelease.Result.OsTooOld -> {
                        onError(context.stringResource(MR.strings.update_check_eol))
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
                logcat(LogPriority.ERROR, e)
            } finally {
                onFinish()
            }
        }
    }

    fun getVersionName(withBuildDate: Boolean): String {
        return run {
            "Stable ${BuildConfig.VERSION_NAME}".let {
                if (withBuildDate) {
                    "$it (${getFormattedBuildTime()})"
                } else {
                    it
                }
            }
        }
    }

    internal fun getFormattedBuildTime(): String {
        return try {
            LocalDateTime.ofInstant(
                Instant.parse(BuildConfig.BUILD_TIME),
                ZoneId.systemDefault(),
            )
                .toDateTimestampString(
                    UiPreferences.dateFormat(
                        Injekt.get<UiPreferences>().dateFormat.get(),
                    ),
                )
        } catch (_: Exception) {
            BuildConfig.BUILD_TIME
        }
    }
}
