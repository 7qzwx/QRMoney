package qzwx.app.feature_home.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_home.screen.HomePage
fun EntryProviderScope<NavKey>.HomeRouteBuilder(
    backStack: NavBackStack<NavKey>
) {
    entry<HomeRoute.Home> { HomePage(backStack = backStack) }
}