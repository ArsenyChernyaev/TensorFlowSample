package com.example.tensorflowsample.image.processing

import android.app.IntentService
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import com.example.tensorflowsample.Constants
import com.example.tensorflowsample.R
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream


class ImageProcessingService : IntentService(ImageProcessingService::class.java.name) {

    private lateinit var myClassifier: DigitClassifier
    private var myVersion: Int = 0
    private lateinit var myModelFile: File
    private lateinit var myVersionFile: File

    override fun onCreate() {
        super.onCreate()
        myModelFile = moveResourceToExternal(R.raw.mnist_model)
        myVersionFile = moveResourceToExternal(R.raw.model_info)
        myVersion = JSONObject(myVersionFile.readText()).getInt("version")
        myClassifier = DigitClassifier(myModelFile.inputStream())
    }

    override fun onHandleIntent(intent: Intent) {

        val receiver = intent.extras?.get(Constants.IntentKeys.RECEIVER_KEY) as? ResultReceiver
        if (receiver == null) {
            Log.e(Constants.Errors.IMAGE_PROCESSING_ERROR, "Request doesn't contain result receiver")
            return
        }

        val requestTypeString = intent.type
        if (requestTypeString == null) {
            responseWithError(receiver, "Intent type should be declared")
            return
        }

        try {
            val requestType = RequestType.valueOf(requestTypeString)
            when (requestType) {
                RequestType.Classification -> classifyImage(intent, receiver)
                RequestType.Update -> updateModel(receiver)
            }
        } catch (e: Exception) {
            responseWithError(receiver, e.message ?: "")
        }
    }

    private fun updateModel(receiver: ResultReceiver) {
        val versions = getListOfAllVersions().sortedBy { it.version }
        if (!versions.any()) {
            receiver.send(WorkStatus.FINISHED.ordinal, Bundle().apply {
                putString(Constants.IntentKeys.UPDATE_MESSAGE_KEY, "No versions are available")
            })
            return
        }
        val lastVersion = versions.last()
        if (lastVersion.version <= myVersion) {
            receiver.send(WorkStatus.FINISHED.ordinal, Bundle().apply {
                putString(Constants.IntentKeys.UPDATE_MESSAGE_KEY, "No new versions are available")
            })
            return
        }

        downloadModel(lastVersion, myModelFile)
        myVersion = lastVersion.version
        myVersionFile.writeText("{\n" +
                "    \"version\": $myVersion\n" +
                "}")
        myClassifier = DigitClassifier(myModelFile.inputStream())
        receiver.send(WorkStatus.FINISHED.ordinal, Bundle().apply {
            putString(Constants.IntentKeys.UPDATE_MESSAGE_KEY, "Successfully updated model to version $myVersion")
        })
    }

    private fun classifyImage(
        intent: Intent,
        receiver: ResultReceiver
    ): Boolean {
        val image = intent.getParcelableExtra<Bitmap>(Constants.IntentKeys.IMAGE_KEY)
        if (image == null) {
            responseWithError(receiver, "Request doesn't contain image")
            return true
        }
        val classificationResult = myClassifier.recognizeImage(image)

        receiver.send(WorkStatus.FINISHED.ordinal, Bundle().apply {
            putString(Constants.IntentKeys.PREDICTION_KEY, classificationResult.label)
            putFloat(Constants.IntentKeys.PREDICTION_CONFIDENCE_KEY, classificationResult.confidence)
        })
        return false
    }

    private fun moveResourceToExternal(resourceId: Int) : File {
        val name = resources.getResourceEntryName(resourceId)
        val externalFilesDir = getExternalFilesDir("model")
        val targetResourceFile = File(externalFilesDir, name)
        if (targetResourceFile.exists()) {
            return targetResourceFile
        }
        val resourceStream = resources.openRawResource(resourceId)
        val buffer = ByteArray(resourceStream.available())
        resourceStream.read(buffer)

        val targetFileStream = FileOutputStream(targetResourceFile, false)
        targetFileStream.write(buffer)
        targetFileStream.flush()
        targetFileStream.close()

        return targetResourceFile
    }

    private fun responseWithError(receiver: ResultReceiver, errorMessage: String) {
        receiver.send(WorkStatus.ERROR.ordinal, Bundle().apply {
            putString(Constants.IntentKeys.BUNDLE_ERROR_KEY, errorMessage)
        })
    }
}