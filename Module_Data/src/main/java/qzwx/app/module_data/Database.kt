package qzwx.app.module_data


import androidx.room.Database
import androidx.room.RoomDatabase
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QDebtDao
import qzwx.app.module_data.dao.QMyDataDao
import qzwx.app.module_data.dao.QWalletDao
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_data.entity.QDebtEntity
import qzwx.app.module_data.entity.QMyDateEntity
import qzwx.app.module_data.entity.QWalletEntity

@Database(
    entities = [QBillEntity::class, QMyDateEntity::class, QWalletEntity::class, QDebtEntity::class],
    version = 3
)
abstract class QBillDatabase : RoomDatabase() {
    abstract fun qBillDao(): QBillDao
    abstract fun qMyDateDao(): QMyDataDao
    abstract fun qWalletDao(): QWalletDao
    abstract fun qDebtDao(): QDebtDao

}