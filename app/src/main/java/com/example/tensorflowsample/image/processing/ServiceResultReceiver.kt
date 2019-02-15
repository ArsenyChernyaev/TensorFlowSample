package com.example.tensorflowsample.image.processing

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class ServiceResultReceiver(
    handler: Handler,
    private val predictionProcessingCallback: (resultStatus: WorkStatus, resultData: Bundle) -> Unit
) :
    ResultReceiver(handler) {

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        val workStatus = WorkStatus.values()[resultCode]
        predictionProcessingCallback(workStatus, resultData)
    }
}