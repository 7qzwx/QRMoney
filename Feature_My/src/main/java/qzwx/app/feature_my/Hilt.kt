package qzwx.app.feature_my

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import qzwx.app.feature_my.repository.MyPage_RT
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QMyDataDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureMyModule {
    @Provides
    @Singleton
    fun provideMyPageRepository(
        qMyDataDao: QMyDataDao,
        qBillDao: QBillDao
    ): MyPage_RT {
        return MyPage_RT(qMyDataDao, qBillDao)
    }
}
