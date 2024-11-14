package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.yalantis.ucrop.UCrop
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.image_classifier_failed))
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        // TODO: Mendapatkan gambar dari Gallery.
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            binding.previewImageView.setImageURI(currentImageUri)  // Tampilkan gambar yang dipilih di Home
            startCrop(currentImageUri!!)  // Melanjutkan ke proses cropping
        } else {
            Log.d("Photo Picker", "No Media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            startCrop(uri)
        }
    }

    private fun startCrop(imageUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        UCrop.of(imageUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                currentImageUri = it  // Simpan URI hasil cropping
                binding.previewImageView.setImageURI(currentImageUri)  // Tampilkan gambar yang di-crop
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == UCrop.REQUEST_CROP) {
            showToast("Cropping dibatalkan.")  // Pesan pemberitahuan untuk pembatalan
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e("CropError", cropError?.message.toString())
            showToast(getString(R.string.crop_failed))
        }
    }

    private fun analyzeImage(uri: Uri) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            val classifierHelper = ImageClassifierHelper(
                context = this,
                classifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                        results?.firstOrNull()?.categories?.firstOrNull()?.let { category ->
                            val label = category.label
                            val score = category.score

                            val intent = Intent(this@MainActivity, ResultActivity::class.java)
                            intent.putExtra(ResultActivity.EXTRA_IMAGE_URI,uri.toString())
                            intent.putExtra(ResultActivity.EXTRA_RESULT_LABEL, label)
                            intent.putExtra(ResultActivity.EXTRA_RESULT_SCORE, score)
                            startActivity(intent)
                        }
                    }

                    override fun onError(error: String) {
                        showToast(error)
                    }
                }
            )
            classifierHelper.classifyImage(bitmap)
        }


    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}