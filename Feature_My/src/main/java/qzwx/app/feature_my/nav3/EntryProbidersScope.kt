package qzwx.app.feature_my.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_my.screen.MyPage
import qzwx.app.feature_my.screen.QWidgetsPage
import qzwx.app.feature_my.screen.qwish.AddNewWishPage
import qzwx.app.feature_my.screen.qwish.QWishPage
import qzwx.app.feature_my.screen.qwish.WishDetailPage

fun EntryProviderScope<NavKey>.MyRouteBuilder(backStack: NavBackStack<NavKey>) {
    entry<MyRoute.My> { MyPage(backStack) }
    entry<MyRoute.QWish> {
        QWishPage(
            onBackClick = { backStack.removeLastOrNull() },
            onAddNewWish = { backStack.add(MyRoute.AddNewWish) },
            onWishClick = { wishId -> backStack.add(MyRoute.WishDetail(wishId)) }
        )
    }
    entry<MyRoute.AddNewWish> { AddNewWishPage(backStack = backStack) }
    entry<MyRoute.EditWish> { route ->
        AddNewWishPage(
            backStack = backStack,
            wishId = route.wishId
        )
    }
    entry<MyRoute.WishDetail> { route ->
        WishDetailPage(
            wishId = route.wishId,
            onBackClick = { backStack.removeLastOrNull() },
            onEditClick = { wishId -> backStack.add(MyRoute.EditWish(wishId)) }
        )
    }
    entry<MyRoute.QWidgets> { QWidgetsPage() }
}
