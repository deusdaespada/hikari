package tachiyomi.core.common.util.system

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeImageDecoderTest {

    private fun createTestJpeg(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.RED)
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val bytes = stream.toByteArray()
        bitmap.recycle()
        return bytes
    }

    @Test
    fun testDecodeARGB8888() {
        val jpegBytes = createTestJpeg(100, 100)
        val targetBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val success = NativeImageDecoder.decode(targetBitmap, jpegBytes)
        assertTrue(success)
        assertEquals(android.graphics.Color.RED, targetBitmap.getPixel(50, 50))
        targetBitmap.recycle()
    }

    @Test
    fun testDecodeRGB565() {
        val jpegBytes = createTestJpeg(100, 100)
        val targetBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
        val success = NativeImageDecoder.decode(targetBitmap, jpegBytes)
        assertTrue(success)
        val pixel = targetBitmap.getPixel(50, 50)
        assertTrue(android.graphics.Color.red(pixel) > 240)
        assertTrue(android.graphics.Color.green(pixel) < 15)
        assertTrue(android.graphics.Color.blue(pixel) < 15)
        targetBitmap.recycle()
    }

    @Test
    fun testDecodeRGB565WithFiltersFails() {
        val jpegBytes = createTestJpeg(100, 100)
        val targetBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
        val success = NativeImageDecoder.decode(targetBitmap, jpegBytes, NativeImageDecoder.FILTER_SHARPEN)
        assertFalse(success)
        targetBitmap.recycle()
    }

    @Test
    fun testFiltersModifyPixels() {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLUE
            strokeWidth = 10f
        }
        val base = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(base)
        canvas.drawColor(android.graphics.Color.RED)
        canvas.drawLine(0f, 0f, 100f, 100f, paint)
        
        val stream = java.io.ByteArrayOutputStream()
        base.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val patternJpeg = stream.toByteArray()
        base.recycle()

        val normalBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val filteredBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        assertTrue(NativeImageDecoder.decode(normalBitmap, patternJpeg))
        assertTrue(NativeImageDecoder.decode(filteredBitmap, patternJpeg, NativeImageDecoder.FILTER_SHARPEN))

        var differenceCount = 0
        for (y in 0 until 100) {
            for (x in 0 until 100) {
                if (normalBitmap.getPixel(x, y) != filteredBitmap.getPixel(x, y)) {
                    differenceCount++
                }
            }
        }
        assertTrue(differenceCount > 0)
        normalBitmap.recycle()
        filteredBitmap.recycle()
    }

    @Test
    fun testProcessFilters() {
        val baseBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(baseBitmap)
        canvas.drawColor(android.graphics.Color.RED)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLUE
            strokeWidth = 10f
        }
        canvas.drawLine(0f, 0f, 100f, 100f, paint)

        val processedBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val success = NativeImageDecoder.process(processedBitmap, NativeImageDecoder.FILTER_SHARPEN)
        assertTrue(success)

        var differenceCount = 0
        for (y in 0 until 100) {
            for (x in 0 until 100) {
                if (baseBitmap.getPixel(x, y) != processedBitmap.getPixel(x, y)) {
                    differenceCount++
                }
            }
        }
        assertTrue(differenceCount > 0)
        baseBitmap.recycle()
        processedBitmap.recycle()
    }
}
