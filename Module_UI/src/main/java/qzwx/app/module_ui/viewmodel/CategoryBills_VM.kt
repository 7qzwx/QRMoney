package qzwx.app.module_ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.entity.QBillEntity
import javax.inject.Inject

@HiltViewModel
class CategoryBills_VM @Inject constructor(
    private val billDao: QBillDao
) : ViewModel() {

    fun deleteBill(bill: QBillEntity) {
        viewModelScope.launch {
            billDao.deleteBill(bill)
        }
    }

    fun insertBill(bill: QBillEntity) {
        viewModelScope.launch {
            billDao.insertBill(bill)
        }
    }
}

