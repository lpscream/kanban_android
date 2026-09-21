package com.delprod.kanban.UI.label.label_utils

import android.graphics.Paint
import android.graphics.Typeface
import com.delprod.kanban.UI.label.LabelField
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object TsplBuilder {

    private const val LINE_SPACING_MULTIPLIER = 1.25f
    /** Примерная высота шрифта TSPL font "3" при множителе 1. Откалибруйте под свой принтер. */
    private const val BASE_FONT_HEIGHT_DOTS = 24

    /**
     * Режим CODEPAGE: текст — нативным шрифтом принтера (быстро),
     * штрихкод — точным растром через ZXing (гарантированный размер).
     */
    fun buildCodepageModeCommand(
        labelWidthMm: Float, labelHeightMm: Float,
        fields: List<LabelField>, barcode: BarcodeConfig?, qty: Int
    ): ByteArray {
        val cp1251 = Charset.forName("windows-1251")
        val output = ByteArrayOutputStream()

        output.write(buildString {
            append("SIZE ${labelWidthMm.toInt()} mm, ${labelHeightMm.toInt()} mm\r\n")
            append("GAP 2 mm, 0 mm\r\n")
            append("DIRECTION 1\r\n")
            append("CODEPAGE 1251\r\n")
            append("CLS\r\n")
        }.toByteArray(cp1251))

        for (field in fields) {
            if (field.text.isBlank()) continue
            output.write(buildFieldTextCommands(field).toByteArray(cp1251))
        }

        barcode?.let {
            if (it.data.isNotBlank()) {
                val widthPx = LabelRenderer.mmToPx(it.widthMm)
                val heightPx = LabelRenderer.mmToPx(it.heightMm)
                val bmp = try { BarcodeGenerator.generate(it.data, it.format, widthPx, heightPx) } catch (e: Exception) { null }
                bmp?.let { bitmap ->
                    output.write(TsplBitmapUtil.bitmapToTsplCommand(
                        bitmap, LabelRenderer.mmToPx(it.xMm), LabelRenderer.mmToPx(it.yMm)
                    ))
                    output.write("\r\n".toByteArray(Charsets.US_ASCII))
                }
            }
        }

        output.write("PRINT $qty,1\r\n".toByteArray(Charsets.US_ASCII))
        return output.toByteArray()
    }

    private fun buildFieldTextCommands(field: LabelField): String {
        val paint = Paint().apply {
            textSize = LabelRenderer.mmToPx(field.fontSizeMm).toFloat()
            typeface = if (field.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        val widthPx = LabelRenderer.mmToPx(field.widthMm).toFloat()
        val heightPx = LabelRenderer.mmToPx(field.heightMm).toFloat()
        val lineHeightPx = paint.textSize * LINE_SPACING_MULTIPLIER

        val wrapped = TextWrapUtil.wrapTextByWidth(paint, field.text, widthPx)
        val clipped = TextWrapUtil.clipLinesByHeight(wrapped, lineHeightPx, heightPx)

        val multiplier = (paint.textSize / BASE_FONT_HEIGHT_DOTS).toInt().coerceIn(1, 10)
        val xDots = LabelRenderer.mmToPx(field.xMm)
        var yDots = LabelRenderer.mmToPx(field.yMm)
        val lineHeightDots = lineHeightPx.toInt()

        val sb = StringBuilder()
        for (line in clipped) {
            val escaped = line.replace("\"", "'")
            sb.append("TEXT $xDots,$yDots,\"3\",0,$multiplier,$multiplier,\"$escaped\"\r\n")
            yDots += lineHeightDots
        }
        return sb.toString()
    }

    /** Режим BITMAP: вся этикетка (текст+штрихкод) — единое изображение. */
    fun buildBitmapModeCommand(
        labelWidthMm: Float, labelHeightMm: Float,
        fields: List<LabelField>, barcode: BarcodeConfig?, qty: Int
    ): ByteArray {
        val bitmap = LabelRenderer.renderLabel(labelWidthMm, labelHeightMm, fields, barcode)

        val header = buildString {
            append("SIZE ${labelWidthMm.toInt()} mm, ${labelHeightMm.toInt()} mm\r\n")
            append("GAP 2 mm, 0 mm\r\n")
            append("DIRECTION 1\r\n")
            append("CLS\r\n")
        }

        val output = ByteArrayOutputStream()
        output.write(header.toByteArray(Charsets.US_ASCII))
        output.write(TsplBitmapUtil.bitmapToTsplCommand(bitmap, 0, 0))
        output.write("\r\nPRINT $qty,1\r\n".toByteArray(Charsets.US_ASCII))
        return output.toByteArray()
    }



}