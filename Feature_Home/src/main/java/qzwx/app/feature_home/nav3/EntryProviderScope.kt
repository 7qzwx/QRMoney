package qzwx.app.feature_home.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_home.screen.HomePage

fun EntryProviderScope<NavKey>.HomeRouteBuilder() {
    entry<HomeRoute.Home> { HomePage() }
}