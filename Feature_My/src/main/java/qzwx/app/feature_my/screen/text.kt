package qzwx.app.feature_my.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.utils.MockDataUtils
import javax.inject.Inject


@HiltViewModel
class DevTestViewModel @Inject constructor(
    private val qBillDao: QBillDao
) : ViewModel() {

    /**
     * 一键插入1000条随机测试数据
     */
    fun insertMockData() {
        // 数据库操作推荐在 IO 线程进行
        viewModelScope.launch(Dispatchers.IO) {
            val randomBills = MockDataUtils.generateRandomBills(1000)
            qBillDao.insertBills(randomBills)
        }
    }

    /**
     * 一键清空所有账单数据
     */
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            qBillDao.deleteAllBills()
        }
    }
}
