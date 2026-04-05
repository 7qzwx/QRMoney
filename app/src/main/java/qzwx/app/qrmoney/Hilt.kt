package qzwx.app.qrmoney

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import qzwx.app.module_data.QBillDatabase
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QDebtDao
import qzwx.app.module_data.dao.QMyDataDao
import qzwx.app.module_data.dao.QWalletDao

@Module
@InstallIn(SingletonComponent::class) // 作用域：应用级单例，整个APP都能用
object QAppModule {
    @Provides
    @Singleton
    fun qBillDatabase(@ApplicationContext context: Context): QBillDatabase {
        return Room.databaseBuilder(
            context,
            QBillDatabase::class.java,
            "QBill数据库"    //自定义数据库名称
        )
            .fallbackToDestructiveMigration()
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

    @Provides
    fun qWalletDao(qBillDatabase: QBillDatabase): QWalletDao {
        return qBillDatabase.qWalletDao()
    }

    @Provides
    fun qDebtDao(qBillDatabase: QBillDatabase): QDebtDao {
        return qBillDatabase.qDebtDao()
    }

}