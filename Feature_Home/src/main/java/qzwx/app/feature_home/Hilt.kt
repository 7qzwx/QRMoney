package qzwx.app.feature_home

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import qzwx.app.feature_home.repository.HomePage_RT
import qzwx.app.module_data.dao.QBillDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureHomeModule {
    @Provides
    @Singleton
    fun homePage_Repository(qBillDao: QBillDao): HomePage_RT {
        return HomePage_RT(qBillDao)
    }
}