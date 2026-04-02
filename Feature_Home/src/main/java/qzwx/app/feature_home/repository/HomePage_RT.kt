package qzwx.app.feature_home.repository

import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.entity.QBillEntity

class HomePage_RT(private val qBillDao: QBillDao) {
    val allBills = qBillDao.getAllBills()
    suspend fun insertBill(bill: QBillEntity): Long {
        return qBillDao.insertBill(bill)
    }

    suspend fun deleteBill(bill: QBillEntity) {
        qBillDao.deleteBill(bill)
    }

    suspend fun updateBill(bill: QBillEntity) {
        qBillDao.updateBill(bill)
    }

    suspend fun getBillById(id: Long): QBillEntity? {
        return qBillDao.getBillById(id)
    }
}