package qzwx.app.feature_my.repository

import kotlinx.coroutines.flow.Flow
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QMyDataDao
import qzwx.app.module_data.entity.QMyDateEntity
import javax.inject.Inject

class MyPage_RT @Inject constructor(
    private val qMyDataDao: QMyDataDao,
    private val qBillDao: QBillDao
) {
    fun getMyDate(): Flow<QMyDateEntity?> {
        return qMyDataDao.getMyDate()
    }

    suspend fun insertMyDate(myDate: QMyDateEntity) {
        qMyDataDao.insertMyDate(myDate)
    }

    suspend fun updateTransactionCount(count: Int) {
        qMyDataDao.updateTransactionCount(count)
    }

    fun getBillCount(): Flow<Int> {
        return qBillDao.getBillCount()
    }
}
