package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.squareup.picasso.Picasso
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {


    private lateinit var bind: ActivityMainBinding
    private lateinit var imageClassifier: ImageClassifierHelper

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        startGallery()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startGallery() {
        bind.galleryButton.setOnClickListener {
            val i = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            resulLauncher.launch(i)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val resulLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) {
        if (it != null) {
            showImage(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showImage(uri: Uri) {
        Picasso.get().load(uri).into(bind.previewImageView)
        bind.analyzeButton.setOnClickListener {
            analyzeImage(uri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun analyzeImage(uri: Uri) {
        imageClassifier = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onErr(err: String) {
                    runOnUiThread {
                        showToast(err)
                    }
                }

                override fun onResult(result: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        result?.let { it ->
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                println(it)
                                val sortedCategories =
                                    it[0].categories.sortedByDescending { it?.score }
                                val displayResult =
                                    sortedCategories.joinToString("\n") {
                                        "${it.label} " + NumberFormat.getPercentInstance()
                                            .format(it.score).trim()
                                    }
                                moveToResult(displayResult, inferenceTime, uri)
                            } else {
                                moveToResult("", inferenceTime = 0, uri)
                            }
                        }
                    }
                }
            }
        )

        imageClassifier.classifyStaticImage(uri)

    }

    private fun moveToResult(displayResult: String, inferenceTime: Long, uri: Uri) {
        val i = Intent(this, ResultActivity::class.java)
        i.putExtra(ResultActivity.RESULT, displayResult)
        i.putExtra("inference", inferenceTime)
        i.putExtra(ResultActivity.IMAGE, uri)
        startActivity(i)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}