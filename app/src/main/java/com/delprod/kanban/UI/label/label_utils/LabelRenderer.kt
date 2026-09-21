package com.delprod.kanban.UI.label.label_utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.delprod.kanban.UI.label.LabelField
import com.delprod.kanban.core.logPrint

object LabelRenderer {

    const val PRINTER_DPI = 203 // уточните для своей модели (203 или 300)
    private const val DOTS_PER_MM = PRINTER_DPI / 25.4f
    private const val LINE_SPACING_MULTIPLIER = 1.25f

    fun mmToPx(mm: Float): Int = (mm * DOTS_PER_MM).toInt()

    fun renderLabel(
        labelWidthMm: Float,
        labelHeightMm: Float,
        fields: List<LabelField>,
        barcode: BarcodeConfig?
    ): Bitmap {
        val widthPx = mmToPx(labelWidthMm).coerceAtLeast(1)
        val heightPx = mmToPx(labelHeightMm).coerceAtLeast(1)

        //fixme colro print default ARGB_8888
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        //fixme inverse color on print
        canvas.drawColor(Color.WHITE)

        fields.forEach { if (it.text.isNotBlank()) drawField(canvas, it) }
        barcode?.let { drawBarcode(canvas, it) }

        return bitmap
    }

    private fun drawField(canvas: Canvas, field: LabelField) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            //fixme inverse color on print
            color = Color.BLACK
            textSize = mmToPx(field.fontSizeMm).toFloat()
            typeface = if (field.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        val xPx = mmToPx(field.xMm).toFloat()
        val yPx = mmToPx(field.yMm).toFloat()
        val widthPx = mmToPx(field.widthMm).toFloat()
        val heightPx = mmToPx(field.heightMm).toFloat()
        val lineHeightPx = paint.textSize * LINE_SPACING_MULTIPLIER


        val wrapped = TextWrapUtil.wrapTextByWidth(paint, field.text, widthPx)
        val clipped = TextWrapUtil.clipLinesByHeight(wrapped, lineHeightPx, heightPx)

        var lineY = yPx + paint.textSize
        for (line in wrapped) {
            val lineX = TextWrapUtil.resolveLineX(paint, line, xPx, widthPx, field.align)
            canvas.drawText(line, lineX, lineY, paint)
//            canvas.drawText(line, xPx, lineY, paint)
            lineY += lineHeightPx
        }
    }

    private fun drawBarcode(canvas: Canvas, barcode: BarcodeConfig) {
        logPrint("drawBarcode: $barcode")
        if (barcode.data.isBlank()) return
        val widthPx = mmToPx(barcode.widthMm)
        val heightPx = mmToPx(barcode.heightMm)

        val bitmap = try {
            BarcodeGenerator.generate(barcode.data, barcode.format, widthPx, heightPx)
        } catch (e: Exception) {
            null
        } ?: return
        canvas.drawBitmap(bitmap, mmToPx(barcode.xMm).toFloat(), mmToPx(barcode.yMm).toFloat(), null)
    }
}