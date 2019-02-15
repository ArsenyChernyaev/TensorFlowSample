package com.example.tensorflowsample

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import com.example.tensorflowsample.image.processing.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val myPredictionReceiver = ServiceResultReceiver(Handler()) { status, data ->
        processPredictionCallback(status, data)
    }

    private val myUpdateReceiver = ServiceResultReceiver(Handler()) { status, data ->
        processUpdateModelCallback(status, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clearStatusFields()
        clearButton.setOnClickListener {
            paintView.clear()
        }
        classifyImageButton.setOnClickListener {
            val paintView = findViewById<PaintView>(R.id.paintView)
            val bitmap = paintView.getBitmap()
            setPreviewImage(bitmap)
            requestImageClassification(bitmap)
        }
        updateModelButton.setOnClickListener {
            requestUpdateModel()
        }
    }

    private fun requestImageClassification(bitmap: Bitmap) {
        val preparedImage = prepareImageBeforeSend(bitmap)
        val nextIntent = Intent(this, ImageProcessingService::class.java).apply {
            putExtra(Constants.IntentKeys.IMAGE_KEY, preparedImage)
            putExtra(Constants.IntentKeys.RECEIVER_KEY, myPredictionReceiver)
            type = RequestType.Classification.name
        }
        startService(nextIntent)
    }

    private fun requestUpdateModel() {
        val nextIntent = Intent(this, ImageProcessingService::class.java).apply {
            putExtra(Constants.IntentKeys.RECEIVER_KEY, myUpdateReceiver)
            type = RequestType.Update.name
        }
        startService(nextIntent)
    }

    private fun setPreviewImage(bitmap: Bitmap) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val size = displayMetrics.widthPixels
        val squareBitmap = ThumbnailUtils.extractThumbnail(bitmap, size, size)
        preview.setImageBitmap(squareBitmap)
    }

    private fun processPredictionCallback(resultStatus: WorkStatus, resultData: Bundle) {
        when (resultStatus) {
            WorkStatus.FINISHED -> {
                val prediction = resultData.getString(Constants.IntentKeys.PREDICTION_KEY) ?: return
                classificationResultView.text = getString(R.string.classification, prediction)
                val confidence = resultData.getFloat(Constants.IntentKeys.PREDICTION_CONFIDENCE_KEY)
                confidenceTextView.text = getString(R.string.confidence, confidence)
            }
            WorkStatus.ERROR -> {
                val error = resultData.getString(Constants.IntentKeys.BUNDLE_ERROR_KEY)
                Log.e(
                    Constants.Errors.IMAGE_PROCESSING_ERROR,
                    error ?: Constants.Errors.UNKNOWN_ERROR_MESSAGE
                )
            }
        }
    }

    private fun processUpdateModelCallback(resultStatus: WorkStatus, resultData: Bundle) {
        when (resultStatus) {
            WorkStatus.FINISHED -> {
                showNotification(resultData.getString(Constants.IntentKeys.UPDATE_MESSAGE_KEY) ?: "")
            }
            WorkStatus.ERROR -> {
                val error = resultData.getString(Constants.IntentKeys.BUNDLE_ERROR_KEY)
                Log.e(
                    "Image Processing Error",
                    error ?: "unknown error"
                )
            }
        }
    }

    private fun clearStatusFields() {
        classificationResultView.text = getString(R.string.classification, "")
        confidenceTextView.text = getString(R.string.confidence, 0.0f)
    }

    /**cast image to gray scale, revert color and change size
     * so we don't need to worry about image size
     * this could be done on Service side, but we need to save image on disk then
     * @param image image to prepare
     * **/
    private fun prepareImageBeforeSend(image: Bitmap): Bitmap {
        return prepareImageForClassification(image)
    }

    private fun showNotification(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
