package com.example.tensorflowsample.image.processing

import android.graphics.Bitmap
import com.example.tensorflowsample.Constants
import org.apache.commons.io.IOUtils
import org.tensorflow.lite.Interpreter
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


class DigitClassifier(modelStream: InputStream) {

    private val interpreter : Interpreter

    init {
        val byteArray = IOUtils.toByteArray(modelStream)
        val byteBuffer = ByteBuffer.allocateDirect(byteArray.size)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.put(byteArray)
        interpreter = Interpreter(byteBuffer)
    }

    fun recognizeImage(bitmap: Bitmap): ClassificationResult {
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val result = Array(1) { FloatArray(Constants.Model.OUTPUT_LABELS.size) }
        interpreter.run(byteBuffer, result)
        return getSortedResult(result)
    }

    private fun getSortedResult(resultsArray: Array<FloatArray>): ClassificationResult {

        var bestConfidence = 0.0f
        var bestLabel = Constants.Model.OUTPUT_LABELS.first()

        for (i in Constants.Model.OUTPUT_LABELS.indices) {
            val confidence = resultsArray[0][i]
            val label = Constants.Model.OUTPUT_LABELS[i]

            if (bestConfidence < confidence) {
                bestConfidence = confidence
                bestLabel = label
            }
        }
        return ClassificationResult(bestLabel, bestConfidence)
    }

    data class ClassificationResult(val label: String, val confidence: Float)
}