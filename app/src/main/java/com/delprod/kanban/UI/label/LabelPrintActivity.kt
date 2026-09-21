package com.delprod.kanban.UI.label

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.R
import com.delprod.kanban.UI.label.label_utils.BarcodeConfig
import com.delprod.kanban.UI.label.label_utils.DefaultLabelData
import com.delprod.kanban.UI.label.label_utils.LabelRenderer
import com.delprod.kanban.UI.label.label_utils.TsplBuilder
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class LabelPrintActivity : AppCompatActivity() {

    private lateinit var etIp: EditText
    private lateinit var etPort: EditText
    private lateinit var etLabelWidth: EditText
    private lateinit var etLabelHeight: EditText
    private lateinit var etQty: EditText
    private lateinit var tvStatus: TextView
    private lateinit var ivPreview: ImageView
    private lateinit var rgMode: RadioGroup
    private lateinit var rvFields: RecyclerView

    private lateinit var etBarcodeData: EditText
    private lateinit var etBarcodeX: EditText
    private lateinit var etBarcodeY: EditText
    private lateinit var etBarcodeW: EditText
    private lateinit var etBarcodeH: EditText
    private lateinit var spBarcodeFormat: Spinner

    private val fields: MutableList<LabelField> = DefaultLabelData.createDefaultFields()
    private var barcodeConfig: BarcodeConfig = DefaultLabelData.createDefaultBarcode()

    private val barcodeFormats = listOf(
        BarcodeFormat.CODE_128, BarcodeFormat.CODE_39, BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8, BarcodeFormat.UPC_A, BarcodeFormat.ITF, BarcodeFormat.QR_CODE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_label_print)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.label_activity_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()
        setupFieldsList()
        setupBarcodeInputs()
        findViewById<Button>(R.id.btnPreview).setOnClickListener { showPreview() }
        findViewById<Button>(R.id.btnPrint).setOnClickListener { onPrintClicked() }
    }


    private fun bindViews() {
        etIp = findViewById(R.id.etIp)
        etIp.setText("192.168.7.28")
        etPort = findViewById(R.id.etPort)
        etLabelWidth = findViewById(R.id.etLabelWidth)
        etLabelHeight = findViewById(R.id.etLabelHeight)
        etQty = findViewById(R.id.etQty)
        tvStatus = findViewById(R.id.tvStatus)
        ivPreview = findViewById(R.id.ivPreview)
        rgMode = findViewById(R.id.rgMode)
        rvFields = findViewById(R.id.rvFields)

        etBarcodeData = findViewById(R.id.etBarcodeData)
        etBarcodeX = findViewById(R.id.etBarcodeX)
        etBarcodeY = findViewById(R.id.etBarcodeY)
        etBarcodeW = findViewById(R.id.etBarcodeW)
        etBarcodeH = findViewById(R.id.etBarcodeH)
        spBarcodeFormat = findViewById(R.id.spBarcodeFormat)

        etLabelWidth.setText(DefaultLabelData.DEFAULT_LABEL_WIDTH_MM.toInt().toString())
        etLabelHeight.setText(DefaultLabelData.DEFAULT_LABEL_HEIGHT_MM.toInt().toString())
    }

    private fun setupFieldsList() {
        rvFields.layoutManager = LinearLayoutManager(this)
        rvFields.adapter = LabelFieldAdapter(fields) {}
    }

    private fun setupBarcodeInputs() {
        etBarcodeData.setText(barcodeConfig.data)
        etBarcodeX.setText(barcodeConfig.xMm.toInt().toString())
        etBarcodeY.setText(barcodeConfig.yMm.toInt().toString())
        etBarcodeW.setText(barcodeConfig.widthMm.toInt().toString())
        etBarcodeH.setText(barcodeConfig.heightMm.toInt().toString())
        spBarcodeFormat.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, barcodeFormats.map { it.name }
        )
        spBarcodeFormat.setSelection(barcodeFormats.indexOf(barcodeConfig.format))
    }

    private fun readBarcodeConfigFromInputs() = BarcodeConfig(
        data = etBarcodeData.text.toString(),
        xMm = etBarcodeX.text.toString().toFloatOrNull() ?: 0f,
        yMm = etBarcodeY.text.toString().toFloatOrNull() ?: 0f,
        widthMm = etBarcodeW.text.toString().toFloatOrNull() ?: 40f,
        heightMm = etBarcodeH.text.toString().toFloatOrNull() ?: 15f,
        format = barcodeFormats[spBarcodeFormat.selectedItemPosition.coerceIn(
            0,
            barcodeFormats.lastIndex
        )]
    )

    private fun labelWidthMm() = etLabelWidth.text.toString().toFloatOrNull() ?: DefaultLabelData.DEFAULT_LABEL_WIDTH_MM
    private fun labelHeightMm() = etLabelHeight.text.toString().toFloatOrNull() ?: DefaultLabelData.DEFAULT_LABEL_HEIGHT_MM

    private fun showPreview() {
        barcodeConfig = readBarcodeConfigFromInputs()
        try {
            val bitmap = LabelRenderer.renderLabel(
                labelWidthMm(),
                labelHeightMm(),
                fields,
                barcodeConfig)
            ivPreview.setImageBitmap(bitmap)
            tvStatus.text = "Предпросмотр обновлён"
        } catch (e: Exception) {
            tvStatus.text = "Ошибка предпросмотра: ${e.message}"
        }
    }

    private fun onPrintClicked() {
        val ip = etIp.text.toString().trim()
        val port = etPort.text.toString().trim().toIntOrNull() ?: 9100
        val qty = etQty.text.toString().trim().toIntOrNull() ?: 1
        if (ip.isEmpty()) {
            tvStatus.text = "Укажите IP принтера"
            return
        }
        barcodeConfig = readBarcodeConfigFromInputs()
        val useBitmapMode = rgMode.checkedRadioButtonId == R.id.rbBitmap
        val widthMm = labelWidthMm()
        val heightMm = labelHeightMm()

        tvStatus.text = "Отправка на печать..."

        lifecycleScope.launch(Dispatchers.IO) {
            var socket: Socket? = null
            try {
                val payload = if (useBitmapMode) {
                    TsplBuilder.buildBitmapModeCommand(widthMm, heightMm, fields, barcodeConfig, qty)
                } else {
                    TsplBuilder.buildCodepageModeCommand(widthMm, heightMm, fields, barcodeConfig, qty)
                }

                socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 5000)
                socket.getOutputStream().apply { write(payload); flush() }

                withContext(Dispatchers.Main) { tvStatus.text = "Отправлено успешно: $qty шт." }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { tvStatus.text = "Ошибка печати: ${e.message}" }
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        }
    }



}