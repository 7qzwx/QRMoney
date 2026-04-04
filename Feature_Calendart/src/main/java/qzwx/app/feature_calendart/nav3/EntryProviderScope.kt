package qzwx.app.feature_calendart.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavBackStack
import qzwx.app.feature_calendart.screen.CalendarPage

fun EntryProviderScope<NavKey>.CalendarRouteBuilder(
    backStack: NavBackStack<NavKey>
) {
    entry<CalendarRoute.Calendar> { CalendarPage(backStack = backStack) }
}