package qzwx.app.module_data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debt_table")
data class QDebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    @ColumnInfo(name = "名称") val name: String,

    @ColumnInfo(name = "类型") val type: DebtType,

    @ColumnInfo(name = "总金额") val totalAmount: Double,

    @ColumnInfo(name = "剩余待还金额") val remainingAmount: Double,

    @ColumnInfo(name = "创建时间") val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "最后还清时间") val clearedAt: Long? = null
)

enum class DebtType(val displayName: String) {
    BORROW("借入"),
    LEND("借出")
}

