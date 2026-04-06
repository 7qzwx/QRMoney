package qzwx.app.module_ui.nav3

import androidx.navigation3.runtime.NavKey
import qzwx.app.module_data.entity.QBillEntity

@kotlinx.serialization.Serializable
sealed class UIRoute : NavKey, java.io.Serializable {
    @kotlinx.serialization.Serializable
    data class BillDetail(val bill: QBillEntity) : UIRoute()

    @kotlinx.serialization.Serializable
    data class CategoryBills(val categoryName: String, val bills: List<QBillEntity>) : UIRoute()

    @kotlinx.serialization.Serializable
    data class Search(val walletTypeName: String? = null) : UIRoute()
}