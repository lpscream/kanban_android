package com.delprod.kanban.parser

import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.DateGroup
import com.delprod.kanban.data.FoodOrderReport
import com.delprod.kanban.data.NomenclatureItem
import com.delprod.kanban.data.OrderLine
import com.delprod.kanban.data.Subdivision
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.InputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Парсер под конкретный формат выгрузки из 1С ("Дел_8_3.xls"):
 *
 * Строка 0: дата (одна на группу колонок) ИЛИ код подразделения (напр. "А2896")
 * Строка 1: адрес/регион подразделения (пусто для "итоговых" колонок)
 * Строка 2: "Подраздел <код> - <N>"
 * Строка 3: ответственное лицо
 * Строка 4: телефон
 * Строка 5: числовой id
 * Строка 6: категория (напр. "особовий склад")
 * Строка 7-8: заголовки "Кількість" / "замовлено"/"виконано"
 * Строка 9+: товарные строки, колонка A имеет формат "код      , наименование, единица"
 * Последняя строка ("Вместе") и колонки без адреса в строке 1 — итоговые, игнорируются.
 */
object FoodOrderXlsParser {

    private const val HEADER_ROW_DATE = 0
    private const val HEADER_ROW_REGION = 1
    private const val HEADER_ROW_LABEL = 2
    private const val HEADER_ROW_PERSON = 3
    private const val HEADER_ROW_PHONE = 4
    private const val HEADER_ROW_UNIT_ID = 5
    private const val HEADER_ROW_CATEGORY = 6
    private const val HEADER_ROW_QTY_LABEL = 8
    private const val FIRST_DATA_ROW = 9

    private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val nomenclatureRegex = Regex("^\\s*(\\S+)\\s*,\\s*(.+?)\\s*,\\s*([^,]+)\\s*$")

    fun parse(input: InputStream, fileName: String): FoodOrderReport {
        logPrint(input.toString())
        HSSFWorkbook(input).use { wb: Workbook ->
            val sheet: Sheet = wb.getSheetAt(0)
            return parseSheet(sheet, fileName)
        }
    }

    private fun parseSheet(sheet: Sheet, fileName: String): FoodOrderReport {
        val headerRows = (0..HEADER_ROW_QTY_LABEL).map { sheet.getRow(it) }
        val row0 = headerRows[HEADER_ROW_DATE]
        val row1 = headerRows[HEADER_ROW_REGION]
        val row2 = headerRows[HEADER_ROW_LABEL]
        val row3 = headerRows[HEADER_ROW_PERSON]
        val row4 = headerRows[HEADER_ROW_PHONE]
        val row5 = headerRows[HEADER_ROW_UNIT_ID]
        val row6 = headerRows[HEADER_ROW_CATEGORY]
        val row8 = headerRows[HEADER_ROW_QTY_LABEL]

        val maxCol = row8?.lastCellNum?.toInt() ?: 0

        // 1. Определяем блоки колонок (заказно/выполнено) принадлежащих реальным подразделениям.
        data class Block(val orderedCol: Int, val executedCol: Int, val date: LocalDate, val subdivision: Subdivision, val uuid: String)

        val blocks = mutableListOf<Block>()
        var currentDate: LocalDate? = null

        for (c in 0 until maxCol) {
            // Дата может встретиться в строке 0 в любой колонке - обновляем "текущую" дату
            parseDateCell(row0?.getCell(c))?.let { currentDate = it }

            val qtyLabel = textOf(row8?.getCell(c))
            if (qtyLabel.equals("замовлено", ignoreCase = true)) {
                val executedCol = c + 1
                val region = textOf(row1?.getCell(c))
                if (region.isNotBlank() && currentDate != null) {
                    // Реальное подразделение (в итоговых колонках строка 1 пуста)
                    val code = textOf(row0?.getCell(c)).ifBlank { textOf(row2?.getCell(c)) }
                    val subdivision = Subdivision(
//                        code = code,
                        code = "${textOf(row2?.getCell(c))}, ($code), ${row5?.getCell(c)}",
                        fullLabel = textOf(row2?.getCell(c)),
                        region = region,
                        responsiblePerson = textOf(row3?.getCell(c)),
                        phone = textOf(row4?.getCell(c)),
                        unitId = textOf(row5?.getCell(c)),
                        category = textOf(row6?.getCell(c))
                    )
                    logPrint("subdivision: ${subdivision}")

                    logPrint("currentDate: ${currentDate}")
                    blocks += Block(c, executedCol, currentDate, subdivision, UUID.randomUUID().toString())
                }
                // если region пустой — это итоговая/"Итого" колонка, пропускаем
            }
        }

        // 2. Проходим товарные строки
        val lines = mutableListOf<OrderLine>()
        var r = FIRST_DATA_ROW
        while (r <= sheet.lastRowNum) {

            val row = sheet.getRow(r)
            r++
            val firstCellText = textOf(row?.getCell(0))
            if (firstCellText.equals("Разом", ignoreCase = true)) continue
            val match = nomenclatureRegex.find(firstCellText) ?: continue // пропускаємо не товарні рядки

            val nomenclature = NomenclatureItem(
                code = match.groupValues[1].trim(),
                name = match.groupValues[2].trim(),
                unit = match.groupValues[3].trim()
            )

            logPrint("nomenclature: ${nomenclature}")

            for (block in blocks) {
                val ordered = numberOf(row?.getCell(block.orderedCol))
                val executed = numberOf(row?.getCell(block.executedCol))
                if (ordered == null && executed == null) continue // немає даних по цьому підрозділу — пропускаємо

                lines += OrderLine(
                    date = block.date,
                    subdivision = block.subdivision,
                    nomenclature = nomenclature,
                    quantityOrdered = ordered,
                    quantityExecuted = executed,
                    boxSum = null,
                    datesList = null,
                    uuid = block.uuid
                )

                logPrint("lines: ${lines}")
            }
        }

        val dateGroups = blocks.groupBy { it.date }
            .map { (date, blocksForDate) ->
                DateGroup(
                    date,
                    blocksForDate.map { it.subdivision }.distinct()
                )
            }
            .sortedBy { it.date }
        blocks.forEach { logPrint("block: ${it.toString()}") }
        return FoodOrderReport(sourceFileName = fileName, dateGroups = dateGroups, lines = lines)
    }

    private fun textOf(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.localDateTimeCellValue.toLocalDate().format(dateFmt)
                } else {
                    val d = cell.numericCellValue
                    if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
                }
            }
            else -> cell.toString().trim()
        }
    }

    private fun numberOf(cell: Cell?): Int? {
        if (cell == null) return null
        val kg: Double? = when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.STRING -> cell.stringCellValue.trim().replace(',', '.').toDoubleOrNull()
            CellType.FORMULA -> runCatching { cell.numericCellValue }.getOrNull()
            else -> null
        }
        return kg?.let { Math.round(it * 1000.0).toInt() }
    }

    private fun parseDateCell(cell: Cell?): LocalDate? {
        if (cell == null) return null
        return when (cell.cellType) {
            CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) {
                cell.dateCellValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            } else null
            CellType.STRING -> {
                val text = cell.stringCellValue.trim()
                logPrint("text: ${text}")
                val datePart = text.substringBefore(' ') // відкидаємо "00:00:00"
//                logPrint("date parse: ${LocalDate.parse(datePart, dateFmt)}")
                runCatching { LocalDate.parse(datePart, dateFmt) }.getOrNull()
            }
            else -> null
        }
    }
}
