package qzwx.app.module_data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import qzwx.app.module_data.entity.QBillEntity

@Dao
interface QBillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: QBillEntity): Long   //Long类型好处：插入成功返回新增账单的ID

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<QBillEntity>)

    @Delete
    suspend fun deleteBill(bill: QBillEntity)

    @Query("DELETE FROM QBillEntity")
    suspend fun deleteAllBills()

    @Update
    suspend fun updateBill(bill: QBillEntity)

    @Query("SELECT * FROM QBillEntity ORDER BY `创建时间` DESC")
    fun getAllBills(): Flow<List<QBillEntity>>

    @Query("SELECT * FROM QBillEntity WHERE `创建时间` >= :startMillis AND `创建时间` < :endMillis ORDER BY `创建时间` DESC")
    fun getBillsBetween(startMillis: Long, endMillis: Long): Flow<List<QBillEntity>>

    @Query("SELECT COUNT(id) FROM QBillEntity")
    fun getBillCount(): Flow<Int>

    @Query("SELECT * FROM QBillEntity WHERE id = :id")
    suspend fun getBillById(id: Long): QBillEntity?

    @Query("SELECT * FROM QBillEntity WHERE `wallet_type` = :type ORDER BY `创建时间` DESC")
    fun getBillsByWalletType(type: qzwx.app.module_data.entity.WalletType): Flow<List<QBillEntity>>
}