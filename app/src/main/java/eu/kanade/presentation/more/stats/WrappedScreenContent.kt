package eu.kanade.presentation.more.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.toDurationString
import eu.kanade.tachiyomi.util.system.toShareIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.history.model.WrappedStats
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import java.io.File
import java.io.FileOutputStream
import kotlin.math.absoluteValue
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private object WrappedPalette {
    val Background = Color(0xFF0F0F0F)
    val Surface = Color(0xFF1A1A1A)
    val AccentIntro = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
    val AccentTime = listOf(Color(0xFFFF512F), Color(0xFFDD2476))
    val AccentChapters = listOf(Color(0xFF1D976C), Color(0xFF93F9B9))
    val AccentGenre = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
    val AccentManga = listOf(Color(0xFF000428), Color(0xFF004E92))
    val AccentSummary = listOf(Color(0xFF111111), Color(0xFF333333))
}

@Composable
fun WrappedScreenContent(
    stats: WrappedStats,
    onNavigateBack: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val SLIDE_DURATION = 5000L
    var isPaused by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(pagerState.currentPage, isPaused) {
        if (isPaused) return@LaunchedEffect

        val startTime = System.currentTimeMillis() - (currentProgress * SLIDE_DURATION).toLong()

        while (currentProgress < 1f) {
            if (isPaused) break
            val elapsed = System.currentTimeMillis() - startTime
            currentProgress = (elapsed.toFloat() / SLIDE_DURATION).coerceIn(0f, 1f)
            delay(16)
        }

        if (currentProgress >= 1f && !isPaused) {
            if (pagerState.currentPage < 5) {
                currentProgress = 0f
                pagerState.scrollToPage(pagerState.currentPage + 1)
            } else {
                onNavigateBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WrappedPalette.Background)
            .pointerInput(pagerState.currentPage) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val width = size.width
                        if (offset.x < width / 3) {
                            if (pagerState.currentPage > 0) {
                                currentProgress = 0f
                                scope.launch {
                                    pagerState.scrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        } else {
                            if (pagerState.currentPage < 5) {
                                currentProgress = 0f
                                scope.launch {
                                    pagerState.scrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onNavigateBack()
                            }
                        }
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    },
                )
            },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
        ) { page ->
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val scale = 1f + (pageOffset * 0.1f)
                        scaleX = scale
                        scaleY = scale
                        alpha = 1f - pageOffset.coerceIn(0f, 1f)
                    },
            ) {
                when (page) {
                    0 -> IntroSlide(stats.year)
                    1 -> TimeSlide(stats)
                    2 -> ChapterSlide(stats)
                    3 -> GenreSlide(stats)
                    4 -> TopMangaSlide(stats)
                    5 -> SummarySlide(stats, onNavigateBack)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(6) { index ->
                val isCurrent = pagerState.currentPage == index
                val isBefore = pagerState.currentPage > index

                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(1f)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(
                                when {
                                    isBefore -> 1f
                                    isCurrent -> currentProgress
                                    else -> 0f
                                },
                            )
                            .background(Color.White),
                    )
                }
            }
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun SlideContainer(
    gradient: List<Color>,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun IntroSlide(year: Int) {
    SlideContainer(WrappedPalette.AccentIntro) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 2 },
            ) {
                Text(
                    text = "Hikari",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Light,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000, 500)) + scaleIn(tween(1000, 500)),
            ) {
                Text(
                    text = "WRAPPED",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 8.sp,
                )
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000, 1000)),
            ) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.3f),
                )
            }
        }
    }
}

@Composable
private fun TimeSlide(stats: WrappedStats) {
    val context = LocalContext.current
    val durationStr = remember(stats.totalReadDuration) {
        stats.totalReadDuration.toDuration(DurationUnit.MILLISECONDS)
            .toDurationString(context, fallback = "0h")
    }
    SlideContainer(WrappedPalette.AccentTime) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "You spent",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Text(
                text = durationStr,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "immersed in new worlds.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ChapterSlide(stats: WrappedStats) {
    SlideContainer(WrappedPalette.AccentChapters) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "You devoured",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black.copy(alpha = 0.7f),
            )
            Text(
                text = stats.totalChaptersRead.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 100.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
            )
            Text(
                text = "chapters",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "That's quite a marathon!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun GenreSlide(stats: WrappedStats) {
    val topGenre = stats.topGenres.firstOrNull()?.first ?: "Manga"
    SlideContainer(WrappedPalette.AccentGenre) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(
                text = "Your reading soul is",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f),
            )
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 40.dp, vertical = 20.dp),
            ) {
                Text(
                    text = topGenre,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
            if (stats.topGenres.size > 1) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    stats.topGenres.drop(1).take(3).forEach { (genre, _) ->
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopMangaSlide(stats: WrappedStats) {
    SlideContainer(WrappedPalette.AccentManga) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Your Top Titles",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                stats.topManga.take(3).forEachIndexed { index, (manga, _) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                        MangaCover.Book(
                            data = manga,
                            modifier = Modifier.size(60.dp, 90.dp),
                        )
                        Text(
                            text = manga.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 2,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySlide(
    stats: WrappedStats,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current

    SlideContainer(WrappedPalette.AccentSummary) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(32.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "HIKARI WRAPPED",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp,
                        )
                        Text(
                            stats.year.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        val durationStr = stats.totalReadDuration.toDuration(DurationUnit.MILLISECONDS)
                            .toDurationString(context, fallback = "0h")

                        SummaryItem("TIME", durationStr)
                        SummaryItem("CHAPTERS", stats.totalChaptersRead.toString())
                        SummaryItem("GENRE", stats.topGenres.firstOrNull()?.first ?: "Manga")
                        SummaryItem("STREAK", "${stats.streak} days")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val bitmap = view.drawToBitmap()
                    val cachePath = File(context.cacheDir, "images")
                    cachePath.mkdirs()
                    val file = File(cachePath, "hikari_wrapped.png")
                    val stream = FileOutputStream(file)
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()

                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val intent = uri.toShareIntent(context, message = context.stringResource(MR.strings.label_wrapped))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(0.6f),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    stringResource(MR.strings.wrapped_share),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
