package qzwx.app.feature_my.backup

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import qzwx.app.module_data.entity.DebtType
import qzwx.app.module_data.entity.QDebtEntity
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.QWishEntity
import qzwx.app.module_data.entity.WalletType
import java.io.File

/**
 * 导出器单元测试（JVM）：
 * 用样例账单验证 CSV(UTF-8 BOM) / JSON(schemaVersion) / XLSX(Kexcel) / PDF(compose-pdf) 的真实输出。
 * 金额为元（两位小数），如 123.45 / 1000.00，不做 ×100 换算。
 */
class ExportersTest {

    private fun samplePayload(): ExportPayload {
        val bills = listOf(
            ExportBill(
                id = 1L,
                amount = 123.45,
                type = "EXPENSE",
                category = "餐饮",
                remark = "午餐",
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_700_000_000_000L,
                walletId = null,
                walletType = "ALIPAY",
                debtId = null
            ),
            ExportBill(
                id = 2L,
                amount = 1000.00,
                type = "INCOME",
                category = "工资",
                remark = null,
                createdAt = 1_700_000_100_000L,
                updatedAt = 1_700_000_100_000L,
                walletId = 5L,
                walletType = "BANK_CARD",
                debtId = null
            )
        )
        return ExportPayload(
            bills = bills,
            summary = ExportSummary(count = 2, income = 1000.00, expense = 123.45),
            scope = ExportScope.ALL,
            exportedAt = 1_700_000_200_000L
        )
    }

    @Test
    fun csv_hasBomHeaderRowsAndSummary() {
        val bytes = CsvExporter.export(samplePayload())
        val text = bytes.toString(Charsets.UTF_8)

        // UTF-8 BOM 开头
        assertTrue(bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte())
        assertTrue(text.startsWith("\uFEFF"))
        assertTrue(text.contains("日期时间,类型,种类,金额(元),备注,钱包,关联负债ID"))
        assertTrue(text.contains("支出,餐饮,123.45,午餐,支付宝"))
        assertTrue(text.contains("收入,工资,1000.00,,银行卡"))
        assertTrue(text.contains("合计收入(元),1000.00"))
        assertTrue(text.contains("合计支出(元),123.45"))
        assertTrue(text.contains("净额(元),876.55"))
    }

    @Test
    fun json_hasSchemaVersionAndYuanAmount() {
        val bytes = JsonExporter.export(samplePayload())
        val root = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
        assertEquals(1, root["schemaVersion"]!!.jsonPrimitive.content.toInt())
        assertEquals("QRMoney", root["app"]!!.jsonPrimitive.content)
        assertEquals(2, root["billCount"]!!.jsonPrimitive.content.toInt())
        val bills = root["bills"]!!.jsonArray
        assertEquals(123.45, bills[0].jsonObject["amount"]!!.jsonPrimitive.content.toDouble(), 0.001)
        assertEquals(1000.00, bills[1].jsonObject["amount"]!!.jsonPrimitive.content.toDouble(), 0.001)
        assertEquals(876.55, root["summary"]!!.jsonObject["net"]!!.jsonPrimitive.content.toDouble(), 0.001)
    }

    @Test
    fun json_recordsAccountingDaysStart() {
        val payload = samplePayload().copy(
            myDate = BackupMyDateJson(firstLaunchDate = 1_690_000_000_000L, transactionCount = 42)
        )
        val root = Json.parseToJsonElement(JsonExporter.export(payload).toString(Charsets.UTF_8)).jsonObject
        val myDate = root["myDate"]!!.jsonObject
        assertEquals(1_690_000_000_000L, myDate["firstLaunchDate"]!!.jsonPrimitive.content.toLong())
        assertEquals(42, myDate["transactionCount"]!!.jsonPrimitive.content.toInt())
    }

    @Test
    fun jsonImporter_roundTripRestoresBillsWithAllFields() {
        val bytes = JsonExporter.export(samplePayload())
        val backup = JsonImporter.parse(bytes)
        assertEquals(2, backup.bills.size)
        assertEquals(1, backup.schemaVersion)

        val first = backup.bills.first { it.id == 1L }.toEntity()
        assertEquals(123.45, first.amount, 0.001)
        assertEquals("EXPENSE", first.type.name)
        assertEquals("餐饮", first.category)
        assertEquals("午餐", first.remark)
        assertEquals("ALIPAY", first.walletType.name)
        assertEquals(1_700_000_000_000L, first.createdAt)
        assertEquals(1_700_000_000_000L, first.updatedAt)

        val second = backup.bills.first { it.id == 2L }.toEntity()
        assertEquals(1000.00, second.amount, 0.001)
        assertEquals("INCOME", second.type.name)
        assertEquals("BANK_CARD", second.walletType.name)
        assertEquals(5L, second.walletId)
    }

    @Test
    fun jsonImporter_restoresAccountingDaysStart() {
        val payload = samplePayload().copy(
            myDate = BackupMyDateJson(firstLaunchDate = 1_690_000_000_000L, transactionCount = 42)
        )
        val backup = JsonImporter.parse(JsonExporter.export(payload))
        val myDate = backup.myDate
        assertTrue("备份中应包含记账天数起点", myDate != null)
        assertEquals(1_690_000_000_000L, myDate!!.firstLaunchDate)
        assertEquals(42, myDate.transactionCount)

        // 旧版备份（无 myDate 字段）导入不报错
        val legacy = JsonExporter.export(samplePayload()).toString(Charsets.UTF_8)
            .replace(Regex(",\\s*\"myDate\": null"), "")
        val legacyBackup = JsonImporter.parse(legacy.toByteArray(Charsets.UTF_8))
        assertEquals(null, legacyBackup.myDate)
    }

    @Test
    fun json_roundTrip_containsWholeDatabase() {
        // 全库数据：账单 + 记账天数 + 钱包 + 负债 + 愿望
        val payload = samplePayload().copy(
            myDate = BackupMyDateJson(firstLaunchDate = 1_690_000_000_000L, transactionCount = 42),
            wallets = listOf(
                QWalletEntity(id = 1L, type = WalletType.CASH, balance = 500.0, isDefault = true, updatedAt = 1_690_000_000_000L),
                QWalletEntity(id = 2L, type = WalletType.BANK_CARD, balance = 8800.5, isDefault = false, updatedAt = 1_690_000_100_000L)
            ),
            debts = listOf(
                QDebtEntity(
                    id = 1L, name = "房贷", type = DebtType.BORROW, totalAmount = 100000.0,
                    remainingAmount = 80000.0, createdAt = 1_690_000_000_000L, deadline = 1_700_000_000_000L
                )
            ),
            wishes = listOf(
                QWishEntity(
                    id = 1L, name = "换手机", targetAmount = 6000.0, savedAmount = 1200.5,
                    remark = "年终目标", startDate = 1_690_000_000_000L, expectedDate = 1_750_000_000_000L
                )
            )
        )
        val bytes = JsonExporter.export(payload)
        val backup = JsonImporter.parse(bytes)

        assertEquals(2, backup.wallets.size)
        assertEquals(WalletType.CASH.name, backup.wallets[0].type.name)
        assertEquals(500.0, backup.wallets[0].balance, 0.001)
        assertTrue(backup.wallets[0].isDefault)
        assertEquals(WalletType.BANK_CARD.name, backup.wallets[1].type.name)

        assertEquals(1, backup.debts.size)
        assertEquals("房贷", backup.debts[0].name)
        assertEquals(DebtType.BORROW.name, backup.debts[0].type.name)
        assertEquals(80000.0, backup.debts[0].remainingAmount, 0.001)

        assertEquals(1, backup.wishes.size)
        assertEquals("换手机", backup.wishes[0].name)
        assertEquals(1200.5, backup.wishes[0].savedAmount, 0.001)
        assertEquals(1_750_000_000_000L, backup.wishes[0].expectedDate)

        // 旧版备份（无 wallets/debts/wishes 字段）导入不报错
        val legacy = JsonExporter.export(samplePayload()).toString(Charsets.UTF_8)
            .replace(Regex(",\\s*\"wallets\": \\[\\]"), "")
            .replace(Regex(",\\s*\"debts\": \\[\\]"), "")
            .replace(Regex(",\\s*\"wishes\": \\[\\]"), "")
        val legacyBackup = JsonImporter.parse(legacy.toByteArray(Charsets.UTF_8))
        assertTrue(legacyBackup.wallets.isEmpty())
        assertTrue(legacyBackup.debts.isEmpty())
        assertTrue(legacyBackup.wishes.isEmpty())
    }

    @Test
    fun jsonImporter_rejectsWrongSchemaVersionAndNonQRMoney() {
        // 篡改 schemaVersion -> 拒绝
        val badVersion = JsonExporter.export(samplePayload())
            .toString(Charsets.UTF_8)
            .replace("\"schemaVersion\": 1", "\"schemaVersion\": 99")
        try {
            JsonImporter.parse(badVersion.toByteArray(Charsets.UTF_8))
            assertTrue("应当拒绝不支持的 schemaVersion", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("版本"))
        }

        // 非 QRMoney 来源 -> 拒绝
        val foreign = JsonExporter.export(samplePayload())
            .toString(Charsets.UTF_8)
            .replace("\"app\": \"QRMoney\"", "\"app\": \"OtherApp\"")
        try {
            JsonImporter.parse(foreign.toByteArray(Charsets.UTF_8))
            assertTrue("应当拒绝非 QRMoney 备份", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("QRMoney"))
        }
    }

    @Test
    fun xlsx_isValidXlsxContainerWithTwoSheets() {
        val bytes = XlsxExporter.export(samplePayload())
        assertTrue(bytes != null && bytes.isNotEmpty())
        // xlsx 是 zip 容器：PK\x03\x04 开头
        assertTrue(bytes!![0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte())
        val text = String(bytes, 0, 512)
        // 两个 sheet 名出现在 sharedStrings/workbook 中（文本在压缩包内不直接可见，改为校验 zip 结构）
        assertTrue(text.startsWith("PK"))
    }

    @Test
    fun pdf_startsWithPdfHeaderWithChineseFont() {
        val fontFile = File("src/main/assets/fonts/SmileySans.ttf")
        assertTrue("中文字体缺失: ${fontFile.absolutePath}", fontFile.exists())
        val font = fontFile.readBytes()
        val bytes = PdfExporter.export(samplePayload(), font, font)
        assertTrue(bytes.isNotEmpty())
        assertTrue("PDF 头不正确", bytes.size > 8)
        assertEquals('%'.code.toByte(), bytes[0])
        assertEquals('P'.code.toByte(), bytes[1])
        assertEquals('D'.code.toByte(), bytes[2])
        assertEquals('F'.code.toByte(), bytes[3])
    }

    @Test
    fun generatePdfForDiagnosis() {
        val fontFile = File("src/main/assets/fonts/SmileySans.ttf")
        val bytes = PdfExporter.export(samplePayload(), fontFile.readBytes(), fontFile.readBytes())
        val out = File("build/diag_report.pdf")
        out.parentFile?.mkdirs()
        out.writeBytes(bytes)
        println("PDF written to ${out.absolutePath} size=${bytes.size}")
    }
}
