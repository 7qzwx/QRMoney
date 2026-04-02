package qzwx.app.feature_home.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_home.screen.HomePage
import qzwx.app.feature_home.viewmodel.HomePage_VM

fun EntryProviderScope<NavKey>.HomeRouteBuilder(homepageVm: HomePage_VM) {
    entry<HomeRoute.Home> { HomePage(homepageVm) }
}