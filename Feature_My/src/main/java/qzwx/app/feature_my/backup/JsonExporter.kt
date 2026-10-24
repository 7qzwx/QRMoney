package qzwx.app.feature_my.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import qzwx.app.module_data.entity.QDebtEntity
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.QWishEntity

/**
 * JSON 导出器：备份恢复用，含 schemaVersion。
 * 金额直接输出数据库原值（元，两位小数），不做 ×100 换算。
 * 备份内容为整个软件数据库：账单 + 记账天数起点 + 钱包 / 负债 / 愿望。
 */
@Serializable
data class BackupJson(
    val schemaVersion: Int = BACKUP_SCHEMA_VERSION,
    val app: String = "QRMoney",
    val exportedAt: Long,
    val scope: String,
    val startMillis: Long? = null,
    val endMillis: Long? = null,
    val billCount: Int,
    val summary: BackupSummaryJson,
    val bills: List<ExportBill>,
    val myDate: BackupMyDateJson? = null,
    val wallets: List<QWalletEntity> = emptyList(),
    val debts: List<QDebtEntity> = emptyList(),
    val wishes: List<QWishEntity> = emptyList()
)

@Serializable
data class BackupSummaryJson(
    val income: Double,
    val expense: Double,
    val net: Double
)

object JsonExporter {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun export(payload: ExportPayload): ByteArray {
        val backup = BackupJson(
            exportedAt = payload.exportedAt,
            scope = payload.scope.name,
            startMillis = payload.startMillis,
            endMillis = payload.endMillis,
            billCount = payload.summary.count,
            summary = BackupSummaryJson(
                income = payload.summary.income,
                expense = payload.summary.expense,
                net = payload.summary.net
            ),
            bills = payload.bills,
            myDate = payload.myDate,
            wallets = payload.wallets,
            debts = payload.debts,
            wishes = payload.wishes
        )
        return json.encodeToString(backup).toByteArray(Charsets.UTF_8)
    }
}
