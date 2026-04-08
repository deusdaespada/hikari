package eu.kanade.tachiyomi.ui.reader.viewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Point
import android.graphics.Rect
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import com.davemorrissey.labs.subscaleview.provider.InputProvider

/**
 * A custom ImageRegionDecoder for SubsamplingScaleImageView that uses our
 * high-performance NativeImageDecoder pipeline.
 *
 * Moved to app module to satisfy SubsamplingScaleImageView dependency.
 */
class NativeImageRegionDecoder : ImageRegionDecoder {

    @Volatile
    private var decoder: BitmapRegionDecoder? = null
    private var imageWidth = 0
    private var imageHeight = 0

    override fun init(context: Context, provider: InputProvider): Point {
        provider.openStream().use { stream ->
            val inputStream = checkNotNull(stream) { "Failed to open image stream" }
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, this)
                imageWidth = outWidth
                imageHeight = outHeight
            }
        }

        provider.openStream().use { stream ->
            val inputStream = checkNotNull(stream) { "Failed to open image stream" }
            decoder = checkNotNull(BitmapRegionDecoder.newInstance(inputStream, false)) {
                "Failed to create BitmapRegionDecoder"
            }
        }

        return Point(imageWidth, imageHeight)
    }

    override fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return checkNotNull(decoder) { "Decoder not initialized" }
            .decodeRegion(sRect, options)
    }

    override fun isReady(): Boolean = decoder?.isRecycled == false

    override fun recycle() {
        decoder?.recycle()
        decoder = null
    }
}
