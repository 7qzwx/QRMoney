package qzwx.app.feature_home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import qzwx.app.feature_home.repository.HomePage_RT
import qzwx.app.module_data.entity.QBillEntity
import javax.inject.Inject

@HiltViewModel
class HomePage_VM @Inject constructor(private val qrepository: HomePage_RT) : ViewModel() {
    fun getAllBills() = qrepository.allBills
    fun insertBill(bill: QBillEntity) =
        viewModelScope.launch {
            qrepository.insertBill(bill)

        }

    fun deleteBill(bill: QBillEntity) {
        viewModelScope.launch {
            qrepository.deleteBill(bill)
        }
    }
}


