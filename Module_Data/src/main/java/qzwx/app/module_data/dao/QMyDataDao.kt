package qzwx.app.module_data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import qzwx.app.module_data.entity.QMyDateEntity

@Dao
interface QMyDataDao {
    @Query("SELECT * FROM QMyDateEntity LIMIT 1")
    fun getMyDate(): Flow<QMyDateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyDate(myDate: QMyDateEntity)

    @Query("UPDATE QMyDateEntity SET firstLaunchDate = :date, transactionCount = :count WHERE id = (SELECT id FROM QMyDateEntity LIMIT 1)")
    suspend fun updateMyDate(date: Long, count: Int)

    @Query("UPDATE QMyDateEntity SET transactionCount = :count WHERE id = (SELECT id FROM QMyDateEntity LIMIT 1)")
    suspend fun updateTransactionCount(count: Int)
}