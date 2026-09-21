package com.delprod.kanban.UI.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.delprod.kanban.R
import com.delprod.kanban.UI.auth.LoginActivity
import com.delprod.kanban.core.App
import com.delprod.kanban.data.Settings
import com.delprod.kanban.databinding.ActivitySettingsBinding
import com.delprod.kanban.utils.launchAndCollectIn
import com.delprod.kanban.utils.toast
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter


class SettingsActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }
    private lateinit var viewModel: SettingsViewModel
    private lateinit var settings: Settings
    private lateinit var export: String
    private val TAG = "SettingsActivity_log"



    // 1. Лаунчер для СОЗДАНИЯ и ЗАПИСИ файла
    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain") // Задаем MIME-тип файла
    ) { uri: Uri? ->
        uri?.let {
            saveTextToUri(this, it, export)
            Toast.makeText(this, "Файл успешно сохранен", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Лаунчер для ВЫБОРА и ЧТЕНИЯ файла
    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileContent = readTextFromUri(this, it)
            // Здесь вы можете вывести текст в TextView, например: binding.textView.text = fileContent
            viewModel.importSettings(
                fileContent,
                onSuccess = { toast("успешно") },
                onFailure = { toast(it) }
            )
            Toast.makeText(this, "Прочитано: $fileContent", Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        settings = (this.application as App).settings
        viewModel = (this.application as App).settingsViewModel

        binding.botId.setText(viewModel.botId())
        binding.chatId.setText(viewModel.chatId())

        binding.testBotButton.setOnClickListener {
            Log.d(TAG, "onCreate: test connection pressed button")
            viewModel.chatId(binding.chatId.text.toString())
            viewModel.botId(binding.botId.text.toString())
            viewModel.testConnection()
        }


        viewModel.testConnectionFlow.launchAndCollectIn(this){
            binding.responseMessage.setText(it.toString())
        }

        binding.savePassBtn.setOnClickListener {
            settings.password(binding.editTextNumberPassword.text.toString())
        }

        binding.exportSettingsBtn.setOnClickListener {
            viewModel.exportSettings(
                onSuccess = {
                    export = it
                    createFileLauncher.launch("settings.json")
                },
                onFailure = {toast(it)}
            )

        }

        binding.importSettingsBtn.setOnClickListener {
                openFileLauncher.launch(arrayOf("text/plain"))
        }

        binding.serverUrl.setText(settings.serverBaseUrl())
        binding.saveServerUrlBtn.setOnClickListener {
            val url = binding.serverUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                settings.serverBaseUrl(url)
                toast("Адрес сервера сохранён, перезапустите приложение")
            }
        }

        binding.logoutBtn.setOnClickListener {
            (this.application as App).authRepository.logout()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
    }



    private fun saveTextToUri(context: Context, uri: Uri, text: String) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.write(text)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Вспомогательная функция чтения
    private fun readTextFromUri(context: Context, uri: Uri): String {
        val stringBuilder = StringBuilder()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line).append("\n")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString().trimEnd()
    }
}