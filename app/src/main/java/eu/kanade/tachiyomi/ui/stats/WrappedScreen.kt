package eu.kanade.tachiyomi.ui.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.more.stats.WrappedScreenContent
import eu.kanade.presentation.util.Screen
import tachiyomi.presentation.core.screens.LoadingScreen

class WrappedScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { WrappedScreenModel() }
        val state by screenModel.state.collectAsState()

        if (state is WrappedScreenState.Loading) {
            LoadingScreen()
            return
        }

        val successState = state as WrappedScreenState.Success
        WrappedScreenContent(
            stats = successState.stats,
            onNavigateBack = navigator::pop,
        )
    }
}
