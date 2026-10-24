package qzwx.app.feature_my.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_my.component.SettingItem
import qzwx.app.feature_my.component.SettingsCard
import qzwx.app.feature_my.component.UserInfoCard
import qzwx.app.feature_my.nav3.MyRoute
import qzwx.app.feature_my.viewmodel.MyPage_VM

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ContextCastToActivity")
@Composable
fun MyPage(
    backStack: NavBackStack<NavKey>
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel1: DevTestViewModel = hiltViewModel(activity)
    val viewModel: MyPage_VM = hiltViewModel(activity)
    val myDateState by viewModel.myDate.collectAsState()
    val actualTransactionCount by viewModel.actualTransactionCount.collectAsState()

    // 检查并初始化数据库中的首次运行时间
    LaunchedEffect(myDateState) {
        if (myDateState == null) {
            viewModel.initializeFirstLaunchDate(System.currentTimeMillis())
        }
    }

    // 计算天数
    val daysCount = myDateState?.firstLaunchDate?.let {
        calculateDaysSinceFirstLaunch(it)
    } ?: 1

    // 账单数量从数据库的实际账单表获取
    val transactionCount = actualTransactionCount

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 16.dp),
        ) {
            item {
                UserInfoCard(
                    daysCount = daysCount,
                    transactionCount = transactionCount
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsCard {
                    SettingItem(
                        icon = Icons.Outlined.CloudSync,
                        title = "云同步",
                        trailing = "开发ing",
                        trailingColor = Color.Red,
                        onClick = {
//                          云同步操作
                        }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "数据备份",
                        onClick = {
                            backStack.add(MyRoute.DataBackUp)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsCard {
                    SettingItem(
                        icon = Icons.Outlined.Wallpaper,
                        title = "主题外观",
                        trailing = "开发ing",

                        onClick = { /* 主题外观点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.GifBox,
                        title = "愿望清单",
                        onClick = { backStack.add(MyRoute.QWish) }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.Widgets,
                        title = "桌面小部件",
                        onClick = { backStack.add(MyRoute.QWidgets) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsCard {
                    SettingItem(
                        icon = Icons.Outlined.Help,
                        title = "使用手册",
                        trailing = "开发ing",

                        onClick = { /* 使用手册点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.Person,
                        title = "邀请好友",
                        trailing = "开发ing",

                        onClick = { /* 邀请好友点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "建议与反馈",
                        trailing = "开发ing",

                        onClick = { /* 建议与反馈点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.Star,
                        title = "给应用评分",
                        trailing = "开发ing",

                        onClick = { /* 给应用评分点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.Info,
                        title = "关于我们",
                        trailing = "开发ing",

                        onClick = { /* 关于我们点击 */ }
                    )
                }
            }
            // 底部ICP备案信息
            item {
                Text(
                    text = "©2026 版权归七种文学所有",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }
        }
    }
}


// 计算自首次启动以来的天数
private fun calculateDaysSinceFirstLaunch(firstLaunchDate: Long): Int {
    val firstCalendar = java.util.Calendar.getInstance().apply {
        timeInMillis = firstLaunchDate
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val currentCalendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val diffMillis = currentCalendar.timeInMillis - firstCalendar.timeInMillis
    return (java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMillis)
        .coerceAtLeast(0) + 1).toInt()
}
