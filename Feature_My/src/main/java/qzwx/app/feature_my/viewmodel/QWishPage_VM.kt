package qzwx.app.feature_my.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import qzwx.app.module_data.dao.QWishDao
import qzwx.app.module_data.entity.QWishEntity
import javax.inject.Inject
@HiltViewModel
class QWishPage_VM @Inject constructor(
    private val qWishDao: QWishDao
) : ViewModel() {
    val wishList: StateFlow<List<QWishEntity>> = qWishDao.getAllWishes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun insertWish(
        name: String,
        targetAmount: Double,
        savedAmount: Double,
        remark: String?,
        startDate: Long,
        expectedDate: Long?
    ) {
        viewModelScope.launch {
            val entity = QWishEntity(
                name = name,
                targetAmount = targetAmount,
                savedAmount = savedAmount,
                remark = remark,
                startDate = startDate,
                expectedDate = expectedDate
            )
            qWishDao.insertWish(entity)
        }
    }
    fun getWishById(id: Long, onResult: (QWishEntity?) -> Unit) {
        viewModelScope.launch {
            val wish = qWishDao.getWishById(id)
            onResult(wish)
        }
    }
    fun updateWish(
        id: Long,
        name: String,
        targetAmount: Double,
        savedAmount: Double,
        remark: String?,
        startDate: Long,
        expectedDate: Long?
    ) {
        viewModelScope.launch {
            val entity = QWishEntity(
                id = id,
                name = name,
                targetAmount = targetAmount,
                savedAmount = savedAmount,
                remark = remark,
                startDate = startDate,
                expectedDate = expectedDate
            )
            qWishDao.updateWish(entity)
        }
    }
    fun deleteWish(id: Long, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            val wish = qWishDao.getWishById(id)
            if (wish != null) {
                qWishDao.deleteWish(wish)
            }
            onResult()
        }
    }
}
