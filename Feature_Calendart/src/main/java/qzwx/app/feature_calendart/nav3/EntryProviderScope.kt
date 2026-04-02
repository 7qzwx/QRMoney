package qzwx.app.feature_calendart.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_calendart.screen.CalendarPage

fun EntryProviderScope<NavKey>.CalendarRouteBuilder() {
    entry<CalendarRoute.Calendar> { CalendarPage() }
}