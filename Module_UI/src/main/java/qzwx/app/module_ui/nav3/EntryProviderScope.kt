package qzwx.app.module_ui.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.module_ui.screen.BillDetailPage
import qzwx.app.module_ui.screen.CategoryBillsPage

fun EntryProviderScope<NavKey>.UiRouteBuilder(
    onBackClick: () -> Unit,
    onNavigateToBillDetail: (qzwx.app.module_data.entity.QBillEntity) -> Unit = {}
) {
    entry<UIRoute.BillDetail> { route ->
        BillDetailPage(
            billEntity = route.bill,
            onBackClick = onBackClick
        )
    }
    entry<UIRoute.CategoryBills> { route ->
        CategoryBillsPage(
            categoryName = route.categoryName,
            bills = route.bills,
            onBackClick = onBackClick,
            onItemClick = onNavigateToBillDetail
        )
    }
}