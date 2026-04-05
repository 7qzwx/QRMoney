package qzwx.app.feature_home.repository

import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QWalletDao
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_data.entity.BillType

class HomePage_RT(
    private val qBillDao: QBillDao,
    private val qWalletDao: QWalletDao
) {
    val allBills = qBillDao.getAllBills()

    suspend fun insertBill(bill: QBillEntity): Long {
        val existingBill = if (bill.id != 0L) qBillDao.getBillById(bill.id) else null

        // Reverse old bill effect on wallet
        if (existingBill != null) {
            applyWalletEffect(existingBill, reverse = true)
        }

        val newId = qBillDao.insertBill(bill)

        // Apply new bill effect on wallet
        applyWalletEffect(bill, reverse = false)

        return newId
    }

    suspend fun deleteBill(bill: QBillEntity) {
        qBillDao.deleteBill(bill)
        applyWalletEffect(bill, reverse = true)
    }

    suspend fun updateBill(bill: QBillEntity) {
        insertBill(bill) // we can reuse insertBill for updates
    }

    suspend fun getBillById(id: Long): QBillEntity? {
        return qBillDao.getBillById(id)
    }

    private suspend fun applyWalletEffect(bill: QBillEntity, reverse: Boolean) {
        val wallet = qWalletDao.getWalletByType(bill.walletType) ?: return

        val multiplier = if (reverse) -1 else 1
        val isIncome = (bill.type == BillType.INCOME)

        val delta = if (isIncome) {
            bill.amount * multiplier
        } else {
            -bill.amount * multiplier
        }

        qWalletDao.updateWallet(wallet.copy(balance = wallet.balance + delta))
    }
}