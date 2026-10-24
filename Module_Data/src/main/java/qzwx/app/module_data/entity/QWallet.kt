package qzwx.app.module_data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallet_table",
    indices = [Index(value = ["类型"], unique = true)]
)
@kotlinx.serialization.Serializable
data class QWalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    @ColumnInfo(name = "类型") val type: WalletType,

    @ColumnInfo(name = "余额") val balance: Double = 0.0,

    @ColumnInfo(name = "是否默认") val isDefault: Boolean = false,

    @ColumnInfo(name = "修改时间") val updatedAt: Long = System.currentTimeMillis()
)

@kotlinx.serialization.Serializable
enum class WalletType(val displayName: String) {
    CASH("现金"),
    ALIPAY("支付宝"),
    WECHAT("微信"),
    BANK_CARD("银行卡"),
    OTHER("其它")

    ;

    companion object {
        fun fromDisplayName(displayName: String): WalletType {
            return entries.firstOrNull { it.displayName == displayName } ?: ALIPAY
        }
    }
}
