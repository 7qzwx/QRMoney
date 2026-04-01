package qzwx.app.qrmoney.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_home.nav3.HomeRoute
import qzwx.app.feature_home.screen.HomePage
import qzwx.app.qrmoney.screen.*



fun EntryProviderScope<NavKey>.CalendarRouteBuilder() {
    entry<CalendarRoute.Calendar> { CalendarPage() }
}

fun EntryProviderScope<NavKey>.AssetRouteBuilder() {
    entry<AssetRoute.Asset> { AssetsPage() }
}


fun EntryProviderScope<NavKey>.StatisticRouteBuilder() {
    entry<StatisticRoute.Statictic> { StatisticPage() }
}