package qzwx.app.feature_statistic.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import qzwx.app.feature_statistic.repository.StatisticPage_RT
import qzwx.app.module_data.entity.QBillEntity
import javax.inject.Inject

@HiltViewModel
class StatisticPage_VM @Inject constructor(
    private val repository: StatisticPage_RT
) : ViewModel() {
    val allBills: Flow<List<QBillEntity>> = repository.allBills
}
