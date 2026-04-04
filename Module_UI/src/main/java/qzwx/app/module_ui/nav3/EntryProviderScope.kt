package qzwx.app.module_ui.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.module_ui.screen.BillDetailPage

fun EntryProviderScope<NavKey>.UiRouteBuilder(
    onBackClick: () -> Unit
) {
    entry<UIRoute.BillDetail> { route ->
        BillDetailPage(
            billEntity = route.bill,
            onBackClick = onBackClick
        )
    }
}