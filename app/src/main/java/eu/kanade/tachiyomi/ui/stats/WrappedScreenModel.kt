package eu.kanade.tachiyomi.ui.stats

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tachiyomi.domain.history.interactor.GetWrappedStats
import tachiyomi.domain.history.model.WrappedStats
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.Calendar

class WrappedScreenModel(
    private val getWrappedStats: GetWrappedStats = Injekt.get(),
) : StateScreenModel<WrappedScreenState>(WrappedScreenState.Loading) {

    init {
        loadStats()
    }

    private fun loadStats() {
        screenModelScope.launch {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val stats = getWrappedStats.await(currentYear)
            mutableState.update { WrappedScreenState.Success(stats) }
        }
    }
}

sealed interface WrappedScreenState {
    data object Loading : WrappedScreenState
    data class Success(val stats: WrappedStats) : WrappedScreenState
}
