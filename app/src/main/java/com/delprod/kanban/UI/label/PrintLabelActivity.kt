package com.delprod.kanban.UI.label

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.delprod.kanban.R
import com.delprod.kanban.UI.label.dialogs.PrintLabelDialog
import com.delprod.kanban.UI.label.label_utils.BarcodeConfig
import com.delprod.kanban.UI.label.label_utils.DefaultLabelData
import com.delprod.kanban.UI.label.label_utils.LabelRenderer
import com.delprod.kanban.UI.label.label_utils.TsplBuilder
import com.delprod.kanban.core.App
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.databinding.ActivityPrintLabelBinding
import com.delprod.kanban.db.PrinterSettings
import com.delprod.kanban.utils.EditTextExtentions.addPriceChangeListener
import com.delprod.kanban.utils.EditTextExtentions.attach
import com.delprod.kanban.utils.EditTextExtentions.dateFormatValidation
import com.delprod.kanban.utils.launchAndCollectIn
import com.delprod.kanban.utils.toast
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.sl.usermodel.TextParagraph
import java.net.InetSocketAddress
import java.net.Socket
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PrintLabelActivity : AppCompatActivity() {

    private lateinit var viewwModel: LabelViewModel
    private val binding by lazy { ActivityPrintLabelBinding.inflate(layoutInflater) }
    private lateinit var fields: MutableList<LabelField>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewwModel = (this.application as App).labelViewModel

        val uuid = intent.getStringExtra("LABEL_UUID")
        logPrint("LABEL_UUID: $uuid")
        if (uuid.isNullOrEmpty()){
            binding.labelLayout.isVisible = false
            binding.message.isVisible = true
            binding.message.setText("Ошибка товарного кода")
        }else   {
            viewwModel.fetchLabel(uuid)
            binding.message.isVisible = false
        }

        viewwModel.uiLabelState.launchAndCollectIn(this){
            when(it){
                is LabelUiState.Idle -> {}
                is LabelUiState.Loading -> {
                    binding.labelLayout.isVisible = false
                    binding.message.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is LabelUiState.Error -> {
                    binding.labelLayout.isVisible = false
                    binding.message.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.message.setText(it.message)
                }
                is LabelUiState.Loaded -> {
                    binding.labelLayout.isVisible = true
                    binding.message.isVisible = false
                    binding.progressBar.isVisible = false
                    logPrint(it.label.toString())
                    it.label.forEach {
                        logPrint(it.toString())
                    }
                    fields = it.label.map {
                        LabelField(
                            uniqueName = it.uniqueName,
                            label = it.label,
                            text = it.text,
                            xMm = it.xMm,
                            yMm = it.yMm,
                            widthMm = it.widthMm,
                            heightMm = it.heightMm,
                            labelName = it.labelName,
                            code = it.code,
                            fontSizeMm = it.fontSizeMm,
                            bold = it.bold,
                            id = it.id,
                            align = enumValueOf<TextParagraph.TextAlign>(it.align),
                            barCodeFormat = enumValueOf<BarcodeFormat>(it.barCodeFormat)
                        )
                    } as MutableList<LabelField>
                    fields.forEach {
                        logPrint("label line")
                        logPrint(it.toString())
                    }
                    binding.dateToPrint.setText(fields.find { it.uniqueName == "mfgDate" }?.text)
                    binding.weightOnLabel.setText(fields.find { it.uniqueName == "weight" }?.text)
                    showPreview()
                }
                is LabelUiState.Deleted -> {}
            }
        }

        binding.dateToPrint.dateFormatValidation { onValid ->
            if (onValid){
                logPrint(binding.dateToPrint.text.toString())
                fields.find { it.uniqueName == "mfgDate" }?.text = binding.dateToPrint.text.toString()
                val timeToLive =  fields.find { it.uniqueName == "expDays" }?.text
                try {
                    fields.find { it.uniqueName == "expDate" }?.text = expDateCalculate(binding.dateToPrint.text.toString(), timeToLive?.toInt()!!)
                }catch (e: Exception){
                    logPrint(e.message.toString())
                }
                showPreview()
            }
        }
        binding.weightOnLabel.addPriceChangeListener {
            fields.find { it.uniqueName == "weight" }?.text = "${binding.weightOnLabel.text} кг."
            showPreview()
        }

        binding.printBtn.setOnClickListener { PrintLabelDialog.create(
            this@PrintLabelActivity,
            layoutInflater,
            viewwModel){
            //fetch printer setting
            // lets print label
            onPrintClicked(it)
        } }
    }


    private fun showPreview(){
        val barCodeConfig = readBarcodeConfigFromInputs()
        logPrint("barcodeConfig: $barCodeConfig")
        try {
            val bitmap = LabelRenderer.renderLabel(
                DefaultLabelData.DEFAULT_LABEL_WIDTH_MM,
                DefaultLabelData.DEFAULT_LABEL_HEIGHT_MM,
                fields,
                barCodeConfig
            )
            binding.ivPreview.setImageBitmap(bitmap)
        }catch (e: Exception){
            toast("Ошибка предпросмотра: ${e.message}")
        }
    }



    private fun readBarcodeConfigFromInputs(): BarcodeConfig {
//        val plu = fields.find { it.uniqueCode == "name" }?.text?.replace(".", "") ?: "00000"
        val plu = fields.find { it.uniqueName == "name" }?.code.toString().trim()
        val date = fields.find { it.uniqueName == "mfgDate" }?.text?.replace(".", "") ?: "null"
        val weight = (fields.find { it.uniqueName == "weight" }?.text?.replace(".", "")?.replace(" кг", "") ?: "null").plus("0")
        val xMm = fields.find{ it.uniqueName == "barCodePic"}?.xMm ?: 2.0f
        val yMm = fields.find { it.uniqueName == "barCodePic" }?.yMm ?: 2.0f
        val widthMm = fields.find { it.uniqueName =="barCodePic" }?.widthMm ?: 0.0f
        val heightMm = fields.find { it.uniqueName == "barCodePic" }?.heightMm ?: 0.0f

        fields.find { it.uniqueName == "barCodeDigits" }?.text = "25$plu$weight$date"
        fields.find { it.uniqueName == "barCodePic" }?.text = "25$plu$weight$date"

        fields.forEach {
            logPrint(it.toString())
        }
        return BarcodeConfig(
            data = "25$plu$weight$date",
            xMm = xMm,
            yMm = yMm,
            widthMm = widthMm,
            heightMm = heightMm
        )
    }


    private fun expDateCalculate(date: String, plusDays: Int): String{
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
        val date = LocalDate.parse(date, formatter)
        val newDate = date.plusDays(plusDays.toLong()).toString()
        return LocalDate.parse(
            newDate,
            DateTimeFormatter.ofPattern("yyyy-MM-dd")).format(formatter)
    }



    private fun onPrintClicked(printer: PrinterSettings) {
        val ip = printer.ip.toString()
        val port = printer.port.trim().toIntOrNull() ?: 9100
        val qty = binding.labelQuantity.text.toString().trim().toIntOrNull() ?: 1
//        if (ip.isEmpty()) {
//            tvStatus.text = "Укажите IP принтера"
//            return
//        }
        val barcodeConfig = readBarcodeConfigFromInputs()
        val widthMm = DefaultLabelData.DEFAULT_LABEL_WIDTH_MM
        val heightMm = DefaultLabelData.DEFAULT_LABEL_HEIGHT_MM

        binding.printBtn.text = "Отправка на печать..."

        lifecycleScope.launch(Dispatchers.IO) {
            var socket: Socket? = null
            try {
//                val payload = if (useBitmapMode) {
                val payload =   TsplBuilder.buildBitmapModeCommand(widthMm, heightMm, fields, barcodeConfig, qty)
//                } else {
//                    TsplBuilder.buildCodepageModeCommand(widthMm, heightMm, fields, barcodeConfig, qty)
//                }

                socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 5000)
                socket.getOutputStream().apply { write(payload); flush() }

                withContext(Dispatchers.Main) {  binding.printBtn.text = "Отправлено успешно: $qty шт." }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {  binding.printBtn.text = "Ошибка печати: ${e.message}" }
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        }
    }
}