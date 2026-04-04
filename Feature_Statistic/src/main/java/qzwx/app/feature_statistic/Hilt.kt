package qzwx.app.feature_statistic

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import qzwx.app.feature_statistic.repository.StatisticPage_RT
import qzwx.app.module_data.dao.QBillDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureStatisticModule {
    @Provides
    @Singleton
    fun statisticPage_Repository(qBillDao: QBillDao): StatisticPage_RT {
        return StatisticPage_RT(qBillDao)
    }
}
