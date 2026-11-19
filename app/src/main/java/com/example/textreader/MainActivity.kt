package com.example.textreader

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private lateinit var openButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView
    private lateinit var textView: TextView

    // Открываем выбор файла. Вызывается, когда пользователь что-то выбрал
    // (или нажал "Назад" и тогда uri = null).
    private val openFile = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            textView.text = getString(R.string.no_file)
        } else {
            readTextFile(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openButton = findViewById(R.id.openButton)
        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)
        textView = findViewById(R.id.textView)

        openButton.setOnClickListener {
            // Показываем только файлы .txt
            openFile.launch(arrayOf("text/plain"))
        }
    }

    private fun readTextFile(uri: Uri) {
        textView.text = ""
        showLoading(true)

        // Читаем в отдельном потоке, чтобы не вешать экран
        Thread {
            val result = try {
                readFileToString(uri)
            } catch (e: Exception) {
                // Любая ошибка чтения
                "Ошибка чтения файла: ${e.message}"
            }

            runOnUiThread {
                showLoading(false)
                if (result.trim().isEmpty()) {
                    textView.text = getString(R.string.error_empty_file)
                } else {
                    textView.text = result
                }
            }
        }.start()
    }

    private fun readFileToString(uri: Uri): String {
        val input = contentResolver.openInputStream(uri)
            ?: return ""

        BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
            val builder = StringBuilder()
            var line = reader.readLine()
            while (line != null) {
                builder.append(line)
                builder.append("\n")
                line = reader.readLine()
            }
            return builder.toString()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        val visibility = if (isLoading) View.VISIBLE else View.GONE
        progressBar.visibility = visibility
        loadingText.visibility = visibility
        openButton.isEnabled = !isLoading
    }
}
