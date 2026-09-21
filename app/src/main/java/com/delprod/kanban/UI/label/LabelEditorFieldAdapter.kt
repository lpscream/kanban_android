package com.delprod.kanban.UI.label

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.R
import org.apache.poi.sl.usermodel.TextParagraph

class LabelEditorFieldAdapter(
    private val fields: MutableList<LabelField>,
    private val onChanged: () -> Unit
    ): RecyclerView.Adapter<LabelEditorFieldAdapter.LabelEditorFieldViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabelEditorFieldViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_label_field, parent, false)
        return LabelEditorFieldViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LabelEditorFieldViewHolder,
        position: Int
    ) {
        val field = fields[position]
        holder.tvLabel.text = field.label
        clearWatchers(holder)

        holder.etText.setText(field.text)
        holder.etX.setText(field.xMm.toPlain())
        holder.etY.setText(field.yMm.toPlain())
        holder.etW.setText(field.widthMm.toPlain())
        holder.etH.setText(field.heightMm.toPlain())
        holder.etFontSize.setText(field.fontSizeMm.toPlain())

        attach(holder.etText) { field.text = it; onChanged() }
        attach(holder.etX) { field.xMm = it.toFloatOrNull() ?: field.xMm; onChanged() }
        attach(holder.etY) { field.yMm = it.toFloatOrNull() ?: field.yMm; onChanged() }
        attach(holder.etW) { field.widthMm = it.toFloatOrNull() ?: field.widthMm; onChanged() }
        attach(holder.etH) { field.heightMm = it.toFloatOrNull() ?: field.heightMm; onChanged() }
        attach(holder.etFontSize) { field.fontSizeMm = it.toFloatOrNull() ?: field.fontSizeMm; onChanged() }


        val alignOptions = listOf("Слева", "По центру", "Справа")
        val alignValues = listOf(TextParagraph.TextAlign.LEFT, TextParagraph.TextAlign.CENTER, TextParagraph.TextAlign.RIGHT)

        holder.spAlign.adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_dropdown_item,
            alignOptions
        )
        holder.spAlign.onItemSelectedListener = null // сброс перед перепривязкой (важно из-за переиспользования вью в RecyclerView)
        holder.spAlign.setSelection(alignValues.indexOf(field.align))
        holder.spAlign.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                field.align = alignValues[position]
                onChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        holder.rbIsBold.setOnCheckedChangeListener(null)
        holder.rbIsBold.isChecked = field.bold
        holder.rbIsBold.setOnCheckedChangeListener {_, isChecked ->
            field.bold = isChecked
            onChanged()
        }
    }

    override fun getItemCount(): Int = fields.size


    inner class LabelEditorFieldViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvLabel: TextView = view.findViewById(R.id.tvFieldLabel)
        val etText: EditText = view.findViewById(R.id.etFieldText)
        val etX: EditText = view.findViewById(R.id.etX)
        val etY: EditText = view.findViewById(R.id.etY)
        val etW: EditText = view.findViewById(R.id.etW)
        val etH: EditText = view.findViewById(R.id.etH)
        val etFontSize: EditText = view.findViewById(R.id.etFontSize)
        val spAlign: Spinner = view.findViewById(R.id.spAlign)
        val rbIsBold: RadioButton = view.findViewById(R.id.rbIsBold)
    }


    private fun clearWatchers(holder: LabelEditorFieldViewHolder) {
        listOf(holder.etText, holder.etX, holder.etY, holder.etW, holder.etH, holder.etFontSize).forEach {
            (it.tag as? TextWatcher)?.let { w -> it.removeTextChangedListener(w) }
        }
    }

    private fun Float.toPlain(): String = if (this == toInt().toFloat()) toInt().toString() else toString()

    private fun attach(editText: EditText, onText: (String) -> Unit) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { onText(s?.toString() ?: "") }
            override fun afterTextChanged(s: Editable?) {}
        }
        editText.tag = watcher
        editText.addTextChangedListener(watcher)
    }
}