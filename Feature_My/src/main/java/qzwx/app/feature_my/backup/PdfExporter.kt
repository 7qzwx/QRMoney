package qzwx.app.feature_my.backup

import io.github.rikoappdev.composepdf.*

/**
 * PDF 导出器：使用 compose-pdf（compose-to-pdf），A4 多页报表。
 * 每页带 QRMoney 文字 Logo，标题 + 导出信息 + 收支汇总 + 账单明细表（自动分页、重复表头、页码）。
 * 金额直接输出两位小数（元）。字体由调用方提供（支持中文的 TrueType 字节）。
 */
object PdfExporter {

    private val BODY_STYLE = TextStyle(fontSize = 8.sp, color = Color(0xFF212529))
    private val HEADER_STYLE = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
    private val TITLE_STYLE = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
    private val SECTION_STYLE = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))

    /** QRMoney 品牌色 */
    private val BRAND_DARK = Color(0xFF1B5E20)
    private val BRAND_LIGHT = Color(0xFFE8F5E9)

    fun export(payload: ExportPayload, regularFontBytes: ByteArray, boldFontBytes: ByteArray): ByteArray {
        val summary = payload.summary
        val scopeText = if (payload.scope == ExportScope.ALL) "全部账单"
        else "自定义日期范围（${formatDate(payload.startMillis)} ~ ${formatDate(payload.endMillis)}）"

        val doc = pdfDocument(
            PageConfig(
                margin = 32.dp,
                pageNumbers = true,
                pageNumberFormat = { page, total -> "第 $page / $total 页" }
            )
        ) {
            // ===== 每页页眉：QRMoney 文字 Logo + 报表名 =====
            header {
                row {
                    cell(1f) {
                        row {
                            box(
                                padding = 3.dp,
                                background = BRAND_DARK,
                                cornerRadius = 3.dp
                            ) {
                                text("QRMoney", TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFFFF)))
                            }
                            spacer(6.dp)
                            text("账单导出报表", TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BRAND_DARK))
                        }
                    }
                }
                divider(color = BRAND_DARK, thickness = 1.dp)
            }

            // ===== 首页标题 =====
            row {
                text("七种文学旗下记账软件：QRMoney", TITLE_STYLE)
            }
            spacer(6.dp)
            keyValue("导出时间", formatTimestamp(payload.exportedAt))
            keyValue("导出范围", scopeText)
            keyValue("账单条数", "${summary.count} 条")

            spacer(10.dp)
            text("收支汇总（单位：元）", SECTION_STYLE)
            spacer(4.dp)
            box(padding = 8.dp, border = 1.dp, borderColor = Color(0xFFCCCCCC), background = BRAND_LIGHT) {
                row {
                    cell(1f) { text("收入：¥${yuanText(summary.income)}", TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))) }
                    cell(1f) { text("支出：¥${yuanText(summary.expense)}", TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))) }
                    cell(1f) { text("净额：¥${yuanText(summary.net)}", TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))) }
                }
            }

            spacer(12.dp)
            text("账单明细（金额单位：元）", SECTION_STYLE)
            spacer(4.dp)

            table(
                columns = listOf(
                    PdfColumn(3f, "日期时间"),
                    PdfColumn(1f, "类型", TextAlign.Center),
                    PdfColumn(2f, "种类"),
                    PdfColumn(2f, "金额(元)", TextAlign.End),
                    PdfColumn(3f, "备注"),
                    PdfColumn(1.5f, "钱包", TextAlign.Center)
                ),
                headerStyle = HEADER_STYLE,
                cellStyle = BODY_STYLE,
                zebra = Color(0xFFF5F7F5),
                headerBackground = BRAND_LIGHT
            ) {
                payload.bills.forEach { bill ->
                    row(
                        formatTimestamp(bill.createdAt),
                        if (bill.isExpense) "支出" else "收入",
                        bill.category,
                        yuanText(bill.amount),
                        bill.remark ?: "",
                        walletDisplayName(bill.walletType)
                    )
                }
                totalRow("收入合计(元)", "", "", yuanText(summary.income), "", "")
                totalRow("支出合计(元)", "", "", yuanText(summary.expense), "", "")
                totalRow("净额(元)", "", "", yuanText(summary.net), "", "")
            }

            // ===== 页脚：品牌信息 =====
            footer {
                divider(color = Color(0xFFDDDDDD), thickness = 1.dp)
                text(
                    "QRMoney · 数据本地备份导出 · ${formatTimestamp(payload.exportedAt)}",
                    TextStyle(fontSize = 7.sp, color = Color(0xFF9E9E9E))
                )
            }
        }
        return doc.render(regularFontBytes, boldFontBytes)
    }
}
