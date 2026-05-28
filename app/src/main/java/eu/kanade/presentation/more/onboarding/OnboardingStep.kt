package eu.kanade.presentation.more.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.icerock.moko.resources.StringResource

internal interface OnboardingStep {

    val isComplete: Boolean

    val titleRes: StringResource

    val subtitleRes: StringResource

    val icon: ImageVector

    @Composable
    fun Content()
}
