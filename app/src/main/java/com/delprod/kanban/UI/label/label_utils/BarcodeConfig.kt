package com.delprod.kanban.UI.label.label_utils

import com.google.zxing.BarcodeFormat

data class BarcodeConfig(
    var data: String,
    var xMm: Float,
    var yMm: Float,
    var widthMm: Float,
    var heightMm: Float,
    var format: BarcodeFormat = BarcodeFormat.CODE_128
)
