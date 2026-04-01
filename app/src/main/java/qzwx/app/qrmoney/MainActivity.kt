package qzwx.app.qrmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import qzwx.app.feature_home.nav3.HomeRouteBuilder
import qzwx.app.feature_my.nav3.MyRouteBuilder
import qzwx.app.qrmoney.nav3.*
import qzwx.app.qrmoney.ui.theme.QAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            QAppTheme() {
                QApp()
            }
        }
    }
}

@Composable
fun QApp() {
    val bottomBarRoute = listOf(
        BottomBarRoute.Home,
        BottomBarRoute.Calendar,
        BottomBarRoute.Statistic,
        BottomBarRoute.Asset,
        BottomBarRoute.My
    )
    var selectedRoute: BottomBarRoute by remember { mutableStateOf(BottomBarRoute.Home) }
    val backStack = rememberNavBackStack(selectedRoute.route)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
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
        }) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                HomeRouteBuilder()
                CalendarRouteBuilder()
                AssetRouteBuilder()
                MyRouteBuilder()
                StatisticRouteBuilder()
            }
        )
    }
}