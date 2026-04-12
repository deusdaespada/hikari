package tachiyomi.domain.history.model

import tachiyomi.domain.manga.model.Manga

data class WrappedStats(
    val year: Int,
    val totalReadDuration: Long,
    val totalChaptersRead: Int,
    val topManga: List<Pair<Manga, Long>>,
    val topGenres: List<Pair<String, Int>>,
    val topAuthor: String?,
    val busiestMonth: Int, // 0-11
    val busiestDay: Int, // 1-7 (or 0-6)
    val streak: Int,
)
