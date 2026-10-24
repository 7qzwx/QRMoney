package qzwx.app.module_data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import qzwx.app.module_data.entity.QWishEntity

@Dao
interface QWishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWish(wish: QWishEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishes(wishes: List<QWishEntity>)

    @Query("DELETE FROM wish_table")
    suspend fun deleteAllWishes()

    /** 清空愿望表后写入备份数据（事务），供“清空后导入”使用 */
    @Transaction
    suspend fun replaceAllWishes(wishes: List<QWishEntity>) {
        deleteAllWishes()
        insertWishes(wishes)
    }

    @Update
    suspend fun updateWish(wish: QWishEntity)

    @Delete
    suspend fun deleteWish(wish: QWishEntity)

    @Query("SELECT * FROM wish_table ORDER BY 开始日期 DESC")
    fun getAllWishes(): Flow<List<QWishEntity>>

    @Query("SELECT * FROM wish_table")
    suspend fun getAllWishesOnce(): List<QWishEntity>

    @Query("SELECT * FROM wish_table WHERE id = :id LIMIT 1")
    suspend fun getWishById(id: Long): QWishEntity?
}

