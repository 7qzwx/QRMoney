package qzwx.app.feature_my.backup

import kotlinx.serialization.json.Json
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_data.entity.WalletType

/**
 * JSON 导入器：解析本软件导出的备份 JSON（含 schemaVersion），返回完整备份内容。
 * 导入校验：schemaVersion 必须匹配、来源必须是 QRMoney；字段结构由 ignoreUnknownKeys 容忍未来扩展。
 */
object JsonImporter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun parse(bytes: ByteArray): BackupJson {
        val backup = json.decodeFromString<BackupJson>(bytes.toString(Charsets.UTF_8))
        require(backup.schemaVersion == BACKUP_SCHEMA_VERSION) {
            "备份文件版本不受支持（schemaVersion=${backup.schemaVersion}，当前支持 $BACKUP_SCHEMA_VERSION）"
        }
        require(backup.app == "QRMoney") { "不是 QRMoney 导出的备份文件" }
        return backup
    }
}

/** 导出行 -> 数据库实体（保留 id，供覆盖/清空导入使用） */
fun ExportBill.toEntity(): QBillEntity = QBillEntity(
    id = id,
    amount = amount,
    type = BillType.valueOf(type),
    category = category,
    remark = remark,
    createdAt = createdAt,
    updatedAt = updatedAt,
    walletId = walletId,
    walletType = WalletType.valueOf(walletType),
    debtId = debtId
)
