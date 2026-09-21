package com.delprod.kanban.data

import java.time.LocalDate

/**
 *
 * One "стовпчикова група" (2 колонки: замовлено/виконано) для конкретної дати.
 * Кожна дата може мати декілька підрозділів.
 */
data class Subdivision(
    val code: String,          // напр. "А2896"
    val fullLabel: String,     // напр. "Підрозділ А2896 - 1"
    val region: String,        // напр. "Одеська обл., Подільський р-н., Кодимська ОТГ"
    val responsiblePerson: String,
    val phone: String,
    val unitId: String,        // числовой идентификатор с строки 5 (напр. "248666")
    val category: String       // напр. "особовий склад"
)

data class NomenclatureItem(
    val code: String,   // напр. "12033"
    val name: String,   // напр. "Сало свіже"
    val unit: String    // напр. "кг"
)

data class OrderLine(
    val date: LocalDate,
    val subdivision: Subdivision,
    val nomenclature: NomenclatureItem,
    val quantityOrdered: Int?,
    val quantityExecuted: Int?,
    val boxSum: Int?,
    val datesList: String?,
    val uuid: String
)

/**
 * Группировка по датам - именно это показываем пользователю как чекбоксы при импорте
 * */
data class DateGroup(
    val date: LocalDate,
    val subdivisions: List<Subdivision>
)

/**
 * Полный результат парсинга одного файла
 * */
data class FoodOrderReport(
    val sourceFileName: String,
    val dateGroups: List<DateGroup>,
    val lines: List<OrderLine>
) {
    val availableDates: List<LocalDate> get() = dateGroups.map { it.date }.distinct().sorted()

    fun filterByDates(selectedDates: Set<LocalDate>): List<OrderLine> =
        lines.filter { it.date in selectedDates }
}
