package qzwx.app.module_data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import qzwx.app.module_data.entity.QWalletEntity

@Dao
interface QWalletDao {
    @Query("SELECT * FROM wallet_table")
    fun getAllWallets(): Flow<List<QWalletEntity>>

    @Query("SELECT * FROM wallet_table")
    suspend fun getAllWalletsOnce(): List<QWalletEntity>

    @Query("SELECT * FROM wallet_table WHERE id = :id")
    suspend fun getWalletById(id: Long): QWalletEntity?

    @Query("SELECT * FROM wallet_table WHERE `类型` = :type LIMIT 1")
    suspend fun getWalletByType(type: qzwx.app.module_data.entity.WalletType): QWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: QWalletEntity): Long

    @Update
    suspend fun updateWallet(wallet: QWalletEntity)

    @Delete
    suspend fun deleteWallet(wallet: QWalletEntity)
}

