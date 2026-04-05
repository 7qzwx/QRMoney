package qzwx.app.module_data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import qzwx.app.module_data.entity.QDebtEntity

@Dao
interface QDebtDao {
    @Query("SELECT * FROM debt_table")
    fun getAllDebts(): Flow<List<QDebtEntity>>

    @Query("SELECT * FROM debt_table WHERE id = :id")
    suspend fun getDebtById(id: Long): QDebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: QDebtEntity): Long

    @Update
    suspend fun updateDebt(debt: QDebtEntity)

    @Delete
    suspend fun deleteDebt(debt: QDebtEntity)
}

