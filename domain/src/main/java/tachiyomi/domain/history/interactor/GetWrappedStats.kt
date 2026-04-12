package tachiyomi.domain.history.interactor

import kotlinx.coroutines.flow.first
import tachiyomi.domain.history.model.WrappedStats
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.manga.repository.MangaRepository
import java.util.Calendar

class GetWrappedStats(
    private val historyRepository: HistoryRepository,
    private val mangaRepository: MangaRepository,
) {

    suspend fun await(year: Int): WrappedStats {
        val allHistory = historyRepository.getHistory("").first()
        val calendar = Calendar.getInstance()

        val yearlyHistory = allHistory.filter {
            val readAt = it.readAt ?: return@filter false
            calendar.time = readAt
            calendar.get(Calendar.YEAR) == year
        }

        if (yearlyHistory.isEmpty()) {
            return WrappedStats(
                year = year,
                totalReadDuration = 0,
                totalChaptersRead = 0,
                topManga = emptyList(),
                topGenres = emptyList(),
                topAuthor = null,
                busiestMonth = 0,
                busiestDay = 0,
                streak = 0,
            )
        }

        val totalReadDuration = yearlyHistory.sumOf { it.readDuration }
        val totalChaptersRead = yearlyHistory.size

        val mangaDurations = yearlyHistory.groupBy { it.mangaId }
            .mapValues { (_, history) -> history.sumOf { it.readDuration } }

        val topMangaIds = mangaDurations.entries.sortedByDescending { it.value }.take(10)
        val mangaList = mangaRepository.getMangaByIds(topMangaIds.map { it.key })
        val topManga = topMangaIds.mapNotNull { entry ->
            val manga = mangaList.find { it.id == entry.key } ?: return@mapNotNull null
            manga to entry.value
        }

        val allMangaIds = yearlyHistory.map { it.mangaId }.distinct()
        val allMangaList = mangaRepository.getMangaByIds(allMangaIds)

        val genres = mutableMapOf<String, Int>()
        val authors = mutableMapOf<String, Int>()

        yearlyHistory.forEach { history ->
            val manga = allMangaList.find { it.id == history.mangaId } ?: return@forEach
            manga.genre?.forEach { genre ->
                genres[genre] = genres.getOrDefault(genre, 0) + 1
            }
            manga.author?.let { author ->
                authors[author] = authors.getOrDefault(author, 0) + 1
            }
        }

        val topGenres = genres.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }
        val topAuthor = authors.entries.maxByOrNull { it.value }?.key

        val monthCounts = IntArray(12)
        val dayCounts = IntArray(7)
        yearlyHistory.forEach {
            calendar.time = it.readAt!!
            monthCounts[calendar.get(Calendar.MONTH)]++
            dayCounts[calendar.get(Calendar.DAY_OF_WEEK) - 1]++
        }

        val busiestMonth = monthCounts.indices.maxBy { monthCounts[it] }
        val busiestDay = dayCounts.indices.maxBy { dayCounts[it] }

        val readDates = yearlyHistory.mapNotNull { it.readAt }
            .map {
                calendar.time = it
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            .distinct()
            .sorted()

        var currentStreak = 0
        var maxStreak = 0
        var lastDate = -1L

        readDates.forEach { date ->
            if (lastDate == -1L) {
                currentStreak = 1
            } else {
                val diff = date - lastDate
                if (diff <= 86400000L + 3600000L) {
                    currentStreak++
                } else {
                    currentStreak = 1
                }
            }
            maxStreak = maxOf(maxStreak, currentStreak)
            lastDate = date
        }

        return WrappedStats(
            year = year,
            totalReadDuration = totalReadDuration,
            totalChaptersRead = totalChaptersRead,
            topManga = topManga,
            topGenres = topGenres,
            topAuthor = topAuthor,
            busiestMonth = busiestMonth,
            busiestDay = busiestDay,
            streak = maxStreak,
        )
    }
}
