package eu.kanade.tachiyomi.ui.reader.viewer.webtoon

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Item decoration that provides a seamless "stitched" experience for webtoons by
 * overlapping adjacent items by 1 pixel. This eliminates white lines (seams)
 * caused by sub-pixel rendering gaps when scaling the RecyclerView.
 */
class WebtoonSeamDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)

        // If it's not the first item, shift it up by 1 pixel to overlap with the previous item
        if (position > 0) {
            outRect.top = -1
        }
    }
}
