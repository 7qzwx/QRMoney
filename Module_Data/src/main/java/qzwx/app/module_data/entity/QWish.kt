package qzwx.app.module_data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wish_table")
data class QWishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    @ColumnInfo(name = "名称") val name: String,

    @ColumnInfo(name = "金额") val targetAmount: Double,

    @ColumnInfo(name = "已攒金额") val savedAmount: Double = 0.0,

    @ColumnInfo(name = "备注") val remark: String? = null,

    @ColumnInfo(name = "开始日期") val startDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "期望日期") val expectedDate: Long? = null
)

