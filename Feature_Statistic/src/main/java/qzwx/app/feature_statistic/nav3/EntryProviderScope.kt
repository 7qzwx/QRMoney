package qzwx.app.feature_statistic.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_statistic.screen.StatisticPage

fun EntryProviderScope<NavKey>.StatisticRouteBuilder() {
    entry<StatisticRoute.Statictic> { StatisticPage() }
}