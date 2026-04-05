package qzwx.app.module_data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_table")
data class QWalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    @ColumnInfo(name = "类型") val type: WalletType,

    @ColumnInfo(name = "余额") val balance: Double = 0.0,

    @ColumnInfo(name = "修改时间") val updatedAt: Long = System.currentTimeMillis()
)

enum class WalletType(val displayName: String) {
    CASH("现金"),
    ALIPAY("支付宝"),
    WECHAT("微信"),
    BANK_CARD("银行卡"),
    OTHER("其它")
}

