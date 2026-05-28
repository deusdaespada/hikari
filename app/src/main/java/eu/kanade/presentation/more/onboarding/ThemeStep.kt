package eu.kanade.presentation.more.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.setAppCompatDelegateThemeMode
import eu.kanade.presentation.more.settings.widget.AppThemeModePreferenceWidget
import eu.kanade.presentation.more.settings.widget.AppThemePreferenceWidget
import tachiyomi.presentation.core.components.SectionCard
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.ui.graphics.vector.ImageVector
import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR

internal class ThemeStep : OnboardingStep {

    override val isComplete: Boolean = true

    override val titleRes: StringResource = MR.strings.onboarding_theme_title

    override val subtitleRes: StringResource = MR.strings.onboarding_theme_subtitle

    override val icon: ImageVector = Icons.Outlined.Palette

    private val uiPreferences: UiPreferences = Injekt.get()

    @Composable
    override fun Content() {
        val themeModePref = uiPreferences.themeMode
        val themeMode by themeModePref.collectAsState()

        val appThemePref = uiPreferences.appTheme
        val appTheme by appThemePref.collectAsState()

        val amoledPref = uiPreferences.themeDarkAmoled
        val amoled by amoledPref.collectAsState()

        SectionCard {
            Column {
                AppThemeModePreferenceWidget(
                    value = themeMode,
                    onItemClick = {
                        themeModePref.set(it)
                        setAppCompatDelegateThemeMode(it)
                    },
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )

                AppThemePreferenceWidget(
                    value = appTheme,
                    amoled = amoled,
                    onItemClick = { appThemePref.set(it) },
                )
            }
        }
    }
}
