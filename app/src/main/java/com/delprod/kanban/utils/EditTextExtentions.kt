package com.delprod.kanban.utils

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.widget.EditText
import com.delprod.kanban.R
import java.text.SimpleDateFormat
import java.util.Locale

object EditTextExtentions {


        // 2 знака до точки, 2 знака после точки
    fun EditText.addPriceChangeListener(maxDigits: Int = 4, onClick: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                var clean = s.toString().replace("[^\\d]".toRegex(), "")

                // Ограничиваем максимальную длину: 2 знака до точки + 2 после = 4 цифры
                if (clean.length > maxDigits) {
                    clean = clean.takeLast(maxDigits) // отбрасываем "лишнее" слева
                }

                if (clean.isNotEmpty()) {
                    val parsed = clean.toDouble() / 100
                    val formatted = String.format(java.util.Locale.US, "%.2f", parsed)
                    this@addPriceChangeListener.setText(formatted)
                    this@addPriceChangeListener.setSelection(formatted.length)
                    onClick(formatted)
                } else {
                    this@addPriceChangeListener.setText("")
                    onClick("")
                }
                isEditing = false
            }
        })
    }

//    fun EditText.addPriceChangeListener(onClick: (String) -> Unit){
//        this.addTextChangedListener(object : TextWatcher {
//            private var isEditing = false
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(s: Editable?) {
//                if (isEditing) return
//                isEditing = true
//                val clean = s.toString().replace("[^\\d]".toRegex(), "")
//                if (clean.isNotEmpty()) {
//                    val parsed = clean.toDouble() / 100
//                    val formatted = String.format(java.util.Locale.US,"%.2f", parsed)
//                    this@addPriceChangeListener.setText(formatted)
//                    this@addPriceChangeListener.setSelection(formatted.length)
//                    onClick(formatted)
//                } else {
//                    this@addPriceChangeListener.setText("")
//                    onClick("")
//                }
//                isEditing = false
//            }
//        })
//    }


    fun EditText.dateFormatValidation(
        onValid: (Boolean) -> Unit
    ) {
        var isDeleting = false
        var isFormatting = false
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {
                // count > after означает, что символов удаляется больше, чем добавляется -> это удаление
                isDeleting = count > after
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return // защита от рекурсии при programmatic setText
                if (!isDeleting) {
                    when (this@dateFormatValidation.text.length) {
                        2, 5 -> {
                            isFormatting = true
                            this@dateFormatValidation.text =
                                SpannableStringBuilder("${this@dateFormatValidation.text}.")
                            this@dateFormatValidation.setSelection(this@dateFormatValidation.length())
                            isFormatting = false
                        }
                    }
                }
                val currentText = this@dateFormatValidation.text.toString()
                if (!isValidDate(currentText)) {
                    this@dateFormatValidation.error = this@dateFormatValidation.resources.getString(
                        R.string.date_format
                    )
                    onValid(false)
                } else {
                    this@dateFormatValidation.error = null
                    onValid(true)
                }
            }
        })
    }


    fun EditText.isValidDate(): Boolean {
        return isValidDate(this@isValidDate.text.toString())
    }

    private fun isValidDate(dateString: String): Boolean {
        // Если поле пустое, ошибку не показываем
        if (dateString.isEmpty()) {
            return true
        }
        return try {
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            dateFormat.isLenient = false // Строгая проверка даты
            dateFormat.parse(dateString)
            when(dateString.length){
                8 -> true
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }


    fun EditText.attach(onText: (String) -> Unit){
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { onText(s?.toString() ?: "") }
            override fun afterTextChanged(s: Editable?) {}
        }
        this.tag = watcher
        this.addTextChangedListener(watcher)
    }
}