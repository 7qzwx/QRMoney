package qzwx.app.feature_my.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_my.screen.MyPage

fun EntryProviderScope<NavKey>.MyRouteBuilder() {
    entry<MyRoute.My> { MyPage() }
}
