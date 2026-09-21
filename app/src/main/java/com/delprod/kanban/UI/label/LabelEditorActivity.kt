package com.delprod.kanban.UI.label

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.R
import com.delprod.kanban.UI.label.label_utils.BarcodeConfig
import com.delprod.kanban.UI.label.label_utils.DefaultLabelData
import com.delprod.kanban.UI.label.label_utils.LabelRenderer
import com.delprod.kanban.core.App
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.databinding.ActivityLabelEditorBinding
import com.delprod.kanban.db.LabelData
import com.delprod.kanban.utils.EditTextExtentions.attach
import com.delprod.kanban.utils.launchAndCollectIn
import com.delprod.kanban.utils.toast
import com.google.zxing.BarcodeFormat
import org.apache.poi.sl.usermodel.TextParagraph

class LabelEditorActivity : AppCompatActivity() {

    private lateinit var fields: MutableList<LabelField>
    private val binding by lazy { ActivityLabelEditorBinding.inflate(layoutInflater) }
    private lateinit var viewModel: LabelViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel = (this.application as App).labelViewModel

        val uuid = intent.getStringExtra("LABEL_UUID")
        if(uuid != null){
            viewModel.fetchLabel(uuid)
            bind()
            binding.deleteBtn.isVisible = true
            binding.deleteBtn.setOnClickListener {
                val ids: MutableList<Int> = mutableListOf()
                fields.forEach {
                    ids.add(it.id)
                }
                viewModel.deleteLabel(ids)
                finish()
            }
        }else{
            fields = DefaultLabelData.createDefaultFields()
            binding.rvFields.layoutManager = LinearLayoutManager(this@LabelEditorActivity)
            binding.rvFields.adapter = LabelEditorFieldAdapter(fields){
                showPreview()
            }
            binding.deleteBtn.isVisible = false
            binding.progressBar.isVisible = false
            binding.message.isVisible = false
        }


        binding.labelCode.attach{ onText ->
            fields.forEach {
                it.code = onText
            }
            showPreview()
        }
        binding.labelName.attach { onText ->
            fields.forEach {
                it.labelName = onText
            }
        }

        binding.addLabelBtn.setOnClickListener {
            when{
                binding.labelName.text.length < 10 ->{
                    toast("Укажите наименование этикетки")
                    return@setOnClickListener
                }
                binding.labelCode.text.length == 0 || binding.labelCode.text.length > 5 -> {
                    toast("Исправьте артику товара. Прим. 00012")
                    return@setOnClickListener
                }
            }
            if (uuid == null){
                val label = fields.map {
                    LabelData(
                        uniqueName = it.uniqueName,
                        code = it.code,
                        uuid = it.uuid,
                        label = it.label,
                        text = it.text,
                        xMm = it.xMm,
                        yMm = it.yMm,
                        widthMm = it.widthMm,
                        heightMm = it.heightMm,
                        labelName = it.labelName,
                        fontSizeMm = it.fontSizeMm,
                        bold = it.bold,
                        align = it.align.name,
                        barCodeFormat = it.barCodeFormat.name
                    )
                }
                viewModel.insertLabel(label,
                    onSuccess = {
                        toast("Сохранено")
                        finish()
                    },
                    onFailure = {toast(it)})
            }else{
                val label = fields.map {
                    LabelData(
                        id = it.id,
                        uniqueName = it.uniqueName,
                        code = it.code,
                        uuid = it.uuid,
                        label = it.label,
                        text = it.text,
                        xMm = it.xMm,
                        yMm = it.yMm,
                        widthMm = it.widthMm,
                        heightMm = it.heightMm,
                        labelName = it.labelName,
                        fontSizeMm = it.fontSizeMm,
                        bold = it.bold,
                        align = it.align.name,
                        barCodeFormat = it.barCodeFormat.name
                    )
                }
                viewModel.updateLabel(label,
                    onSuccess = {
                        toast("Сохранено")
                        finish()
                    },
                    onFailure = {toast(it)})
            }

        }
    }

    private fun bind(){
        viewModel.uiLabelState.launchAndCollectIn(this){
            when(it){
                is LabelUiState.Idle -> {
                    binding.labelLayout.isVisible = false
                    binding.message.isVisible = true
                    binding.message.setText("Простой")
                    binding.progressBar.isVisible = false
                }
                is LabelUiState.Loading -> {
                    binding.labelLayout.isVisible = false
                    binding.message.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is LabelUiState.Loaded -> {
                    binding.labelLayout.isVisible = true
                    binding.message.isVisible = false
                    binding.progressBar.isVisible = false
                    fields = it.label.map {
                        LabelField(
                            uniqueName = it.uniqueName,
                            label = it.label,
                            uuid = it.uuid,
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
                    binding.labelCode.setText(fields.find { it.uniqueName == "name" }!!.code)
                    binding.labelName.setText(fields.find { it.uniqueName == "name" }!!.labelName)
                    binding.rvFields.layoutManager = LinearLayoutManager(this@LabelEditorActivity)
                    binding.rvFields.adapter = LabelEditorFieldAdapter(fields){
                        showPreview()
                    }
                }
                is LabelUiState.Error -> {
                    binding.labelLayout.isVisible = false
                    binding.message.isVisible = true
                    binding.message.setText("${it.message}")
                    binding.progressBar.isVisible = false
                }
                is LabelUiState.Deleted -> {
                    binding.labelLayout.isVisible = true
                    binding.message.isVisible = false
                    binding.progressBar.isVisible = false
                    fields.clear()
                    fields.addAll(DefaultLabelData.createDefaultFields())
                    binding.deleteBtn.isVisible = false
                    showPreview()
                }
            }
        }
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
        val weight = fields.find { it.uniqueName == "weight" }?.text?.replace(".", "")?.replace(" кг", "") ?: "null"
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
}