package eu.kanade.tachiyomi.data.coil

import android.graphics.BitmapFactory
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.request.allowRgb565
import coil3.request.bitmapConfig
import okio.BufferedSource
import tachiyomi.core.common.util.system.ImageUtil
import tachiyomi.core.common.util.system.logcat

/**
 * A Coil [Decoder] specifically tuned for very tall webtoon-style images.
 * It uses aggressive inSampleSize to prevent OOM and reduce memory pressure
 * for images that exceed typical texture limits.
 */
class WebtoonImageDecoder(private val source: BufferedSource, private val options: Options) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val decodeOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        // Peek to get dimensions without consuming the source
        source.peek().inputStream().use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        }

        val srcWidth = decodeOptions.outWidth
        val srcHeight = decodeOptions.outHeight

        if (srcWidth <= 0 || srcHeight <= 0) return null

        val isExtremelyTall = srcHeight > srcWidth * 3
        val exceedsTextureLimit =
            srcHeight > ImageUtil.hardwareBitmapThreshold || srcWidth > ImageUtil.hardwareBitmapThreshold

        if (!isExtremelyTall && !exceedsTextureLimit) return null

        logcat { "Tuning Webtoon image: ${srcWidth}x${srcHeight}" }

        var inSampleSize = 1
        val maxDimension = maxOf(srcWidth, srcHeight)
        val targetLimit = ImageUtil.hardwareBitmapThreshold.coerceAtLeast(2048)

        while (maxDimension / inSampleSize > targetLimit) {
            inSampleSize *= 2
        }

        if (options.allowRgb565 && isExtremelyTall && inSampleSize == 1) {
            inSampleSize = 2
        }

        decodeOptions.inJustDecodeBounds = false
        decodeOptions.inSampleSize = inSampleSize
        decodeOptions.inPreferredConfig = options.bitmapConfig

        val bitmap = source.inputStream().use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: throw IllegalStateException("Failed to decode extremely tall image.")

        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = inSampleSize > 1,
        )
    }

    class Factory : Decoder.Factory {
        override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader): Decoder? {
            // Only apply this to standard raster formats, AVIF/JXL handled by TachiyomiImageDecoder
            val source = result.source.source()
            return if (isWebtoonApplicable(source)) {
                WebtoonImageDecoder(source, options)
            } else {
                null
            }
        }

        private fun isWebtoonApplicable(source: BufferedSource): Boolean {
            val type = source.peek().inputStream().use {
                ImageUtil.findImageType(it)
            }
            return when (type) {
                ImageUtil.ImageType.JPEG, ImageUtil.ImageType.PNG, ImageUtil.ImageType.WEBP -> true
                else -> false
            }
        }
    }
}
