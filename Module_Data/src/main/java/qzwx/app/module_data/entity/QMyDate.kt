package qzwx.app.module_data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QMyDateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val firstLaunchDate: Long,
    val transactionCount: Int
)
