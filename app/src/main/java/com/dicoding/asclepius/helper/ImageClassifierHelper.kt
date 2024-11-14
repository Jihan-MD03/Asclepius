package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions

class ImageClassifierHelper(
    var threshold: Float = 0.1f,
    var maxResults: Int = 3,
    val modelName: String = "cancer_classification.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)

        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: Exception) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyImage(image: Bitmap) {
        if (imageClassifier == null) {
            setupImageClassifier()
        }

        // Mengonversi bitmap ke format ARGB_8888
        val bitmapArgb = image.copy(Bitmap.Config.ARGB_8888, true)

        // Konversi ke TensorImage
        val tensorImage = TensorImage.fromBitmap(bitmapArgb)

        // Menghitung waktu inferensi
        val inferenceTime = SystemClock.uptimeMillis()

        // Melakukan klasifikasi
        val results = imageClassifier?.classify(tensorImage)

        // Mengirimkan hasil
        classifierListener?.onResults(results, SystemClock.uptimeMillis() - inferenceTime)
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: List<Classifications>?, inferenceTime: Long)
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}

