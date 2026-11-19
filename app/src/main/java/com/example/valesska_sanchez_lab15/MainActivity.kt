package com.example.valesska_sanchez_lab15

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions

/**
 * Lab 15 – Detección de malaria en glóbulos rojos (Parasitized / Uninfected)
 * con ML Kit Custom Image Labeling.
 * Basado en el Codelab "Build your first computer-vision app with ML Kit's Image Labelling".
 * El modelo blood_cells_model.tflite (con metadatos y labels) está en app/src/main/assets/.
 */
class MainActivity : AppCompatActivity() {

    // Las 4 imágenes de prueba (res/drawable, salen de test_images/ en Colab) con su clase real
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        img = findViewById(R.id.imageToLabel)
        txtOutput = findViewById(R.id.txtOutput)
        txtImageName = findViewById(R.id.txtImageName)

        // 1. Cargar el modelo propio desde assets/
        val localModel = LocalModel.Builder()
            .setAssetFilePath("blood_cells_model.tflite")
            .build()

        // 2. Crear el etiquetador: devolvemos las 2 clases con su probabilidad
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
    }

    /** Muestra la imagen actual en el ImageView. */
    private fun showSample() {
        val (resId, realClass) = samples[index]
        currentBitmap = BitmapFactory.decodeResource(resources, resId)
        img.setImageBitmap(currentBitmap)
        txtImageName.text = "Imagen ${index + 1}/${samples.size} – clase real: $realClass"
        txtOutput.text = "Pulsa «Label Image»"
    }

    /** 3. Ejecuta la inferencia y muestra etiquetas + probabilidades. */
    private fun labelImage() {
        val image = InputImage.fromBitmap(currentBitmap, 0)
        txtOutput.text = "Clasificando…"
        labeler.process(image)
            .addOnSuccessListener { labels ->
                if (labels.isEmpty()) {
                    txtOutput.text = "Sin resultados"
                    return@addOnSuccessListener
                }
                val sb = StringBuilder()
                val best = labels.maxBy { it.confidence }
                sb.append("PREDICCIÓN: ${best.text}\n")
                sb.append("Confianza: ${"%.2f".format(best.confidence * 100)} %\n\n")
                sb.append("Probabilidades:\n")
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
