package eu.kanade.tachiyomi.data.coil

import android.graphics.Bitmap
import android.os.Build
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.request.allowRgb565
import okio.BufferedSource
import tachiyomi.core.common.util.system.ImageUtil
import tachiyomi.core.common.util.system.NativeImageDecoder

/**
 * A Coil [Decoder] that leverages the custom native C++ [NativeImageDecoder] pipeline.
 * Features:
 * - High-performance decoding via AImageDecoder (API 30+)
 * - Hardware-accelerated upscaling via EASU (FSR 1.0)
 * - Reduced JVM heap pressure
 */
class HikariImageDecoder(
    private val resources: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null

        val bytes = resources.source().readByteArray()

        // Extract original dimensions
        val dimenOptions = ImageUtil.extractImageOptions(okio.Buffer().write(bytes))
        val srcWidth = dimenOptions.outWidth
        val srcHeight = dimenOptions.outHeight

        val dstWidth = options.size.widthPx(options.scale) { srcWidth }
        val dstHeight = options.size.heightPx(options.scale) { srcHeight }

        val sampleSize = DecodeUtils.calculateInSampleSize(
            srcWidth = srcWidth,
            srcHeight = srcHeight,
            dstWidth = dstWidth,
            dstHeight = dstHeight,
            scale = options.scale,
        )

        val isUpscaling = dstWidth > srcWidth / sampleSize || dstHeight > srcHeight / sampleSize
        var filters = 0
        if (isUpscaling) {
            filters = filters or NativeImageDecoder.FILTER_UPSCALING
        }

        val bitmap = Bitmap.createBitmap(
            dstWidth,
            dstHeight,
            if (options.allowRgb565) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888,
        )

        val success = NativeImageDecoder.decode(bitmap, bytes, filters)

        if (!success) {
            bitmap.recycle()
            return null
        }

        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = sampleSize > 1,
        )
    }

    class Factory : Decoder.Factory {
        override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader): Decoder? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null

            val source = result.source.source()
            if (!isSupported(source)) return null

            if (isLargeOrTall(source)) return null

            return HikariImageDecoder(result.source, options)
        }

        private fun isLargeOrTall(source: BufferedSource): Boolean {
            val options = ImageUtil.extractImageOptions(source.peek())
            val srcWidth = options.outWidth
            val srcHeight = options.outHeight

            val isExtremelyTall = srcHeight > srcWidth * 3
            val exceedsTextureLimit = srcHeight > ImageUtil.hardwareBitmapThreshold ||
                srcWidth > ImageUtil.hardwareBitmapThreshold

            return isExtremelyTall || exceedsTextureLimit
        }

        private fun isSupported(source: BufferedSource): Boolean {
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
