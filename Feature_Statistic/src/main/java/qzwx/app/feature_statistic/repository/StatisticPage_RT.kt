package qzwx.app.feature_statistic.repository

import qzwx.app.module_data.dao.QBillDao

class StatisticPage_RT(private val qBillDao: QBillDao) {
    val allBills = qBillDao.getAllBills()

    fun getBillsBetween(startMillis: Long, endMillis: Long) = qBillDao.getBillsBetween(startMillis, endMillis)
}
