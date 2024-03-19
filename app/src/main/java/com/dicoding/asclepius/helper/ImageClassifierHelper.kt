package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.dicoding.asclepius.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


class ImageClassifierHelper(
    private val threshold: Float = 0.1f,
    private val maxResult: Int = 3,
    private val modeulName: String = "cancer_classification.tflite",
    private val context: Context,
    private val classifierListener: ClassifierListener?
) {
    private var imageClassifier: ImageClassifier? = null
    private var tensorImage: TensorImage? = null

    interface ClassifierListener{
        fun onErr(err: String)
        fun onResult(
            result: List<Classifications>?,
            inferenceTime: Long
        )
    }

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResult)
        val baseOptBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optBuilder.setBaseOptions(baseOptBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modeulName,
                optBuilder.build()
            )
        } catch (e: IllegalStateException) {
            classifierListener?.onErr(context.getString(R.string.image_classifier_failed))
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun classifyStaticImage(imageUri: Uri) {
        if (imageClassifier == null){
            setupImageClassifier()
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(CastOp(DataType.UINT8))
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        }.copy(Bitmap.Config.ARGB_8888, true)?.let { bitmap ->
            tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
        }

        val imageProcessOpt = ImageProcessingOptions.builder()
            .build()

        var inferenceTime = SystemClock.uptimeMillis()
        val results = imageClassifier?.classify(tensorImage, imageProcessOpt)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        classifierListener?.onResult(
            result = results,
            inferenceTime = inferenceTime
        )
    }

}