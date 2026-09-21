package com.delprod.kanban.data.models

data class ProductListItem(
    val id: Long,
    val code: String,
    val name: String,
    val unit: String,
    val plu: String?,
    var quantityOrdered: String?
)