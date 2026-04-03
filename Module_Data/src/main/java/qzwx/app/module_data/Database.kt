package qzwx.app.module_data


import androidx.room.Database
import androidx.room.RoomDatabase
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QMyDataDao
import qzwx.app.module_data.entity.QBillEntity

@Database(
    entities = [QBillEntity::class],
    version = 1
)
abstract class QBillDatabase : RoomDatabase() {
    abstract fun qBillDao(): QBillDao
    abstract fun qMyDateDao(): QMyDataDao

}