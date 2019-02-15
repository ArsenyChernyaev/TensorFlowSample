package com.example.tensorflowsample

import java.util.*

object Constants {
    object Model {
        const val INPUT_IMG_SIZE_WIDTH = 28
        const val INPUT_IMG_SIZE_HEIGHT = 28
        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1
        const val MODEL_INPUT_SIZE = FLOAT_TYPE_SIZE * INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT * PIXEL_SIZE

        val OUTPUT_LABELS: List<String> = Collections.unmodifiableList(
            Arrays.asList("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
        )
    }

    object IntentKeys {
        const val RECEIVER_KEY = "receiver"
        const val BUNDLE_ERROR_KEY = "error"
        const val IMAGE_KEY = "image"
        const val PREDICTION_KEY = "prediction"
        const val PREDICTION_CONFIDENCE_KEY = "confidence"
        const val UPDATE_MESSAGE_KEY = "update_result"
    }

    object Errors {
        const val IMAGE_PROCESSING_ERROR = "Image processing error"
        const val UNKNOWN_ERROR_MESSAGE = "Unknown error"
    }
}