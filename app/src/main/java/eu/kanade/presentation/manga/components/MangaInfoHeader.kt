package eu.kanade.presentation.manga.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.util.mangaSharedElement
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.system.copyToClipboard
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.findChildOfType
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HikariCard
import tachiyomi.presentation.core.components.HikariCardDefaults
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.clickableNoIndication
import tachiyomi.presentation.core.util.secondaryItemAlpha
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun MangaInfoBox(
    isTabletUi: Boolean,
    appBarPadding: Dp,
    manga: Manga,
    sourceName: String,
    isStubSource: Boolean,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    sharedElementTag: String = "cover",
) {
    Box(modifier = modifier) {
        val backdropGradientColors = listOf(
            Color.Transparent,
            MaterialTheme.colorScheme.background,
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(manga)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(colors = backdropGradientColors),
                    )
                }
                .blur(16.dp)
                .alpha(0.35f),
        )

        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            if (!isTabletUi) {
                MangaAndSourceTitlesSmall(
                    appBarPadding = appBarPadding,
                    manga = manga,
                    sourceName = sourceName,
                    isStubSource = isStubSource,
                    onCoverClick = onCoverClick,
                    doSearch = doSearch,
                    sharedElementTag = sharedElementTag,
                )
            } else {
                MangaAndSourceTitlesLarge(
                    appBarPadding = appBarPadding,
                    manga = manga,
                    sourceName = sourceName,
                    isStubSource = isStubSource,
                    onCoverClick = onCoverClick,
                    doSearch = doSearch,
                    sharedElementTag = sharedElementTag,
                )
            }
        }
    }
}

@Composable
fun MangaActionRow(
    favorite: Boolean,
    trackingCount: Int,
    isUserIntervalMode: Boolean,
    onAddToLibraryClicked: () -> Unit,
    onWebViewClicked: (() -> Unit)?,
    onWebViewLongClicked: (() -> Unit)?,
    onTrackingClicked: () -> Unit,
    onEditIntervalClicked: (() -> Unit)?,
    onEditCategory: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val defaultColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)

    HikariCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            MangaActionButton(
                title = if (favorite) {
                    stringResource(MR.strings.in_library)
                } else {
                    stringResource(MR.strings.add_to_library)
                },
                icon = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                color = if (favorite) MaterialTheme.colorScheme.primary else defaultColor,
                onClick = onAddToLibraryClicked,
                onLongClick = onEditCategory,
            )
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = MaterialTheme.padding.small),
                thickness = 0.5.dp,
                color = HikariCardDefaults.dividerColor(),
            )
            MangaActionButton(
                title = stringResource(MR.strings.pref_library_update_smart_update),
                icon = Icons.Default.HourglassEmpty,
                color = if (isUserIntervalMode) MaterialTheme.colorScheme.primary else defaultColor,
                onClick = { onEditIntervalClicked?.invoke() },
            )
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = MaterialTheme.padding.small),
                thickness = 0.5.dp,
                color = HikariCardDefaults.dividerColor(),
            )
            MangaActionButton(
                title = if (trackingCount == 0) {
                    stringResource(MR.strings.manga_tracking_tab)
                } else {
                    pluralStringResource(MR.plurals.num_trackers, count = trackingCount, trackingCount)
                },
                icon = if (trackingCount == 0) Icons.Outlined.Sync else Icons.Outlined.Done,
                color = if (trackingCount == 0) defaultColor else MaterialTheme.colorScheme.primary,
                onClick = onTrackingClicked,
            )
            if (onWebViewClicked != null) {
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = MaterialTheme.padding.small),
                    thickness = 0.5.dp,
                    color = HikariCardDefaults.dividerColor(),
                )
                MangaActionButton(
                    title = stringResource(MR.strings.action_web_view),
                    icon = Icons.Outlined.Public,
                    color = defaultColor,
                    onClick = onWebViewClicked,
                    onLongClick = onWebViewLongClicked,
                )
            }
        }
    }
}

@Composable
fun ExpandableMangaDescription(
    defaultExpandState: Boolean,
    description: String?,
    notes: String,
    onEditNotes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (expanded, onExpanded) = rememberSaveable {
        mutableStateOf(defaultExpandState)
    }
    val desc = description.takeIf { !it.isNullOrBlank() } ?: stringResource(MR.strings.description_placeholder)

    val preferences = remember { Injekt.get<UiPreferences>() }
    val loadImages = remember { preferences.imagesInDescription.get() }

    HikariCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickableNoIndication { onExpanded(!expanded) },
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.padding.medium)
                .animateContentSize(),
        ) {
            MangaNotesSection(
                content = notes,
                expanded = expanded,
                onEditNotes = onEditNotes,
            )
            SelectionContainer {
                MarkdownRender(
                    content = desc,
                    modifier = Modifier
                        .secondaryItemAlpha()
                        .then(
                            if (!expanded) {
                                Modifier.height(72.dp).clipToBounds()
                            } else {
                                Modifier
                            },
                        ),
                    annotator = descriptionAnnotator(
                        loadImages = loadImages,
                        linkStyle = getMarkdownLinkStyle().toSpanStyle(),
                    ),
                    loadImages = loadImages,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_caret_down)
            Icon(
                painter = rememberAnimatedVectorPainter(image, !expanded),
                contentDescription = stringResource(
                    if (expanded) MR.strings.manga_info_collapse else MR.strings.manga_info_expand,
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 2.dp),
            )
        }
    }
}

@Composable
fun MangaGenreSection(
    tagsProvider: () -> List<String>?,
    onTagSearch: (String) -> Unit,
    onCopyTagToClipboard: (tag: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tags = tagsProvider()
    if (tags.isNullOrEmpty()) return

    var showMenu by remember { mutableStateOf(false) }
    var tagSelected by remember { mutableStateOf("") }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(MR.strings.action_search)) },
            onClick = {
                onTagSearch(tagSelected)
                showMenu = false
            },
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(MR.strings.action_copy_to_clipboard)) },
            onClick = {
                onCopyTagToClipboard(tagSelected)
                showMenu = false
            },
        )
    }

    FlowRow(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            TagsChip(
                text = tag,
                onClick = {
                    tagSelected = tag
                    showMenu = true
                },
            )
        }
    }
}

@Composable
private fun MangaAndSourceTitlesLarge(
    appBarPadding: Dp,
    manga: Manga,
    sourceName: String,
    isStubSource: Boolean,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
    sharedElementTag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = appBarPadding + 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MangaCover.Book(
            modifier = Modifier.fillMaxWidth(0.65f),
            data = ImageRequest.Builder(LocalContext.current)
                .data(manga)
                .crossfade(true)
                .build(),
            mangaId = manga.id,
            contentDescription = stringResource(MR.strings.manga_cover),
            onClick = onCoverClick,
            tag = sharedElementTag,
        )
        Spacer(modifier = Modifier.height(16.dp))
        MangaTitle(
            title = manga.title,
            mangaId = manga.id,
            doSearch = doSearch,
            sharedElementTag = sharedElementTag,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        MangaMetadataRows(
            author = manga.author,
            artist = manga.artist,
            status = manga.status,
            sourceName = sourceName,
            isStubSource = isStubSource,
            doSearch = doSearch,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MangaAndSourceTitlesSmall(
    appBarPadding: Dp,
    manga: Manga,
    sourceName: String,
    isStubSource: Boolean,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
    sharedElementTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = appBarPadding + 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangaCover.Book(
            modifier = Modifier
                .sizeIn(maxWidth = 100.dp)
                .align(Alignment.Top),
            data = ImageRequest.Builder(LocalContext.current)
                .data(manga)
                .crossfade(true)
                .build(),
            mangaId = manga.id,
            contentDescription = stringResource(MR.strings.manga_cover),
            onClick = onCoverClick,
            tag = sharedElementTag,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            MangaTitle(
                title = manga.title,
                mangaId = manga.id,
                doSearch = doSearch,
                sharedElementTag = sharedElementTag,
            )
            Spacer(modifier = Modifier.height(2.dp))
            MangaMetadataRows(
                author = manga.author,
                artist = manga.artist,
                status = manga.status,
                sourceName = sourceName,
                isStubSource = isStubSource,
                doSearch = doSearch,
            )
        }
    }
}

@Composable
private fun MangaTitle(
    title: String,
    mangaId: Long,
    doSearch: (query: String, global: Boolean) -> Unit,
    sharedElementTag: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = LocalTextStyle.current.textAlign,
) {
    val context = LocalContext.current
    androidx.compose.animation.AnimatedVisibility(
        visible = true,
        enter = androidx.compose.animation.fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        modifier = modifier,
    ) {
        Text(
            text = title.ifBlank { stringResource(MR.strings.unknown_title) },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .mangaSharedElement("$sharedElementTag-title", mangaId)
                .clickableNoIndication(
                    onLongClick = {
                        if (title.isNotBlank()) {
                            context.copyToClipboard(title, title)
                        }
                    },
                    onClick = { if (title.isNotBlank()) doSearch(title, true) },
                ),
            textAlign = textAlign,
        )
    }
}

@Composable
private fun MangaMetadataRows(
    author: String?,
    artist: String?,
    status: Long,
    sourceName: String,
    isStubSource: Boolean,
    doSearch: (query: String, global: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = LocalTextStyle.current.textAlign,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            enter = androidx.compose.animation.fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                initialAlpha = 0f,
            ),
        ) {
            Row(
                modifier = Modifier.secondaryItemAlpha(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = author?.takeIf { it.isNotBlank() }
                        ?: stringResource(MR.strings.unknown_author),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickableNoIndication(
                        onLongClick = {
                            if (!author.isNullOrBlank()) {
                                context.copyToClipboard(author, author)
                            }
                        },
                        onClick = { if (!author.isNullOrBlank()) doSearch(author, true) },
                    ),
                    textAlign = textAlign,
                )
            }
        }

        if (!artist.isNullOrBlank() && author != artist) {
            Row(
                modifier = Modifier.secondaryItemAlpha(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Brush,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickableNoIndication(
                        onLongClick = { context.copyToClipboard(artist, artist) },
                        onClick = { doSearch(artist, true) },
                    ),
                    textAlign = textAlign,
                )
            }
        }

        Row(
            modifier = Modifier.secondaryItemAlpha(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val statusIcon = when (status) {
                SManga.ONGOING.toLong() -> Icons.Outlined.Schedule
                SManga.COMPLETED.toLong() -> Icons.Outlined.DoneAll
                SManga.LICENSED.toLong() -> Icons.Outlined.AttachMoney
                SManga.PUBLISHING_FINISHED.toLong() -> Icons.Outlined.Done
                SManga.CANCELLED.toLong() -> Icons.Outlined.Close
                SManga.ON_HIATUS.toLong() -> Icons.Outlined.Pause
                else -> Icons.Outlined.Block
            }
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = when (status) {
                    SManga.ONGOING.toLong() -> stringResource(MR.strings.ongoing)
                    SManga.COMPLETED.toLong() -> stringResource(MR.strings.completed)
                    SManga.LICENSED.toLong() -> stringResource(MR.strings.licensed)
                    SManga.PUBLISHING_FINISHED.toLong() -> stringResource(MR.strings.publishing_finished)
                    SManga.CANCELLED.toLong() -> stringResource(MR.strings.cancelled)
                    SManga.ON_HIATUS.toLong() -> stringResource(MR.strings.on_hiatus)
                    else -> stringResource(MR.strings.unknown)
                },
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                textAlign = textAlign,
            )
        }

        Row(
            modifier = Modifier.secondaryItemAlpha(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isStubSource) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Public,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = sourceName,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.clickableNoIndication {
                    doSearch(sourceName, false)
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                textAlign = textAlign,
            )
        }
    }
}

@Composable
private fun descriptionAnnotator(loadImages: Boolean, linkStyle: SpanStyle) = remember(loadImages, linkStyle) {
    markdownAnnotator(
        annotate = { content, child ->
            if (!loadImages && child.type == MarkdownElementTypes.IMAGE) {
                val inlineLink = child.findChildOfType(MarkdownElementTypes.INLINE_LINK)

                val url = inlineLink?.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
                    ?.getUnescapedTextInNode(content)
                    ?: inlineLink?.findChildOfType(MarkdownElementTypes.AUTOLINK)
                        ?.findChildOfType(MarkdownTokenTypes.AUTOLINK)
                        ?.getUnescapedTextInNode(content)
                    ?: return@markdownAnnotator false

                val textNode = inlineLink?.findChildOfType(MarkdownElementTypes.LINK_TITLE)
                    ?: inlineLink?.findChildOfType(MarkdownElementTypes.LINK_TEXT)
                val altText = textNode?.findChildOfType(MarkdownTokenTypes.TEXT)
                    ?.getUnescapedTextInNode(content).orEmpty()

                withLink(LinkAnnotation.Url(url = url)) {
                    pushStyle(linkStyle)
                    appendInlineContent(MARKDOWN_INLINE_IMAGE_TAG)
                    append(altText)
                    pop()
                }

                return@markdownAnnotator true
            }

            if (child.type in DISALLOWED_MARKDOWN_TYPES) {
                append(content.substring(child.startOffset, child.endOffset))
                return@markdownAnnotator true
            }

            false
        },
        config = markdownAnnotatorConfig(
            eolAsNewLine = true,
        ),
    )
}

private val DefaultTagChipModifier = Modifier.padding(vertical = 4.dp)

@Composable
private fun TagsChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        SuggestionChip(
            modifier = modifier.then(DefaultTagChipModifier),
            onClick = onClick,
            label = { Text(text = text, style = MaterialTheme.typography.bodySmall) },
        )
    }
}

@Composable
private fun RowScope.MangaActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
