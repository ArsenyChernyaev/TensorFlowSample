package com.example.tensorflowsample.image.processing

import android.graphics.*
import com.example.tensorflowsample.Constants
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.graphics.ColorMatrixColorFilter
import android.graphics.ColorMatrix
import android.graphics.Bitmap

private val INVERT = ColorMatrix(
    floatArrayOf(
        -1f,  0f,  0f, 0f, 255f,
         0f, -1f,  0f, 0f, 255f,
         0f,  0f, -1f, 0f, 255f,
         0f,  0f,  0f, 1f,   0f)
)

private val BLACKWHITE = ColorMatrix(
    floatArrayOf(
        0.5f, 0.5f, 0.5f, 0f, 0f,
        0.5f, 0.5f, 0.5f, 0f, 0f,
        0.5f, 0.5f, 0.5f, 0f, 0f,
          0f,   0f,   0f, 1f, 0f
    )
)


fun prepareImageForClassification(bitmap: Bitmap): Bitmap {
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    colorMatrix.postConcat(BLACKWHITE)
    colorMatrix.postConcat(INVERT)
    val f = ColorMatrixColorFilter(colorMatrix)

    val paint = Paint()
    paint.colorFilter = f

    val bmpGrayscale = Bitmap.createScaledBitmap(
        bitmap,
        Constants.Model.INPUT_IMG_SIZE_WIDTH,
        Constants.Model.INPUT_IMG_SIZE_HEIGHT,
        false
    )
    val canvas = Canvas(bmpGrayscale)
    canvas.drawBitmap(bmpGrayscale, 0f, 0f, paint)
    return bmpGrayscale
}

fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    assert(bitmap.width == Constants.Model.INPUT_IMG_SIZE_WIDTH) { "Expected bitmap width: ${Constants.Model.INPUT_IMG_SIZE_WIDTH}, actual: ${bitmap.width}" }
    assert(bitmap.height == Constants.Model.INPUT_IMG_SIZE_HEIGHT) { "Expected bitmap width: ${Constants.Model.INPUT_IMG_SIZE_HEIGHT}, actual: ${bitmap.height}" }

    val byteBuffer = ByteBuffer.allocateDirect(Constants.Model.MODEL_INPUT_SIZE)
    byteBuffer.order(ByteOrder.nativeOrder())
    val pixels = IntArray(Constants.Model.INPUT_IMG_SIZE_WIDTH * Constants.Model.INPUT_IMG_SIZE_HEIGHT)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    for (pixel in pixels) {
        val rChannel = (pixel shr 16 and 0xFF).toFloat()
        val gChannel = (pixel shr 8 and 0xFF).toFloat()
        val bChannel = (pixel and 0xFF).toFloat()
        val pixelValue = (rChannel + gChannel + bChannel) / 3f / 255f
        byteBuffer.putFloat(pixelValue)
    }
    return byteBuffer
}