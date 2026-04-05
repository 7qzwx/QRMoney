package qzwx.app.module_ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import qzwx.app.module_data.dao.QWalletDao
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.WalletType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.dao.QBillDao
import javax.inject.Inject

@HiltViewModel
class SharedBillViewModel @Inject constructor(
    private val walletDao: QWalletDao,
    private val billDao: QBillDao
) : ViewModel() {

    private val walletInitMutex = Mutex()
    @Volatile
    private var walletsInitialized = false

    val wallets: StateFlow<List<QWalletEntity>> = walletDao.getAllWallets()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val allBills: StateFlow<List<QBillEntity>> = billDao.getAllBills()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        ensureWalletsInitialized()
    }

    fun ensureWalletsInitialized() {
        viewModelScope.launch {
            ensureWalletsInitializedInternal()
        }
    }

    private suspend fun ensureWalletsInitializedInternal() {
        if (walletsInitialized) return
        walletInitMutex.withLock {
            if (walletsInitialized) return@withLock

            val allTypes = WalletType.entries
            val existing = walletDao.getAllWalletsOnce()
            val existingTypes = existing.map { it.type }.toSet()

            allTypes.forEach { type ->
                if (!existingTypes.contains(type)) {
                    val isDefault = existing.isEmpty() && type == WalletType.ALIPAY
                    walletDao.insertWallet(QWalletEntity(type = type, isDefault = isDefault))
                }
            }

            walletsInitialized = true
        }
    }

    val defaultWalletName: StateFlow<String> = walletDao.getAllWallets()
        .map { list ->
            list.find { it.isDefault }?.type?.displayName ?: "支付宝"
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "支付宝"
        )

    fun updateWalletBalance(type: WalletType, newBalance: Double, isDefault: Boolean? = null, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (isDefault == true) {
                // 将其他的钱包设为非默认
                val allExisting = walletDao.getAllWalletsOnce()
                allExisting.forEach { w ->
                    if (w.isDefault) {
                        walletDao.updateWallet(w.copy(isDefault = false, updatedAt = System.currentTimeMillis()))
                    }
                }
            }

            val existingWallet = walletDao.getWalletByType(type)
            if (existingWallet != null) {
                walletDao.updateWallet(
                    existingWallet.copy(
                        balance = newBalance,
                        isDefault = isDefault ?: existingWallet.isDefault,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                walletDao.insertWallet(
                    QWalletEntity(
                        type = type,
                        balance = newBalance,
                        isDefault = isDefault ?: (type == WalletType.ALIPAY)
                    )
                )
            }
            onDone()
        }
    }

    fun getBillsByWallet(type: WalletType): Flow<List<QBillEntity>> = billDao.getBillsByWalletType(type)

    fun applyBillToWallet(bill: QBillEntity, reverse: Boolean = false) {
        viewModelScope.launch {
            val wallet = walletDao.getWalletByType(bill.walletType) ?: return@launch

            val multiplier = if (reverse) -1 else 1
            val isIncome = (bill.type == BillType.INCOME)

            val delta = if (isIncome) {
                bill.amount * multiplier
            } else {
                -bill.amount * multiplier
            }

            walletDao.updateWallet(wallet.copy(balance = wallet.balance + delta))
        }
    }

    suspend fun insertAndReturnId(bill: QBillEntity): Long {
        val existingBill = if (bill.id != 0L) billDao.getBillById(bill.id) else null

        if (existingBill != null) {
            applyBillToWallet(existingBill, reverse = true)
        }

        val newId = billDao.insertBill(bill)
        applyBillToWallet(bill.copy(id = newId), reverse = false)
        return newId
    }

    fun insertBill(bill: QBillEntity) {
        viewModelScope.launch {
            insertAndReturnId(bill)
        }
    }

    fun deleteBill(bill: QBillEntity) {
        viewModelScope.launch {
            billDao.deleteBill(bill)
            applyBillToWallet(bill, reverse = true)
        }
    }
}
