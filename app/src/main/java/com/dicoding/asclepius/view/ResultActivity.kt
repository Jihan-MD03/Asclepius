package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Toolbar sebagai ActionBar
        setSupportActionBar(binding.toolbar)

        // Menampilkan tombol back di ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        val label = intent.getStringExtra(EXTRA_RESULT_LABEL) ?: "Unknown"
        val score = intent.getFloatExtra(EXTRA_RESULT_SCORE, 0.0f)
        // Menampilkan gambar yang dipilih
        Log.d("ResultActivity", "Received URI: $imageUri")
        binding.resultImage.setImageURI(imageUri)

        // Menampilkan label hasil klasifikasi dan confidence score dalam satu TextView
        val resultText = "Label: $label\nConfidence Score: ${score * 100}%"
        binding.resultText.text = resultText

    }
    // Menangani aksi klik pada tombol kembali
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()  // Kembali ke activity sebelumnya
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT_LABEL = "extra_result_label"
        const val EXTRA_RESULT_SCORE = "extra_result_score"
    }
}
