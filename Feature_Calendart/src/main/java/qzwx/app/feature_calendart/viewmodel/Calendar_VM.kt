package qzwx.app.feature_calendart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.entity.QBillEntity
import javax.inject.Inject

@HiltViewModel
class Calendar_VM @Inject constructor(
    private val billDao: QBillDao
) : ViewModel() {

    val bills: StateFlow<List<QBillEntity>> = billDao.getAllBills()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun deleteBill(bill: QBillEntity) {
        billDao.deleteBill(bill)
    }

    suspend fun insertAndReturnId(bill: QBillEntity): Long {
        return billDao.insertBill(bill)
    }
}