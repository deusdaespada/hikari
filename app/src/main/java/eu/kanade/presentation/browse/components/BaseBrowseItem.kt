package eu.kanade.presentation.browse.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.kanade.presentation.history.components.ItemPosition
import tachiyomi.presentation.core.components.HikariGroupedListItem
import tachiyomi.presentation.core.components.HikariListItemPosition
import tachiyomi.presentation.core.components.material.padding

@Composable
fun BaseBrowseItem(
    modifier: Modifier = Modifier,
    position: ItemPosition? = null,
    onClickItem: () -> Unit = {},
    onLongClickItem: () -> Unit = {},
    icon: @Composable RowScope.() -> Unit = {},
    action: @Composable RowScope.() -> Unit = {},
    content: @Composable RowScope.() -> Unit = {},
) {
    if (position == null) {
        Row(
            modifier = modifier
                .combinedClickable(
                    onClick = onClickItem,
                    onLongClick = onLongClickItem,
                )
                .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            content()
            action()
        }
    } else {
        HikariGroupedListItem(
            modifier = modifier,
            position = position.toHikariListItemPosition(),
            onClick = onClickItem,
            onLongClick = onLongClickItem,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon()
                content()
                action()
            }
        }
    }
}

private fun ItemPosition.toHikariListItemPosition(): HikariListItemPosition {
    return when (this) {
        ItemPosition.First -> HikariListItemPosition.First
        ItemPosition.Middle -> HikariListItemPosition.Middle
        ItemPosition.Last -> HikariListItemPosition.Last
        ItemPosition.Single -> HikariListItemPosition.Single
    }
}
