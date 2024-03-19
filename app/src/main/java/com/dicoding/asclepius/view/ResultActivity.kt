package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.squareup.picasso.Picasso

class ResultActivity : AppCompatActivity() {
    private lateinit var bind: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityResultBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        val dataImage:Uri? = intent.getParcelableExtra(IMAGE)
        val dataResult = intent.getStringExtra(RESULT)

        Picasso.get().load(dataImage).into(bind.resultImage)
        bind.resultText.text = dataResult
    }

    companion object {
        const val IMAGE = "image_uri"
        const val RESULT = "result"
    }

}