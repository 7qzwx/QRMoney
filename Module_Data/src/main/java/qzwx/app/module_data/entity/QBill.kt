package qzwx.app.module_data.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class QBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    @ColumnInfo(name = "金额") val amount: Long,

    @ColumnInfo(name = "类型") val type: BillType = BillType.EXPENSE,

    @ColumnInfo(name = "种类") val category: String,

    @ColumnInfo(name = "备注") val remark: String? = null,

    // 创建时间
    @ColumnInfo(name = "创建时间") val createdAt: Long = System.currentTimeMillis(),

    // 修改时间
    @ColumnInfo(name = "修改时间") val updatedAt: Long = System.currentTimeMillis(),
)


enum class BillType {
    EXPENSE,
    INCOME
}