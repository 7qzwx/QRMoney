package qzwx.app.module_ui.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.module_ui.screen.BillDetailPage
import qzwx.app.module_ui.screen.CategoryBillsPage
import qzwx.app.module_ui.screen.SharedSearchPage

fun EntryProviderScope<NavKey>.UiRouteBuilder(backStack: androidx.navigation3.runtime.NavBackStack<NavKey>) {
    entry<UIRoute.BillDetail> { route ->
        BillDetailPage(
            billEntity = route.bill,
            onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() }
        )
    }
    entry<UIRoute.CategoryBills> { route ->
        CategoryBillsPage(
            categoryName = route.categoryName,
            bills = route.bills,
            onItemClick = { clickedBill ->
                backStack.add(UIRoute.BillDetail(clickedBill))
            },
            onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() }
        )
    }
    entry<UIRoute.Search> { route ->
        SharedSearchPage(backStack = backStack, walletTypeName = route.walletTypeName)
    }
}