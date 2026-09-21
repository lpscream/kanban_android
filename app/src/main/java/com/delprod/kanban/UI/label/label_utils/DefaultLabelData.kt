package com.delprod.kanban.UI.label.label_utils

import com.delprod.kanban.UI.label.LabelField
import org.apache.poi.sl.usermodel.TextParagraph


object DefaultLabelData {

    const val DEFAULT_LABEL_WIDTH_MM = 58f
    const val DEFAULT_LABEL_HEIGHT_MM = 60f

    fun createDefaultFields(): MutableList<LabelField> = mutableListOf(
        LabelField(
            "name",
            "Наименование",
            "",
            "Свинина, грудина без кістки, заморожена(блоки)",
            2f,
            0.5f,
            54f,
            8f,
            fontSizeMm = 2.5f,
            bold = true,
            align = TextParagraph.TextAlign.CENTER
        ),
        LabelField(
            "dstu",
            "ДСТУ",
            "",
            "ДСТУ 4590:2006",
            2f,
            6f,
            54f,
            8f,
            fontSizeMm = 2.5f,
            bold = true,
            align = TextParagraph.TextAlign.CENTER
        ),
        LabelField(
            "address",
            "Адрес производства",
            "",
            "Країна виробник : Україна, ТОВ \"ДЕЛ-ПРОД\" вул. Харківська, буд. 32, м. Суми, Сумська область, 40016 Виробничi потужн.: 19-км Старокиївської дор., буд.№ 31, м.Одеса, Одеська область, Україна тел. (063)034-84-49",
            2f,
            9f,
            54f,
            6f,
            fontSizeMm = 2.0f
        ),
        LabelField(
            "nutrition",
            "Пищевая ценность",
            "",
            "Харчова цiннiсть на 100 г продукту: Бiлки –15.64 г, Жири – 23,41 г Енергетична цінність 338ккал  на 100 г продукту.",
            2f,
            18f,
            54f,
            8f,
            fontSizeMm = 2.0f
        ),
        LabelField(
            "storage",
            "Условия хранения",
            "",
            "Зберiгати при темп. не вище -1 до +1°С, не більше 8 діб",
            2f,
            22.5f,
            54f,
            6f,
            fontSizeMm = 2.0f
        ),
        LabelField(
            "permit",
            "Эксплуатационное разрешение",
            "",
            "Експл.дозвіл №51-10-16 від 24.07.2023р. ISO 22000:2018 №UA.FS.230626.01-23  від 26.07.23 р.",
            2f,
            25f,
            54f,
            6f,
            fontSizeMm = 2.0f
        ),
        LabelField(
            "note",
            "Примечание",
            "",
            "Номер партії співпадає з датою заморозки",
            2f,
            41f,
            54f,
            6f,
            fontSizeMm = 2.0f,
            align = TextParagraph.TextAlign.CENTER
        ),
        LabelField(
            "mfgDate",
            "Дата изготовления",
            "",
            "01.08.26",
            2f,
            46f,
            26f,
            5f,
            fontSizeMm = 5.0f,
            bold = true
        ),
        LabelField(
            "mfgDateSign",
            "Подпись даты изготовления",
            "",
            "Дата виг./замор.:",
            2f,
            43f,
            26f,
            5f,
            fontSizeMm = 3.0f
        ),
        LabelField(
            "expDate",
            "Срок годности до",
            "",
            "01.02.27",
            30f,
            46f,
            26f,
            5f,
            fontSizeMm = 5.0f,
            bold = true
        ),
        LabelField(
            "expDateSign",
            "Подпись срока годности",
            "",
            "Вжити до:",
            30f,
            43f,
            26f,
            5f,
            fontSizeMm = 3.0f
        ),
        LabelField("expDays", "Время хранения в днях", "", "8", 0f, 0f, 0f, 0f, fontSizeMm = 0f),
        LabelField("weight", "Вес", "", "12.500 кг", 30f, 52f, 26f, 5f, fontSizeMm = 5.5f, bold = true),
        LabelField(
            "weightSign",
            "Подпись массы",
            "",
            "Маса нетто:",
            2f,
            53f,
            26f,
            5f,
            fontSizeMm = 4.0f
        ),
        LabelField(
            "barCodeDigits",
            "Штрих-код ц.часть",
            "",
            "250000112940290626",
            2f,
            37f,
            54f,
            5f,
            fontSizeMm = 4.0f,
            align = TextParagraph.TextAlign.CENTER,
            bold = true
        ),
        LabelField(
            "barCodePic",
            "Штрих-код гр.часть",
            "",
            "250000112940290626",
            2f,
            30f,
            54f,
            8f,
            fontSizeMm = 4.0f,
            align = TextParagraph.TextAlign.CENTER,
            bold = true
        ),
        LabelField(
            "additionalField_1",
            "Дополнительное поле 1",
            "",
            "Доп. поле",
            0f,
            0f,
            0f,
            0f,
            fontSizeMm = 0.0f,
            align = TextParagraph.TextAlign.LEFT,
            bold = false
        ),
        LabelField(
            "additionalField_2",
            "Дополнительное поле 2",
            "",
            "Доп. поле",
            0f,
            0f,
            0f,
            0f,
            fontSizeMm = 0.0f,
            align = TextParagraph.TextAlign.LEFT,
            bold = false
        ),
        LabelField(
            "additionalField_3",
            "Дополнительное поле 3",
            "",
            "Доп. поле",
            0f,
            0f,
            0f,
            0f,
            fontSizeMm = 0.0f,
            align = TextParagraph.TextAlign.LEFT,
            bold = false
        ),
        LabelField(
            "additionalField_4",
            "Дополнительное поле 4",
            "",
            "Доп. поле",
            0f,
            0f,
            0f,
            0f,
            fontSizeMm = 0.0f,
            align = TextParagraph.TextAlign.LEFT,
            bold = false
        ),
        LabelField(
            "additionalField_5",
            "Дополнительное поле 5",
            "",
            "Доп. поле",
            0f,
            0f,
            0f,
            0f,
            fontSizeMm = 0.0f,
            align = TextParagraph.TextAlign.LEFT,
            bold = false
        )
    )

    fun createDefaultBarcode(): BarcodeConfig = BarcodeConfig(
        data = "250000112940290626",
        xMm = 2f, yMm = 29f, widthMm = 54f, heightMm = 8f
    )
}