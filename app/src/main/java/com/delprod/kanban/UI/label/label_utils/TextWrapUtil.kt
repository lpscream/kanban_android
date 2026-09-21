package com.delprod.kanban.UI.label.label_utils

import android.graphics.Paint
import org.apache.poi.sl.usermodel.TextParagraph

object TextWrapUtil {

    /**
     * Разбивает текст на строки так, чтобы каждая помещалась в maxWidthPx
     * при заданном Paint. Слово шире доступной ширины режется по символам,
     * чтобы избежать зависания.
     */
    fun wrapTextByWidth(paint: Paint, text: String, maxWidthPx: Float): List<String> {
        if (text.isBlank()) return emptyList()
        if (maxWidthPx <= 0) return listOf(text)

        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidthPx) {
                currentLine = StringBuilder(candidate)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }
                if (paint.measureText(word) > maxWidthPx) {
                    var remaining = word
                    while (paint.measureText(remaining) > maxWidthPx && remaining.length > 1) {
                        var cut = remaining.length
                        while (cut > 1 && paint.measureText(remaining.substring(0, cut)) > maxWidthPx) cut--
                        lines.add(remaining.substring(0, cut))
                        remaining = remaining.substring(cut)
                    }
                    currentLine = StringBuilder(remaining)
                } else {
                    currentLine = StringBuilder(word)
                }
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    /** Обрезает строки по высоте поля, добавляя многоточие если не влезли все. */
    fun clipLinesByHeight(lines: List<String>, lineHeightPx: Float, maxHeightPx: Float): List<String> {
        if (lineHeightPx <= 0) return lines
        val maxLines = (maxHeightPx / lineHeightPx).toInt().coerceAtLeast(1)
        if (lines.size <= maxLines) return lines

        val clipped = lines.take(maxLines).toMutableList()
        val last = clipped.last()
        clipped[clipped.lastIndex] = if (last.length > 1) last.dropLast(1) + "…" else "…"
        return clipped
    }

    fun resolveLineX(
        paint: Paint,
        line: String,
        baseX: Float,
        fieldWidthPx: Float,
        align: TextParagraph.TextAlign
    ): Float {
        val lineWidth = paint.measureText(line)
        return when (align) {
           TextParagraph.TextAlign.LEFT -> baseX
           TextParagraph.TextAlign.CENTER -> baseX + (fieldWidthPx - lineWidth) / 2f
           TextParagraph.TextAlign.RIGHT -> baseX + (fieldWidthPx - lineWidth)
            TextParagraph.TextAlign.JUSTIFY -> baseX
            TextParagraph.TextAlign.JUSTIFY_LOW -> baseX
            TextParagraph.TextAlign.DIST -> baseX
            TextParagraph.TextAlign.THAI_DIST -> baseX
        }.coerceAtLeast(baseX) // не даём уйти левее границы поля, если строка шире поля
    }
}