package qzwx.app.module_ui.nav3

import androidx.navigation3.runtime.NavKey
import qzwx.app.module_data.entity.QBillEntity

sealed class UIRoute : NavKey {
    data class BillDetail(val bill: QBillEntity) : UIRoute()
}