package qzwx.app.qrmoney.nav3

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_asset.nav3.AssetRoute
import qzwx.app.feature_calendart.nav3.CalendarRoute
import qzwx.app.feature_home.nav3.HomeRoute
import qzwx.app.feature_my.nav3.MyRoute
import qzwx.app.feature_statistic.nav3.StatisticRoute


sealed class BottomBarRoute(val route: NavKey, val Icon: ImageVector, val label: String) {
    data object Home :
        BottomBarRoute(route = HomeRoute.Home, Icon = Icons.Default.MenuBook, label = "账单")

    data object Calendar :
        BottomBarRoute(
            route = CalendarRoute.Calendar,
            Icon = Icons.Default.CalendarMonth,
            label = "日历"
        )

    data object Statistic : BottomBarRoute(
        route = StatisticRoute.Statictic, Icon = Icons.Default.PieChart, label = "图表"
    )

    data object Asset : BottomBarRoute(
        route = AssetRoute.Asset, Icon = Icons.Default.AccountBalanceWallet, label = "钱包"
    )

    data object My : BottomBarRoute(
        route = MyRoute.My, Icon = Icons.Default.Person, label = "我的"
    )


}








