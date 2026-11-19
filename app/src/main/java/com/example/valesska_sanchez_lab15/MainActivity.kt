package com.example.valesska_sanchez_lab15

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import java.io.File
import kotlin.math.min

/**
 * Lab 15 – Malaria detection in red blood cells (Parasitized / Uninfected)
 * with ML Kit Custom Image Labeling.
 * Based on the Codelab "Build your first computer-vision app with ML Kit's Image Labelling".
 * The model blood_cells_model.tflite (with metadata and labels) is in app/src/main/assets/.
 *
 * Image sources:
 *  - 4 test images (res/drawable, taken from test_images/ in Colab)
 *  - Phone camera (full-resolution photo through FileProvider)
 *  - Phone gallery (system photo picker, no permissions needed)
 */
class MainActivity : AppCompatActivity() {

    // The 4 test images with their true class
    private val samples = listOf(
        R.drawable.cell_parasitized_1 to "Parasitized",
        R.drawable.cell_parasitized_2 to "Parasitized",
        R.drawable.cell_uninfected_1 to "Uninfected",
        R.drawable.cell_uninfected_2 to "Uninfected"
    )
    private var index = 0

    private lateinit var img: ImageView
    private lateinit var txtOutput: TextView
    private lateinit var txtImageName: TextView
    private lateinit var labeler: ImageLabeler
    private lateinit var currentBitmap: Bitmap

    // Uri of the file where the camera saves the photo
    private var photoUri: Uri? = null

    // Camera: opens the phone's camera app and saves the photo to photoUri
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            val uri = photoUri
            if (saved && uri != null) showExternalImage(uri, "Camera photo")
        }

    // Gallery: Android photo picker (no storage permission required)
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) showExternalImage(uri, "Gallery image")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        img = findViewById(R.id.imageToLabel)
        txtOutput = findViewById(R.id.txtOutput)
        txtImageName = findViewById(R.id.txtImageName)

        // 1. Load our own model from assets/
        val localModel = LocalModel.Builder()
            .setAssetFilePath("blood_cells_model.tflite")
            .build()

        // 2. Create the labeler: return both classes with their probability
        val options = CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.0f)
            .setMaxResultCount(2)
            .build()
        labeler = ImageLabeling.getClient(options)

        showSample()

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            index = (index + 1) % samples.size; showSample()
        }
        findViewById<Button>(R.id.btnPrev).setOnClickListener {
            index = (index - 1 + samples.size) % samples.size; showSample()
        }
        findViewById<Button>(R.id.btnTest).setOnClickListener { labelImage() }
        findViewById<Button>(R.id.btnCamera).setOnClickListener { openCamera() }
        findViewById<Button>(R.id.btnGallery).setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    /** Shows the current test image. */
    private fun showSample() {
        val (resId, realClass) = samples[index]
        currentBitmap = BitmapFactory.decodeResource(resources, resId)
        img.setImageBitmap(currentBitmap)
        txtImageName.text = "Image ${index + 1}/${samples.size} – true class: $realClass"
        txtOutput.text = "Press «Label Image»"
    }

    /** Creates a temporary file in cache/images/ and opens the camera. */
    private fun openCamera() {
        val dir = File(cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "camera_photo.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        photoUri = uri
        takePicture.launch(uri)
    }

    /**
     * Loads a camera/gallery photo and classifies it.
     * The whole image is used (no cropping), exactly like in Colab training,
     * so no part of the cell (or the parasite) is lost.
     */
    private fun showExternalImage(uri: Uri, source: String) {
        try {
            val bitmap = loadBitmap(uri)
            currentBitmap = bitmap
            img.setImageBitmap(bitmap)
            txtImageName.text = "$source – true class: unknown"
            labelImage()   // classify immediately
        } catch (e: Exception) {
            txtOutput.text = "Could not open the image: ${e.message}"
        }
    }

    /** Reads the image already rotated correctly and scaled down (max. 1024 px). */
    private fun loadBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE   // ML Kit needs a software bitmap
                val w = info.size.width
                val h = info.size.height
                val scale = min(1f, 1024f / maxOf(w, h))
                decoder.setTargetSize((w * scale).toInt(), (h * scale).toInt())
            }
        } else {
            val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
            contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opts) }
                ?: throw IllegalStateException("unreadable image")
        }
    }


    /** 3. Runs inference and shows labels + probabilities. */
    private fun labelImage() {
        val image = InputImage.fromBitmap(currentBitmap, 0)
        txtOutput.text = "Classifying…"
        labeler.process(image)
            .addOnSuccessListener { labels ->
                if (labels.isEmpty()) {
                    txtOutput.text = "No results"
                    return@addOnSuccessListener
                }
                val sb = StringBuilder()
                val best = labels.maxBy { it.confidence }
                sb.append("PREDICTION: ${best.text}\n")
                sb.append("Confidence: ${"%.2f".format(best.confidence * 100)} %\n\n")
                sb.append("Probabilities:\n")
                labels.sortedByDescending { it.confidence }.forEach {
                    sb.append("• ${it.text.padEnd(12)} ${"%6.2f".format(it.confidence * 100)} %\n")
                }
                txtOutput.text = sb.toString()
            }
            .addOnFailureListener { e ->
                txtOutput.text = "Error: ${e.message}"
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        labeler.close()
    }
}
