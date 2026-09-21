package com.delprod.kanban.UI.label.label_utils

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream

object TsplBitmapUtil {




    /**
     * @param invert если true — меняет полярность бит (для принтеров/клонов,
     * которые трактуют BITMAP-данные наоборот: 0=чёрный, 1=белый вместо стандарта TSC).
     */
    fun bitmapToTsplCommand(bitmap: Bitmap, xDots: Int, yDots: Int, invert: Boolean = true): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8
        val imageData = ByteArray(widthBytes * height)

        for (row in 0 until height) {
            for (col in 0 until width) {
                val isBlack = isPixelBlack(bitmap.getPixel(col, row))
                // XOR с invert: если invert=true, логика переворачивается
                val shouldSetBit = isBlack != invert
                if (shouldSetBit) {
                    val byteIndex = row * widthBytes + (col / 8)
                    val bitIndex = 7 - (col % 8)
                    imageData[byteIndex] = (imageData[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                }
            }
        }

        val header = "BITMAP $xDots,$yDots,$widthBytes,$height,0,"
        val output = ByteArrayOutputStream()
        output.write(header.toByteArray(Charsets.US_ASCII))
        output.write(imageData)
        return output.toByteArray()
    }

    private fun isPixelBlack(pixel: Int): Boolean {
        if (Color.alpha(pixel) < 128) return false
        val luma = 0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)
        return luma < 128
    }
//    fun bitmapToTsplCommand(bitmap: Bitmap, xDots: Int, yDots: Int): ByteArray {
//        val width = bitmap.width
//        val height = bitmap.height
//        val widthBytes = (width + 7) / 8
//        val imageData = ByteArray(widthBytes * height)
//
//        for (row in 0 until height) {
//            for (col in 0 until width) {
//                if (isPixelBlack(bitmap.getPixel(col, row))) {
//                    val byteIndex = row * widthBytes + (col / 8)
//                    val bitIndex = 7 - (col % 8)
//                    imageData[byteIndex] = (imageData[byteIndex].toInt() or (1 shl bitIndex)).toByte()
//                }
//            }
//        }
//
//        val header = "BITMAP $xDots,$yDots,$widthBytes,$height,0,"
//        val output = ByteArrayOutputStream()
//        output.write(header.toByteArray(Charsets.US_ASCII))
//        output.write(imageData)
//        return output.toByteArray()
//    }
//
//    private fun isPixelBlack(pixel: Int): Boolean {
//        if (Color.alpha(pixel) < 128) return false
//        val luma = 0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)
//        return luma < 128
//    }
}