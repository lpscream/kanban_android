package com.delprod.kanban.data.goods

import kotlinx.serialization.Serializable

@Serializable
data class ProductModel(
    var article: Int = 0,
    var name: String = "",
    var weight: Int = 0,
    var dateList: List<String> = emptyList(),
    var nomenclatureCode: Int = 0,
    var boxQuantity: Int = 0
)



@Serializable
data class Order(
    var sumBoxQuantity: Int = 0,
    var sumWeight: Int = 0,
    var client: String? = "",
    var comment: String? = "",
    var date: String = "",
    var goods: List<ProductModel> = mutableListOf()
)


data class BarcodeData(
    val barIdCodeType: BarcodeIdType?,
    val article: Int,
    val weightKg: Int,
    val date: String
)


enum class BarcodeIdType(val type: Int){
    PLU(27), ARTICLE(25), UNKNOWN(0)
}