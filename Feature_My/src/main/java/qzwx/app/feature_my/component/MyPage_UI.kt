package qzwx.app.feature_my.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiNature
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun UserInfoCard(
    daysCount: Int,
    transactionCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0F2F1)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 背景装饰
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                // 左侧装饰植物
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(60.dp)
                ) {
                    // 简化的植物装饰
                    Icon(
                        imageVector = Icons.Outlined.EmojiNature,
                        contentDescription = null,
                        tint = Color(0x334CAF50),
                        modifier = Modifier.size(60.dp)
                    )
                }
                // 右侧装饰植物
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiNature,
                        contentDescription = null,
                        tint = Color(0x334CAF50),
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            // 用户信息内容
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 用户头像
                Image(
                    painter = painterResource(id = qzwx.app.feature_my.R.drawable.qzwxlogo),
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(
                            2.dp,
                            Color.White,
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))
                // 用户名
                Text(
                    text = "七种文学",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(8.dp))
                // 记账信息 - 使用动态计算的数据
                Text(
                    text = "坚持记账的第${daysCount}天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )

                Text(
                    text = "已记录${transactionCount}条账单",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}


@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    trailing: String? = null,
    trailingColor: Color = Color.Gray,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,  // 去除点击波纹效果
                onClick = onClick
            )
            .padding(
                vertical = 16.dp,
                horizontal = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧图标
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(24.dp)
        )
        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )
        // 可选的尾部文本
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodyMedium,
                color = trailingColor,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        // 右箭头
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = "下一步",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
