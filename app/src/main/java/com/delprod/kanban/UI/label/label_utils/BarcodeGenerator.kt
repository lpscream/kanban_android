package com.delprod.kanban.UI.label.label_utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object BarcodeGenerator {

    /**
     * Генерирует монохромный Bitmap штрихкода/QR-кода точного размера
     * widthPx x heightPx. Для линейных штрихкодов (CODE_128, EAN_13 и т.д.)
     * высота растягивается на весь heightPx (полосы), для QR — рекомендуется
     * квадратное соотношение width == height.
     */
    fun generate(data: String, format: BarcodeFormat, widthPx: Int, heightPx: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 0,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        val safeWidth = widthPx.coerceAtLeast(10)
        val safeHeight = heightPx.coerceAtLeast(10)

        val bitMatrix: BitMatrix = MultiFormatWriter().encode(data, format, safeWidth, safeHeight, hints)

        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        for (x in 0 until safeWidth) {
            for (y in 0 until safeHeight) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}