package qzwx.app.qrmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import qzwx.app.feature_asset.nav3.AssetRouteBuilder
import qzwx.app.feature_calendart.nav3.CalendarRouteBuilder
import qzwx.app.feature_home.nav3.HomeRouteBuilder
import qzwx.app.feature_home.viewmodel.HomePage_VM
import qzwx.app.feature_home.viewmodel.HomePage_VMFactory
import qzwx.app.feature_my.nav3.MyRouteBuilder
import qzwx.app.feature_statistic.nav3.StatisticRouteBuilder
import qzwx.app.module_data.QBillDatabase
import qzwx.app.module_ui.theme.QAppTheme
import qzwx.app.qrmoney.nav3.BottomBarRoute


class MainActivity : ComponentActivity() {
    private val database by lazy { QBillDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            QAppTheme() {
                val homepageVm: HomePage_VM =
                    viewModel(factory = HomePage_VMFactory(database.qBillDao()))
                QApp(
                    homepageVm = homepageVm
                )
            }
        }
    }
}

@Composable
fun QApp(
    homepageVm: HomePage_VM
) {
    val bottomBarRoute = listOf(
        BottomBarRoute.Home,
        BottomBarRoute.Calendar,
        BottomBarRoute.Statistic,
        BottomBarRoute.Asset,
        BottomBarRoute.My
    )
    var selectedRoute: BottomBarRoute by remember { mutableStateOf(BottomBarRoute.Home) }
    val backStack = rememberNavBackStack(selectedRoute.route)
    val isBottomBarVisible = bottomBarRoute.any { it.route == backStack.lastOrNull() }  // 是否显示底部导航栏

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar {
                    bottomBarRoute.forEach { route ->
                        NavigationBarItem(
                            selected = selectedRoute == route,
                            icon = { Icon(route.Icon, route.label) },
                            label = { Text(route.label) },
                            onClick = {
                                selectedRoute = route
                                backStack.clear()
                                backStack.add(route.route)
                            }
                        )
                    }
                }
            }
        }) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                HomeRouteBuilder(
                    homepageVm = homepageVm
                )
                CalendarRouteBuilder()
                AssetRouteBuilder()
                MyRouteBuilder()
                StatisticRouteBuilder()
            }
        )
    }
}