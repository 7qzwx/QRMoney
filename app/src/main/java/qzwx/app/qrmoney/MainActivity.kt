package qzwx.app.qrmoney

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import qzwx.app.feature_asset.nav3.AssetRouteBuilder
import qzwx.app.feature_calendart.nav3.CalendarRouteBuilder
import qzwx.app.feature_home.nav3.HomeRouteBuilder
import qzwx.app.feature_my.nav3.MyRouteBuilder
import qzwx.app.feature_statistic.nav3.StatisticRouteBuilder
import qzwx.app.module_ui.nav3.UiRouteBuilder
import qzwx.app.module_ui.theme.QAppTheme
import qzwx.app.qrmoney.nav3.BottomBarRoute
import qzwx.app.qrmoney.scheduler.MealReminderScheduler

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTaskRoot) {
            val intentAction = intent.action
            if (intent.hasCategory(android.content.Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction == android.content.Intent.ACTION_MAIN) {
                finish()
                return
            }
        }
        enableEdgeToEdge()
        setContent {
            QAppTheme {
                QApp()
            }
        }
        // ① 检查并申请通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        // ② 检查精确闹钟权限（Android 12+）
        checkExactAlarmPermission()

        // ③ 设置闹钟
        MealReminderScheduler(this).scheduleToday()

        // ✅ 冷启动：从通知点进来走这里
        handleNotificationIntent(intent)
    }

    // ✅ 保留：App 在后台时，点通知会走这里，确保 intent 被消费
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 现在不需要做任何事，主页已经显示
        // 以后想加"点通知跳到记账页"，就在这里写逻辑
    }
    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("from_notification", false) == true) {
//            val type = intent.getStringExtra("reminder_type")
            // TODO: 这里写你的跳转逻辑，比如跳到记账录入页
            // navigateToAddBill(type)

        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                // 跳转到系统设置页，让用户手动开启"闹钟和提醒"
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }


}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QApp() {
    val bottomBarRoute = listOf(
        BottomBarRoute.Home,
        BottomBarRoute.Calendar,
        BottomBarRoute.Statistic,
        BottomBarRoute.Asset,
        BottomBarRoute.My
    )
    val pagerCount = bottomBarRoute.size
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var selectedRouteIndex by rememberSaveable { mutableIntStateOf(0) }
    // 为每个主页面维护独立NavBackStack
    val backStacks = List(pagerCount) { idx ->
        rememberNavBackStack(bottomBarRoute[idx].route)
    }
    val pagerState = rememberPagerState(
        initialPage = selectedRouteIndex,
        pageCount = { pagerCount }
    )
    val isBottomBarVisible =
        bottomBarRoute.any { it.route == backStacks[pagerState.currentPage].lastOrNull() }

    // 切换底部导航或滑动时只切换索引，不重置backStack
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        if (selectedRouteIndex != pagerState.currentPage) {
            selectedRouteIndex = pagerState.currentPage
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) {
                    bottomBarRoute.forEachIndexed { idx, route ->
                        NavigationBarItem(
                            selected = selectedRouteIndex == idx,
                            icon = { Icon(route.Icon, route.label) },
                            label = { Text(route.label) },
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(idx)
                                }
                            }
                        )
                    }
                }
            }
        }) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) { page ->
            val backStack = backStacks[page]
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    HomeRouteBuilder(backStack)
                    CalendarRouteBuilder(backStack)
                    AssetRouteBuilder(backStack)
                    MyRouteBuilder(backStack)
                    StatisticRouteBuilder(backStack)
                    UiRouteBuilder(backStack)
                }
            )
        }
    }
}
