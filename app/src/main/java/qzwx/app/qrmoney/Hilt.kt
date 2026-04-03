package qzwx.app.qrmoney

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import qzwx.app.module_data.QBillDatabase
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QMyDataDao

@Module
@InstallIn(SingletonComponent::class) // 作用域：应用级单例，整个APP都能用
object QAppModule {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `QMyDateEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstLaunchDate` INTEGER NOT NULL, `transactionCount` INTEGER NOT NULL)"
            )
        }
    }

    @Provides
    @Singleton
    fun qBillDatabase(@ApplicationContext context: Context): QBillDatabase {
        return Room.databaseBuilder(
            context,
            QBillDatabase::class.java,
            "QBill数据库"    //自定义数据库名称
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun qBillDao(qBillDatabase: QBillDatabase): QBillDao {
        return qBillDatabase.qBillDao()
    }

    @Provides
    fun qMyDateDao(qBillDatabase: QBillDatabase): QMyDataDao {
        return qBillDatabase.qMyDateDao()
    }


}