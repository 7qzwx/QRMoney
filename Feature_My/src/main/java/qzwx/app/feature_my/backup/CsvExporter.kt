package qzwx.app.feature_my.backup

/**
 * CSV 导出器：用户分析用，UTF-8 BOM。
 * 列：日期时间, 类型, 种类, 金额(元), 备注, 钱包, 关联负债ID
 * 金额直接输出两位小数（元），不做 ×100 换算。
 */
object CsvExporter {

    private val HEADER = listOf("日期时间", "类型", "种类", "金额(元)", "备注", "钱包", "关联负债ID")

    fun export(payload: ExportPayload): ByteArray {
        val sb = StringBuilder()
        // UTF-8 BOM：'\uFEFF' 编码后为 EF BB BF，Excel 打开不乱码
        sb.append('\uFEFF')
        sb.appendLine(HEADER.joinToString(",") { escapeCsv(it) })

        payload.bills.forEach { bill ->
            sb.appendLine(
                listOf(
                    formatTimestamp(bill.createdAt),
                    if (bill.isExpense) "支出" else "收入",
                    bill.category,
                    yuanText(bill.amount),
                    bill.remark ?: "",
                    walletDisplayName(bill.walletType),
                    bill.debtId?.toString() ?: ""
                ).joinToString(",") { escapeCsv(it) }
            )
        }

        // 汇总行（金额单位：元，两位小数）
        sb.appendLine()
        sb.appendLine(",,合计收入(元),${yuanText(payload.summary.income)},合计支出(元),${yuanText(payload.summary.expense)}")
        sb.appendLine(",,净额(元),${yuanText(payload.summary.net)}")
        sb.appendLine(",,导出时间,${formatTimestamp(payload.exportedAt)}")

        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    /** CSV 字段转义：含逗号/引号/换行时用引号包裹，内部引号翻倍 */
    private fun escapeCsv(value: String): String {
        val needQuote = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needQuote) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }
}
