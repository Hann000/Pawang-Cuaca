package com.kelp.pawangcuaca.Activitis

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.kelp.pawangcuaca.R

class PrediksiActivity : AppCompatActivity() {

    private lateinit var ortEnv: OrtEnvironment
    private var ortSession: OrtSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediksi)

        val kembaliBtn = findViewById<Button>(R.id.kembaliBtn)
        val prediksiBtn = findViewById<Button>(R.id.prediksiBtn)

        initializeModel()

        kembaliBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        prediksiBtn.setOnClickListener {
            val inputData = collectInputData()
            if (inputData != null) {
                predictWeather(inputData)
            } else {
                Toast.makeText(
                    this,
                    "Harap masukkan semua data input dengan benar.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initializeModel() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            val modelPath = "weather_model.onnx"
            val assetManager = assets
            val modelStream = assetManager.open(modelPath)
            val modelBytes = modelStream.readBytes()
            ortSession = ortEnv.createSession(modelBytes)

            ortSession?.let { session ->
                Log.d("PrediksiActivity", "Model loaded successfully")
                Log.d("PrediksiActivity", "Input names: ${session.inputNames}")
                Log.d("PrediksiActivity", "Output names: ${session.outputNames}")

                session.inputInfo.forEach { (name, info) ->
                    Log.d("PrediksiActivity", "Input '$name' info: ${info.info}")
                }

                session.outputInfo.forEach { (name, info) ->
                    Log.d("PrediksiActivity", "Output '$name' info: ${info.info}")
                }
            }

            if (ortSession == null) {
                Toast.makeText(this, "Gagal memuat model.", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e("PrediksiActivity", "Error loading model", e)
            Toast.makeText(this, "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun collectInputData(): FloatArray? {
        val suhu = findViewById<EditText>(R.id.suhuEdit).text.toString().toFloatOrNull()
        val angin = findViewById<EditText>(R.id.WspeedEdit).text.toString().toFloatOrNull()
        val kelembapan = findViewById<EditText>(R.id.HumEdit).text.toString().toFloatOrNull()
        val uv = findViewById<EditText>(R.id.uvEdit).text.toString().toFloatOrNull()

        return if (suhu != null && angin != null && kelembapan != null && uv != null) {
            floatArrayOf(suhu, angin, kelembapan, uv)
        } else {
            Toast.makeText(this, "Semua data input harus diisi dengan benar.", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun predictWeather(inputData: FloatArray) {
        if (ortSession == null) {
            Toast.makeText(this, "Model belum dimuat. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val session = ortSession ?: throw IllegalStateException("Model belum diinisialisasi.")
            val inputName = session.inputNames.iterator().next()

            val shape = longArrayOf(1, 4) // Pastikan dimensi input sesuai dengan model
            val inputTensor = OnnxTensor.createTensor(ortEnv, inputData.reshape(shape))

            val inputs = mapOf(inputName to inputTensor)
            val result = session.run(inputs)

            val output = result[0].value
            Log.d("PrediksiActivity", "Output value: $output")
            Log.d("PrediksiActivity", "Output type: ${output?.javaClass?.name}")

            val prediction = when (output) {
                is Array<*> -> {
                    if (output.isNotEmpty() && output[0] is String) {
                        output[0] as String // Ambil prediksi pertama
                    } else {
                        null
                    }
                }
                else -> {
                    throw IllegalStateException("Tipe output tidak didukung: ${output?.javaClass}")
                }
            }

            runOnUiThread {
                if (prediction != null) {
                    updatePredictionText(prediction) // Perbarui teks hasil prediksi
                    showPredictionPopup(prediction) // Tampilkan popup hasil prediksi
                } else {
                    Toast.makeText(
                        this,
                        "Gagal menginterpretasi hasil prediksi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } catch (e: Exception) {
            Log.e("PrediksiActivity", "Error during prediction", e)
            runOnUiThread {
                Toast.makeText(this, "Gagal melakukan prediksi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun FloatArray.reshape(shape: LongArray): Array<FloatArray> {
        require(shape.size == 2 && shape[0].toInt() * shape[1].toInt() == this.size) {
            "Invalid shape for reshaping"
        }
        val rows = shape[0].toInt()
        val cols = shape[1].toInt()
        return Array(rows) { i ->
            FloatArray(cols) { j ->
                this[i * cols + j]
            }
        }
    }

    private fun showPredictionPopup(weatherType: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_layout)
        dialog.setCancelable(true)

        val closeButton = dialog.findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        val animationView = dialog.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val predictionText = dialog.findViewById<TextView>(R.id.predictionText)

        val animationFile = when (weatherType.lowercase()) {
            "sunny" -> R.raw.sunny_animation
            "rainy" -> R.raw.rainy_animation
            "cloudy" -> R.raw.cloudy_animation
            "snowy" -> R.raw.snowy_animation
            else -> R.raw.sunny_animation
        }

        val predictionTextString =  when (weatherType.lowercase()) {
            "sunny" -> "Cuaca Akan Cerah"
            "rainy" -> "Cuaca Akan Hujan"
            "cloudy" -> "Cuaca Akan Sedikit Berawan"
            "snowy" -> "Cuaca Akan Turun Salju"
            else -> "Data Cuaca Tidak Tersedia"
        }

        animationView.setAnimation(animationFile)
        animationView.playAnimation()

        predictionText.text = "$predictionTextString"

        dialog.show()
    }

    private fun updatePredictionText(prediction: String) {
        val predictionTextView = findViewById<TextView>(R.id.predictionText)
        if (predictionTextView != null) {
            predictionTextView.text = "$prediction"
        } else {
            Log.e("PrediksiActivity", "TextView 'predictionText' not found.")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            ortSession?.let {
                it.close()
            }
            ortEnv.close()
        } catch (e: Exception) {
            Log.e("PrediksiActivity", "Error during cleanup", e)
        }
    }
}
