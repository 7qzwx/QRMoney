package qzwx.app.feature_my.backup

import kotlinx.serialization.Serializable
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_data.entity.QDebtEntity
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.QWishEntity
import qzwx.app.module_data.entity.WalletType
import java.util.Locale
import kotlin.math.roundToLong

/** 备份 JSON 的 schema 版本，未来恢复解析时据此做兼容判断 */
const val BACKUP_SCHEMA_VERSION: Int = 1

/** 导出格式（与 QRMoney 导出模块规范一一对应） */
enum class ExportFormat(
    val label: String,
    val extension: String,
    val mimeType: String,
    val description: String
) {
    CSV("CSV", "csv", "text/csv", "用户分析用，UTF-8 BOM"),
    JSON("JSON", "json", "application/json", "备份恢复用，含全部数据"),
    XLSX("Excel", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Kexcel，明细 + 汇总 Sheet"),
    PDF("PDF", "pdf", "application/pdf", "compose-to-pdf，A4 多页报表");

    /** 生成默认文件名，如 QRMoney_账单备份_全部_20261024_153045.csv */
    fun defaultFileName(scope: ExportScope, startMillis: Long?, endMillis: Long?, timestamp: Long): String {
        val scopePart = when (scope) {
            ExportScope.ALL -> "全部"
            ExportScope.CUSTOM -> {
                val start = formatDate(startMillis)
                val end = formatDate(endMillis)
                if (start != null && end != null) "${start}_至_${end}" else "自定义"
            }
        }
        return "QRMoney_账单备份_${scopePart}_${formatTimestampCompact(timestamp)}.$extension"
    }
}

/** 导出范围 */
enum class ExportScope(val label: String) {
    ALL("全部账单"),
    CUSTOM("自定义日期范围")
}

/**
 * 导出的单条账单。
 * 金额直接使用数据库原值（元，已为两位小数），不做 ×100 换算，保证导出文件金额显示正确。
 */
@Serializable
data class ExportBill(
    val id: Long,
    val amount: Double,
    val type: String,
    val category: String,
    val remark: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val walletId: Long? = null,
    val walletType: String,
    val debtId: Long? = null
) {
    val isExpense: Boolean get() = type == BillType.EXPENSE.name
}

/** 导出汇总（金额单位：元，两位小数） */
data class ExportSummary(
    val count: Int,
    val income: Double,
    val expense: Double
) {
    val net: Double get() = income - expense
}

/**
 * 记账天数快照：记录“坚持记账的第 X 天”的起点（首次启动时间），导入时恢复该起点即可还原天数。
 */
@Serializable
data class BackupMyDateJson(
    val firstLaunchDate: Long,
    val transactionCount: Int = 0
)

/** 一次导出会话的完整数据（账单 + 记账天数 + 钱包/负债/愿望全库数据） */
data class ExportPayload(
    val bills: List<ExportBill>,
    val summary: ExportSummary,
    val scope: ExportScope,
    val startMillis: Long? = null,
    val endMillis: Long? = null,
    val exportedAt: Long = System.currentTimeMillis(),
    val myDate: BackupMyDateJson? = null,
    val wallets: List<QWalletEntity> = emptyList(),
    val debts: List<QDebtEntity> = emptyList(),
    val wishes: List<QWishEntity> = emptyList()
)

/** 数据库实体 -> 导出行（金额原样保留，两位小数） */
fun QBillEntity.toExportBill(): ExportBill = ExportBill(
    id = id,
    amount = amount,
    type = type.name,
    category = category,
    remark = remark,
    createdAt = createdAt,
    updatedAt = updatedAt,
    walletId = walletId,
    walletType = walletType.name,
    debtId = debtId
)

/**
 * 数据库列表 -> 导出负载。
 * 收支合计用“分”整数累加（每笔四舍五入到分）再转回元，避免 Double 累加误差。
 * 全库备份：除账单外一并携带钱包/负债/愿望与记账天数起点。
 */
fun List<QBillEntity>.toExportPayload(
    scope: ExportScope,
    startMillis: Long? = null,
    endMillis: Long? = null,
    exportedAt: Long = System.currentTimeMillis(),
    myDate: BackupMyDateJson? = null,
    wallets: List<QWalletEntity> = emptyList(),
    debts: List<QDebtEntity> = emptyList(),
    wishes: List<QWishEntity> = emptyList()
): ExportPayload {
    val bills = map { it.toExportBill() }
    val incomeFen = bills.filter { !it.isExpense }.sumOf { (it.amount * 100).roundToLong() }
    val expenseFen = bills.filter { it.isExpense }.sumOf { (it.amount * 100).roundToLong() }
    return ExportPayload(
        bills = bills,
        summary = ExportSummary(
            count = bills.size,
            income = incomeFen / 100.0,
            expense = expenseFen / 100.0
        ),
        scope = scope,
        startMillis = startMillis,
        endMillis = endMillis,
        exportedAt = exportedAt,
        myDate = myDate,
        wallets = wallets,
        debts = debts,
        wishes = wishes
    )
}

/** 金额 -> 两位小数文本，如 10.0 -> "10.00" */
fun yuanText(amount: Double): String = String.format(Locale.US, "%.2f", amount)

/** 钱包类型枚举名 -> 中文显示名 */
fun walletDisplayName(walletType: String): String =
    WalletType.entries.firstOrNull { it.name == walletType }?.displayName ?: walletType

/** 格式化时间戳 -> yyyy-MM-dd HH:mm:ss */
fun formatTimestamp(millis: Long?): String {
    if (millis == null) return ""
    return java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}

/** 格式化时间戳 -> yyyy-MM-dd */
fun formatDate(millis: Long?): String? {
    if (millis == null) return null
    return java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

/** 格式化时间戳 -> 紧凑形式，用于文件名：yyyyMMdd_HHmmss */
fun formatTimestampCompact(millis: Long): String =
    java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
