package qzwx.app.feature_my.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSettingPage(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = { /* 返回上一页 */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 安全锁卡片
            SecurityLockCard()

            Spacer(modifier = Modifier.height(16.dp))
            // 偏好设置卡片
            PreferenceSettingsCard()
        }
    }
}

@Composable
fun SecurityLockCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "安全锁",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
            // 生物解锁
            SecurityLockItem(
                title = "生物解锁",
                description = "打开应用时执行指纹、人脸等安全认证"
            )

            Spacer(modifier = Modifier.height(16.dp))
            // 手势解锁
            SecurityLockItem(
                title = "手势解锁",
                description = "启动应用时或指纹识别失败时进行认证"
            )
        }
    }
}

@Composable
fun SecurityLockItem(
    title: String,
    description: String
) {
    val switchState = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { switchState.value = !switchState.value },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Switch(
            checked = switchState.value,
            onCheckedChange = { switchState.value = it },
        )
    }
}

@Composable
fun PreferenceSettingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "偏好设置",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
            // 预设偏好设置项
            PreferenceSettingItems()
        }
    }
}

@Composable
fun PreferenceSettingItems() {
    // 从图片中可以看到的设置项
    val settingItems = listOf(
        "快速操作" to "悬浮球、快速记账、快速导入等设置",
        "首页预算显示" to "在应用首页显示预算模块",
        "隐藏手动记账按钮" to "打开后,\"添加一条新记账\"按钮将在右上角",
        "智能提示" to "智能记账时显示历史识别记录",
        "历史备注提示" to "记账时输入备注将实时匹配历史备注",
        "账户余额不足提醒" to "当账户可用余额小于0时进行提醒",
        "日历还款信息" to "在日历视图中显示信贷账户的待还账单"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        settingItems.forEachIndexed { index, (title, description) ->
            // 为最后一项移除分隔线
            PreferenceSettingItem(title, description)

            if (index < settingItems.size - 1) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PreferenceSettingItem(title: String, description: String) {
    val switchState = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { switchState.value = !switchState.value }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // 对于特定项目，显示不同的尾部内容
        if (title == "账单月起始日") {
            Text(
                text = "1号",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
        } else if (title == "日历周起始日") {
            Text(
                text = "周日",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
        } else if (title == "默认账户") {
            Text(
                text = "沿用分类账户",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
        } else {
            Switch(
                checked = switchState.value,
                onCheckedChange = { switchState.value = it }
            )
        }
    }
}