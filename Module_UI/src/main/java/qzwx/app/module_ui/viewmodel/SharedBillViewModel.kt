package qzwx.app.module_ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import qzwx.app.module_data.dao.QWalletDao
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.WalletType
import javax.inject.Inject

@HiltViewModel
class SharedBillViewModel @Inject constructor(
    private val walletDao: QWalletDao
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

    fun updateWalletBalance(type: WalletType, newBalance: Double, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val existingWallet = walletDao.getWalletByType(type)
            if (existingWallet != null) {
                walletDao.updateWallet(
                    existingWallet.copy(
                        balance = newBalance,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                walletDao.insertWallet(
                    QWalletEntity(
                        type = type,
                        balance = newBalance,
                        isDefault = type == WalletType.ALIPAY
                    )
                )
            }
            onDone()
        }
    }

}
