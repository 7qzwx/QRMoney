package qzwx.app.module_data.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class QBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    @ColumnInfo(name = "金额") val amount: Double,

    @ColumnInfo(name = "类型") val type: BillType = BillType.EXPENSE,

    @ColumnInfo(name = "种类") val category: String,

    @ColumnInfo(name = "备注") val remark: String? = null,

    // 创建时间
    @ColumnInfo(name = "创建时间") val createdAt: Long = System.currentTimeMillis(),

    // 修改时间
    @ColumnInfo(name = "修改时间") val updatedAt: Long = System.currentTimeMillis(),

    // 关联的钱包ID：记录该笔账单资金从哪个钱包发生变化
    @ColumnInfo(name = "wallet_id") val walletId: Long? = null,

    // 支付方式快照：用于在钱包记录变更后仍能保留账单的支付类型
    @ColumnInfo(name = "wallet_type") val walletType: WalletType = WalletType.ALIPAY,

    // 关联的负债ID：如果该账单属于某笔借款或还款记录，可关联此ID
    @ColumnInfo(name = "debt_id") val debtId: Long? = null
)


enum class BillType {
    EXPENSE,
    INCOME
}