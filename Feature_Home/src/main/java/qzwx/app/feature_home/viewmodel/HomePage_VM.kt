package qzwx.app.feature_home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.entity.QBillEntity

class HomePage_VM(private val qdao: QBillDao) : ViewModel() {
    fun getAllBills() = qdao.getAllBills()
    fun insertBill(bill: QBillEntity) {
        viewModelScope.launch {
            qdao.insertBill(bill)
        }
    }


    fun deleteBill(bill: QBillEntity) {
        viewModelScope.launch {
            qdao.deleteBill(bill)
        }
    }
}


class HomePage_VMFactory(private val qdao: QBillDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomePage_VM::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomePage_VM(qdao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}