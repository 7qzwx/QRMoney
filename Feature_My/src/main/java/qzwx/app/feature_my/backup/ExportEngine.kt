package qzwx.app.feature_my.backup

import android.content.Context

/**
 * 导出引擎：按格式统一生成备份文件字节。
 * PDF 需要中文字体（assets/fonts/SmileySans.ttf，得意黑，OFL 开源，含 ASCII 与中文）。
 */
object ExportEngine {

    data class PdfFonts(val regular: ByteArray, val bold: ByteArray)

    /** 加载 PDF 中文字体；regular/bold 共用同一份（该字体无粗体表，标题用字号区分层级） */
    fun loadPdfFonts(context: Context): PdfFonts {
        val bytes = context.assets.open("fonts/SmileySans.ttf").use { it.readBytes() }
        return PdfFonts(regular = bytes, bold = bytes)
    }

    /** 生成对应格式的备份文件字节 */
    fun build(format: ExportFormat, payload: ExportPayload, fonts: PdfFonts?): ByteArray = when (format) {
        ExportFormat.CSV -> CsvExporter.export(payload)
        ExportFormat.JSON -> JsonExporter.export(payload)
        ExportFormat.XLSX -> XlsxExporter.export(payload) ?: error("Excel 文件生成失败")
        ExportFormat.PDF -> {
            val pdfFonts = fonts ?: error("PDF 字体未加载")
            PdfExporter.export(payload, pdfFonts.regular, pdfFonts.bold)
        }
    }
}
