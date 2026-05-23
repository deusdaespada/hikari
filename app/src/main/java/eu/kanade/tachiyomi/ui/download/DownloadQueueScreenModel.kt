package eu.kanade.tachiyomi.ui.download

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.download.model.Download
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class DownloadQueueScreenModel(
    private val downloadManager: DownloadManager = Injekt.get(),
) : ScreenModel {

    private val _state = MutableStateFlow(emptyList<DownloadQueueSection>())
    val state = _state.asStateFlow()

    val isDownloaderRunning = downloadManager.isDownloaderRunning
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        screenModelScope.launch {
            downloadManager.queueState
                .map { downloads ->
                    downloads
                        .groupBy { it.source }
                        .map { (source, downloads) ->
                            DownloadQueueSection(
                                sourceId = source.id,
                                sourceName = source.name,
                                downloads = downloads,
                            )
                        }
                }
                .collect { sections -> _state.update { sections } }
        }
    }

    fun startDownloads() {
        downloadManager.startDownloads()
    }

    fun pauseDownloads() {
        downloadManager.pauseDownloads()
    }

    fun clearQueue() {
        downloadManager.clearQueue()
    }

    fun moveDownload(download: Download, toTop: Boolean) {
        val sections = state.value
        val section = sections.firstOrNull { group ->
            group.downloads.any { it.chapter.id == download.chapter.id }
        } ?: return
        val updatedSection = section.downloads.toMutableList()
        val itemIndex = updatedSection.indexOfFirst { it.chapter.id == download.chapter.id }
        if (itemIndex == -1) return
        val item = updatedSection.removeAt(itemIndex)
        if (toTop) {
            updatedSection.add(0, item)
        } else {
            updatedSection.add(item)
        }
        reorderQueueIfChanged(sections.flattenReplacing(section.sourceId, updatedSection))
    }

    fun moveSeries(download: Download, toTop: Boolean) {
        val sections = state.value
        val allDownloads = sections.flatMap { it.downloads }
        val (selectedSeries, otherSeries) = allDownloads.partition { it.manga.id == download.manga.id }
        reorderQueueIfChanged(
            if (toTop) {
                selectedSeries + otherSeries
            } else {
                otherSeries + selectedSeries
            },
        )
    }

    fun cancelDownload(download: Download) {
        downloadManager.cancelQueuedDownloads(listOf(download))
    }

    fun cancelSeries(download: Download) {
        val downloads = state.value
            .flatMap { it.downloads }
            .filter { it.manga.id == download.manga.id }
        if (downloads.isNotEmpty()) {
            downloadManager.cancelQueuedDownloads(downloads)
        }
    }

    fun <R : Comparable<R>> sortWithinSources(selector: (Download) -> R, reverse: Boolean = false) {
        val downloads = state.value.flatMap { section ->
            section.downloads.sortedBy(selector).let {
                if (reverse) it.asReversed() else it
            }
        }
        reorderQueueIfChanged(downloads)
    }

    fun moveWithinSource(fromChapterId: Long, toChapterId: Long) {
        val sections = state.value
        val section = sections.firstOrNull { group ->
            group.downloads.any { it.chapter.id == fromChapterId } &&
                group.downloads.any { it.chapter.id == toChapterId }
        } ?: return
        val fromIndex = section.downloads.indexOfFirst { it.chapter.id == fromChapterId }
        val toIndex = section.downloads.indexOfFirst { it.chapter.id == toChapterId }
        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) return

        val updatedSection = section.downloads.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        reorderQueueIfChanged(sections.flattenReplacing(section.sourceId, updatedSection))
    }

    private fun reorderQueueIfChanged(downloads: List<Download>) {
        val currentChapterIds = state.value.flatMap { section -> section.downloads.map { it.chapter.id } }
        val newChapterIds = downloads.map { it.chapter.id }
        if (currentChapterIds != newChapterIds) {
            downloadManager.reorderQueue(downloads)
        }
    }

    private fun List<DownloadQueueSection>.flattenReplacing(
        sourceId: Long,
        downloads: List<Download>,
    ): List<Download> {
        return flatMap { section ->
            if (section.sourceId == sourceId) downloads else section.downloads
        }
    }
}

data class DownloadQueueSection(
    val sourceId: Long,
    val sourceName: String,
    val downloads: List<Download>,
)
