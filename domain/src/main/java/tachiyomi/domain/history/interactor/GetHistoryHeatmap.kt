package tachiyomi.domain.history.interactor

import kotlinx.coroutines.flow.first
import tachiyomi.domain.history.repository.HistoryRepository
import java.util.Calendar

class GetHistoryHeatmap(
    private val repository: HistoryRepository,
) {

    suspend fun await(): Map<Long, Int> {
        val history = repository.getHistory("").first()
        val calendar = Calendar.getInstance()

        val oneYearAgo = calendar.apply {
            add(Calendar.YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return history
            .filter { (it.readAt?.time ?: 0) >= oneYearAgo }
            .groupBy {
                calendar.time = it.readAt!!
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            .mapValues { it.value.size }
    }
}
