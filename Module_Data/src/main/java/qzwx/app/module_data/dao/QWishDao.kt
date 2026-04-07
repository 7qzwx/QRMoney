package qzwx.app.module_data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import qzwx.app.module_data.entity.QWishEntity

@Dao
interface QWishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWish(wish: QWishEntity): Long

    @Update
    suspend fun updateWish(wish: QWishEntity)

    @Delete
    suspend fun deleteWish(wish: QWishEntity)

    @Query("SELECT * FROM wish_table ORDER BY 开始日期 DESC")
    fun getAllWishes(): Flow<List<QWishEntity>>

    @Query("SELECT * FROM wish_table WHERE id = :id LIMIT 1")
    suspend fun getWishById(id: Long): QWishEntity?
}

