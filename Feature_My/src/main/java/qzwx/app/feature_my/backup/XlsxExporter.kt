package qzwx.app.feature_my.backup

import com.gyanoba.kexcel.Excel
import com.gyanoba.kexcel.number_format.NumFormat
import com.gyanoba.kexcel.sheet.CellIndex
import com.gyanoba.kexcel.sheet.CellStyle
import com.gyanoba.kexcel.sheet.DoubleCellValue
import com.gyanoba.kexcel.sheet.TextCellValue
import com.gyanoba.kexcel.utils.ExcelColor
import com.gyanoba.kexcel.utils.HorizontalAlign
import com.gyanoba.kexcel.utils.VerticalAlign

/**
 * XLSX 导出器：使用 Kexcel 库，生成「账单明细」+「汇总」两个 Sheet。
 * 金额直接输出数据库原值（元），单元格格式为两位小数（0.00），不做 ×100 换算。
 */
object XlsxExporter {

    private val HEADERS = listOf("日期时间", "类型", "种类", "金额(元)", "备注", "钱包", "关联负债ID")

    /** 两位小数数值格式 */
    private val moneyStyle = CellStyle(numberFormat = NumFormat.standard_2)

    fun export(payload: ExportPayload): ByteArray? {
        val excel = Excel.createExcel()
        val detail = excel["账单明细"]
        val summarySheet = excel["汇总"]

        val headerStyle = CellStyle(
            bold = true,
            fontSize = 12,
            fontColorHex = ExcelColor.fromHexString("FF1B5E20"),
            backgroundColorHex = ExcelColor.fromHexString("FFE8F5E9"),
            horizontalAlign = HorizontalAlign.Center,
            verticalAlign = VerticalAlign.Center
        )

        // ===== Sheet1：账单明细 =====
        HEADERS.forEachIndexed { col, header ->
            detail.updateCell(
                CellIndex.indexByColumnRow(col, 0),
                TextCellValue(header),
                cellStyle = headerStyle
            )
            detail.setColumnWidth(col, when (col) {
                0 -> 22.0   // 日期时间
                1 -> 10.0   // 类型
                2 -> 14.0   // 种类
                3 -> 14.0   // 金额(元)
                4 -> 20.0   // 备注
                5 -> 12.0   // 钱包
                else -> 14.0
            })
        }

        payload.bills.forEachIndexed { index, bill ->
            val row = index + 1
            detail.updateCell(CellIndex.indexByColumnRow(0, row), TextCellValue(formatTimestamp(bill.createdAt)))
            detail.updateCell(CellIndex.indexByColumnRow(1, row), TextCellValue(if (bill.isExpense) "支出" else "收入"))
            detail.updateCell(CellIndex.indexByColumnRow(2, row), TextCellValue(bill.category))
            detail.updateCell(CellIndex.indexByColumnRow(3, row), DoubleCellValue(bill.amount), cellStyle = moneyStyle)
            detail.updateCell(CellIndex.indexByColumnRow(4, row), TextCellValue(bill.remark ?: ""))
            detail.updateCell(CellIndex.indexByColumnRow(5, row), TextCellValue(walletDisplayName(bill.walletType)))
            detail.updateCell(CellIndex.indexByColumnRow(6, row), TextCellValue(bill.debtId?.toString() ?: ""))
        }

        // 明细尾部汇总行
        val sumRow = payload.bills.size + 1
        detail.updateCell(CellIndex.indexByColumnRow(0, sumRow), TextCellValue("汇总"), cellStyle = headerStyle)
        detail.updateCell(CellIndex.indexByColumnRow(2, sumRow), TextCellValue("收入合计(元)"), cellStyle = headerStyle)
        detail.updateCell(CellIndex.indexByColumnRow(3, sumRow), DoubleCellValue(payload.summary.income), cellStyle = moneyStyle)
        detail.updateCell(CellIndex.indexByColumnRow(5, sumRow), TextCellValue("支出合计(元)"), cellStyle = headerStyle)
        detail.updateCell(CellIndex.indexByColumnRow(6, sumRow), DoubleCellValue(payload.summary.expense), cellStyle = moneyStyle)

        // ===== Sheet2：汇总 =====
        val keyStyle = CellStyle(bold = true, fontSize = 12)
        summarySheet.updateCell(CellIndex.indexByColumnRow(0, 0), TextCellValue("QRMoney 账单导出汇总"), cellStyle = keyStyle)
        summarySheet.setColumnWidth(0, 26.0)
        summarySheet.setColumnWidth(1, 40.0)

        fun keyValueRow(row: Int, key: String, value: String, valueStyle: CellStyle? = null) {
            summarySheet.updateCell(CellIndex.indexByColumnRow(0, row), TextCellValue(key), cellStyle = keyStyle)
            summarySheet.updateCell(CellIndex.indexByColumnRow(1, row), TextCellValue(value), cellStyle = valueStyle)
        }

        keyValueRow(2, "导出时间", formatTimestamp(payload.exportedAt))
        keyValueRow(3, "导出范围", if (payload.scope == ExportScope.ALL) "全部账单" else "自定义日期范围")
        keyValueRow(4, "起始日期", formatDate(payload.startMillis) ?: "-")
        keyValueRow(5, "结束日期", formatDate(payload.endMillis) ?: "-")
        keyValueRow(6, "账单条数", payload.summary.count.toString())
        keyValueRow(7, "收入合计(元)", yuanText(payload.summary.income), moneyStyle)
        keyValueRow(8, "支出合计(元)", yuanText(payload.summary.expense), moneyStyle)
        keyValueRow(9, "净额(元)", yuanText(payload.summary.net), moneyStyle)
        keyValueRow(10, "金额单位", "元（两位小数）")

        excel.setDefaultSheet("账单明细")
        return excel.encode()
    }
}
