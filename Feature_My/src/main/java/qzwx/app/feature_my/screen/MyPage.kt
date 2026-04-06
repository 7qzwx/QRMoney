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
import qzwx.app.feature_my.component.SettingItem
import qzwx.app.feature_my.component.SettingsCard
import qzwx.app.feature_my.component.UserInfoCard
import qzwx.app.feature_my.viewmodel.MyPage_VM
import java.util.concurrent.TimeUnit

private const val FIRST_LAUNCH_PREF = "first_launch_pref"
private const val FIRST_LAUNCH_DATE = "first_launch_date"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ContextCastToActivity")
@Composable
fun MyPage(
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
                        title = "云同步（插入模拟数据）",
                        trailing = "未开启，数据仅存本地",
                        trailingColor = Color.Red,
                        onClick = { viewModel1.insertMockData() }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "数据备份(删除所有数据)",
                        onClick = { viewModel1.clearAllData() }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "导入导出",
                        onClick = { /* 导入导出点击 */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsCard {
                    SettingItem(
                        icon = Icons.Outlined.Wallpaper,
                        title = "主题外观",
                        onClick = { /* 主题外观点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.Widgets,
                        title = "桌面小部件",
                        onClick = { /* 桌面小部件点击 */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsCard {
                    SettingItem(
                        icon = Icons.Outlined.Help,
                        title = "使用手册",
                        onClick = { /* 使用手册点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.Person,
                        title = "邀请好友",
                        onClick = { /* 邀请好友点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "建议与反馈",
                        onClick = { /* 建议与反馈点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.Star,
                        title = "给应用评分",
                        onClick = { /* 给应用评分点击 */ }
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingItem(
                        icon = Icons.Outlined.Info,
                        title = "关于我们",
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
    val currentTime = System.currentTimeMillis()
    val diffMillis = currentTime - firstLaunchDate
    return (TimeUnit.MILLISECONDS.toDays(diffMillis) + 1).toInt() // +1 包括首日
}
