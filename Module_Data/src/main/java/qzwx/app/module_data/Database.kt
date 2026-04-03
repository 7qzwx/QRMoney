package qzwx.app.module_data


import androidx.room.Database
import androidx.room.RoomDatabase
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QMyDataDao
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_data.entity.QMyDateEntity

@Database(
    entities = [QBillEntity::class, QMyDateEntity::class],
    version = 2
)
abstract class QBillDatabase : RoomDatabase() {
    abstract fun qBillDao(): QBillDao
    abstract fun qMyDateDao(): QMyDataDao

}