package qzwx.app.feature_my.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import qzwx.app.feature_my.repository.MyPage_RT
import qzwx.app.module_data.entity.QMyDateEntity
import javax.inject.Inject

@HiltViewModel
class MyPage_VM @Inject constructor(
    private val repository: MyPage_RT
) : ViewModel() {

    val myDate: StateFlow<QMyDateEntity?> = repository.getMyDate()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val actualTransactionCount: StateFlow<Int> = repository.getBillCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun initializeFirstLaunchDate(currentTime: Long) {
        viewModelScope.launch {
            if (myDate.value == null) {
                repository.insertMyDate(
                    QMyDateEntity(
                        firstLaunchDate = currentTime,
                        transactionCount = 0
                    )
                )
            }
        }
    }

    // Optional function if you want to update from this screen
    fun updateTransactionCount(count: Int) {
        viewModelScope.launch {
            repository.updateTransactionCount(count)
        }
    }
}
