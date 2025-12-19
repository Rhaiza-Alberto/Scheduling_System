package com.example.schedulingSystem

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.schedulingSystem.databinding.ActivityImageEditorBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageEditorBinding
    private var originalBitmap: Bitmap? = null
    private var currentBitmap: Bitmap? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        binding.toolbarEditor.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get image URI from Intent
        val uriString = intent.getStringExtra("IMAGE_URI")
        if (uriString != null) {
            imageUri = uriString.toUri()
            loadImage(imageUri!!)
        } else {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Rotate Button Logic
        binding.btnRotate.setOnClickListener {
            rotateImage()
        }

        // Crop Button Placeholder
        binding.btnCrop.setOnClickListener {
            // Basic cropping implementation or Toast for external library usage
            Toast.makeText(this, "Crop functionality coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Save Button Logic
        binding.btnSaveImage.setOnClickListener {
            saveAndReturnImage()
        }
    }

    private fun loadImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            originalBitmap = BitmapFactory.decodeStream(inputStream)
            currentBitmap = originalBitmap
            binding.imgPreview.setImageBitmap(currentBitmap)
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateImage() {
        currentBitmap?.let { bitmap ->
            val matrix = Matrix()
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            currentBitmap = rotatedBitmap
            binding.imgPreview.setImageBitmap(currentBitmap)
        }
    }

    private fun saveAndReturnImage() {
        currentBitmap?.let { bitmap ->
            try {
                // Save edited image to a temporary file
                val file = File(cacheDir, "edited_profile_photo.jpg")
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()

                val resultUri = Uri.fromFile(file)
                val resultIntent = Intent()
                resultIntent.putExtra("EDITED_IMAGE_URI", resultUri.toString())
                setResult(RESULT_OK, resultIntent)
                finish()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}