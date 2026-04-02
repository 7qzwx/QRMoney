package qzwx.app.module_data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.entity.QBillEntity

@Database(
    entities = [QBillEntity::class],
    version = 1
)
abstract class QBillDatabase : RoomDatabase() {
    abstract fun qBillDao(): QBillDao

    companion object {
        @Volatile
        private var INSTANCE: QBillDatabase? = null

        fun getInstance(context: Context): QBillDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QBillDatabase::class.java,
                    "QBill数据库"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}