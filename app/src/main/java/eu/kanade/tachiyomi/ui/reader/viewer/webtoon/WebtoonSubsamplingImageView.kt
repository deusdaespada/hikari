package eu.kanade.tachiyomi.ui.reader.viewer.webtoon

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

/**
 * Implementation of subsampling scale image view that ignores all touch events, because the
 * webtoon viewer handles all the gestures.
 *
 * Calls requestLayout() once the image is ready so that the parent ReaderPageImageView can
 * re-measure itself with the correct image-scaled height, causing the RecyclerView item to
 * expand to the full height of the image strip.
 */
class WebtoonSubsamplingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SubsamplingScaleImageView(context, attrs) {

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onReady() {
        (parent as? android.view.View)?.requestLayout()
    }
}
