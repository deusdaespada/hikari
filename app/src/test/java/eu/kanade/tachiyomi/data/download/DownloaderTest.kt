package eu.kanade.tachiyomi.data.download

import android.content.Context
import android.content.SharedPreferences
import eu.kanade.tachiyomi.data.cache.ChapterCache
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.source.online.HttpSource
import io.kotest.assertions.nondeterministic.eventually
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.domain.chapter.interactor.GetChapter
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.download.service.DownloadPreferences
import tachiyomi.domain.download.service.DownloadQueueSortingMode
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.domain.track.interactor.GetTracks
import tachiyomi.core.common.preference.InMemoryPreferenceStore
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import kotlin.time.Duration.Companion.seconds

class DownloaderTest {

    private lateinit var context: Context
    private lateinit var provider: DownloadProvider
    private lateinit var cache: DownloadCache
    private lateinit var chapterCache: ChapterCache
    private lateinit var xml: XML
    private lateinit var getCategories: GetCategories
    private lateinit var getTracks: GetTracks

    private lateinit var preferenceStore: InMemoryPreferenceStore
    private lateinit var downloadPreferences: DownloadPreferences
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private val prefMap = mutableMapOf<String, String>()

    private val mockSource = mockk<HttpSource>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearMocks(sourceManager, getManga, getChapter)
        prefMap.clear()

        context = mockk(relaxed = true)
        provider = mockk(relaxed = true)
        cache = mockk(relaxed = true)
        chapterCache = mockk(relaxed = true)
        xml = mockk(relaxed = true)
        getCategories = mockk(relaxed = true)
        getTracks = mockk(relaxed = true)

        preferenceStore = InMemoryPreferenceStore()
        downloadPreferences = DownloadPreferences(preferenceStore)

        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        every { context.getSharedPreferences("active_downloads", any()) } returns mockPrefs
        every { mockPrefs.all } returns prefMap
        every { mockPrefs.edit() } returns mockEditor

        every { mockEditor.putString(any(), any()) } answers {
            val key = firstArg<String>()
            val value = secondArg<String>()
            prefMap[key] = value
            mockEditor
        }
        every { mockEditor.remove(any()) } answers {
            val key = firstArg<String>()
            prefMap.remove(key)
            mockEditor
        }
        every { mockEditor.clear() } answers {
            prefMap.clear()
            mockEditor
        }

        every { sourceManager.get(any()) } returns mockSource
        every { provider.findChapterDir(any(), any(), any(), any(), any()) } returns null
        every { provider.findChapterDirs(any(), any(), any()) } returns (null to emptyList())
    }

    private fun createDownloader(): Downloader {
        return Downloader(
            context = context,
            provider = provider,
            cache = cache,
            sourceManager = sourceManager,
            chapterCache = chapterCache,
            downloadPreferences = downloadPreferences,
            xml = xml,
            getCategories = getCategories,
            getTracks = getTracks,
        )
    }

    @Test
    fun testFIFOQueueSortingMode() = runBlocking {
        downloadPreferences.downloadQueueSortingMode.set(DownloadQueueSortingMode.FIFO)
        val downloader = createDownloader()

        val manga = Manga.create().copy(id = 1L, title = "Manga A", source = 100L)
        val chapter1 = Chapter.create().copy(id = 10L, mangaId = 1L, chapterNumber = 1.0, sourceOrder = 1L)
        val chapter2 = Chapter.create().copy(id = 20L, mangaId = 1L, chapterNumber = 2.0, sourceOrder = 2L)
        val chapter3 = Chapter.create().copy(id = 30L, mangaId = 1L, chapterNumber = 3.0, sourceOrder = 3L)

        // queueChapters filters out already enqueued, and sorts chaptersToQueue by descending sourceOrder before adding.
        // sourceOrder descending: chapter3 (3), chapter2 (2), chapter1 (1)
        downloader.queueChapters(manga, listOf(chapter1, chapter2, chapter3), autoStart = false)

        val queue = downloader.queueState.value
        assertEquals(3, queue.size)
        // FIFO order: first added is first. Since they were processed from descending sourceOrder,
        // chapter3 (sourceOrder 3) is first, then chapter2 (sourceOrder 2), then chapter1 (sourceOrder 1).
        assertEquals(30L, queue[0].chapter.id)
        assertEquals(20L, queue[1].chapter.id)
        assertEquals(10L, queue[2].chapter.id)
    }

    @Test
    fun testLIFOQueueSortingMode() = runBlocking {
        downloadPreferences.downloadQueueSortingMode.set(DownloadQueueSortingMode.LIFO)
        val downloader = createDownloader()

        val manga = Manga.create().copy(id = 1L, title = "Manga A", source = 100L)
        val chapter1 = Chapter.create().copy(id = 10L, mangaId = 1L, chapterNumber = 1.0, sourceOrder = 1L)
        val chapter2 = Chapter.create().copy(id = 20L, mangaId = 1L, chapterNumber = 2.0, sourceOrder = 2L)

        // Queue first batch
        downloader.queueChapters(manga, listOf(chapter1, chapter2), autoStart = false)
        var queue = downloader.queueState.value
        assertEquals(2, queue.size)
        assertEquals(20L, queue[0].chapter.id)
        assertEquals(10L, queue[1].chapter.id)

        // Queue new batch. In LIFO mode, new downloads are placed before existing pending downloads.
        val chapter3 = Chapter.create().copy(id = 30L, mangaId = 1L, chapterNumber = 3.0, sourceOrder = 3L)
        downloader.queueChapters(manga, listOf(chapter3), autoStart = false)

        queue = downloader.queueState.value
        assertEquals(3, queue.size)
        // Newly added chapter3 should be at the front
        assertEquals(30L, queue[0].chapter.id)
        assertEquals(20L, queue[1].chapter.id)
        assertEquals(10L, queue[2].chapter.id)
    }

    @Test
    fun testChapterAscendingSortingMode() = runBlocking {
        downloadPreferences.downloadQueueSortingMode.set(DownloadQueueSortingMode.CHAPTER_ASC)
        val downloader = createDownloader()

        val manga = Manga.create().copy(id = 1L, title = "Manga A", source = 100L)
        val chapter1 = Chapter.create().copy(id = 10L, mangaId = 1L, chapterNumber = 1.0, sourceOrder = 1L)
        val chapter2 = Chapter.create().copy(id = 20L, mangaId = 1L, chapterNumber = 2.0, sourceOrder = 2L)
        val chapter3 = Chapter.create().copy(id = 30L, mangaId = 1L, chapterNumber = 1.5, sourceOrder = 3L)

        downloader.queueChapters(manga, listOf(chapter1, chapter2, chapter3), autoStart = false)

        val queue = downloader.queueState.value
        assertEquals(3, queue.size)
        // CHAPTER_ASC: sorted by manga title, then chapterNumber ascending (1.0, 1.5, 2.0)
        assertEquals(10L, queue[0].chapter.id) // 1.0
        assertEquals(30L, queue[1].chapter.id) // 1.5
        assertEquals(20L, queue[2].chapter.id) // 2.0
    }

    @Test
    fun testChapterDescendingSortingMode() = runBlocking {
        downloadPreferences.downloadQueueSortingMode.set(DownloadQueueSortingMode.CHAPTER_DESC)
        val downloader = createDownloader()

        val manga = Manga.create().copy(id = 1L, title = "Manga A", source = 100L)
        val chapter1 = Chapter.create().copy(id = 10L, mangaId = 1L, chapterNumber = 1.0, sourceOrder = 1L)
        val chapter2 = Chapter.create().copy(id = 20L, mangaId = 1L, chapterNumber = 2.0, sourceOrder = 2L)
        val chapter3 = Chapter.create().copy(id = 30L, mangaId = 1L, chapterNumber = 1.5, sourceOrder = 3L)

        downloader.queueChapters(manga, listOf(chapter1, chapter2, chapter3), autoStart = false)

        val queue = downloader.queueState.value
        assertEquals(3, queue.size)
        // CHAPTER_DESC: sorted by manga title, then chapterNumber descending (2.0, 1.5, 1.0)
        assertEquals(20L, queue[0].chapter.id) // 2.0
        assertEquals(30L, queue[1].chapter.id) // 1.5
        assertEquals(10L, queue[2].chapter.id) // 1.0
    }

    @Test
    fun testDynamicSortingModeChange() = runBlocking {
        downloadPreferences.downloadQueueSortingMode.set(DownloadQueueSortingMode.FIFO)
        val downloader = createDownloader()

        val manga = Manga.create().copy(id = 1L, title = "Manga A", source = 100L)
        val chapter1 = Chapter.create().copy(id = 10L, mangaId = 1L, chapterNumber = 1.0, sourceOrder = 1L)
        val chapter2 = Chapter.create().copy(id = 20L, mangaId = 1L, chapterNumber = 2.0, sourceOrder = 2L)
        val chapter3 = Chapter.create().copy(id = 30L, mangaId = 1L, chapterNumber = 1.5, sourceOrder = 3L)

        downloader.queueChapters(manga, listOf(chapter1, chapter2, chapter3), autoStart = false)

        var queue = downloader.queueState.value
        assertEquals(30L, queue[0].chapter.id)
        assertEquals(20L, queue[1].chapter.id)
        assertEquals(10L, queue[2].chapter.id)

        // Change mode dynamically to CHAPTER_ASC
        downloadPreferences.downloadQueueSortingMode.set(DownloadQueueSortingMode.CHAPTER_ASC)

        eventually(2.seconds) {
            val q = downloader.queueState.value
            assertEquals(10L, q[0].chapter.id) // 1.0
            assertEquals(30L, q[1].chapter.id) // 1.5
            assertEquals(20L, q[2].chapter.id) // 2.0
        }
    }

    companion object {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        val mainThreadSurrogate = newSingleThreadContext("UI thread")

        val sourceManager = mockk<SourceManager>()
        val getManga = mockk<GetManga>()
        val getChapter = mockk<GetChapter>()
        val json = Json { ignoreUnknownKeys = true }

        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            Dispatchers.setMain(mainThreadSurrogate)

            mockkConstructor(DownloadNotifier::class)
            every { anyConstructed<DownloadNotifier>().dismissProgress() } just Runs
            every { anyConstructed<DownloadNotifier>().onProgressChange(any()) } just Runs
            every { anyConstructed<DownloadNotifier>().onPaused() } just Runs
            every { anyConstructed<DownloadNotifier>().onComplete() } just Runs
            every { anyConstructed<DownloadNotifier>().onWarning(any(), any(), any(), any()) } just Runs
            every { anyConstructed<DownloadNotifier>().onError(any(), any(), any(), any()) } just Runs

            Injekt.importModule(object : uy.kohesive.injekt.api.InjektModule {
                override fun uy.kohesive.injekt.api.InjektRegistrar.registerInjectables() {
                    addSingleton(sourceManager)
                    addSingleton(getManga)
                    addSingleton(getChapter)
                    addSingleton(json)
                }
            })
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            Dispatchers.resetMain()
            mainThreadSurrogate.close()
            unmockkConstructor(DownloadNotifier::class)
        }
    }
}
