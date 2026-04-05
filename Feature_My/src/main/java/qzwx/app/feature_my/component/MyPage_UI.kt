package qzwx.app.feature_my.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 模拟信用卡数据
data class CardData(
    val organization: String,
    val number: String,
    val expire: String,
    val holder: String
)
@Preview(showBackground = true)
@Composable
fun MyPage_Card() {
    val rotated = remember { mutableStateOf(false) }  // 用于判断是否翻转
    val currentDate = LocalDate.now()
    val formattedDate = currentDate.format(DateTimeFormatter.ofPattern("MM/yy"))  //当前日期
    val result = remember {
        mutableStateOf(
            CardData(
                "七种文学-高级会员",
                "2022 0012 0005 0000",
                formattedDate,    //当期日期
                "七种文学-用户名"
            )
        )
    }
    // 卡片旋转动画
    val rotation = animateFloatAsState(
        targetValue = if (rotated.value) 180f else 0f,
        animationSpec = tween(500)
    ).value
    // 前面的透明度动画
    val animateFront = animateFloatAsState(
        targetValue = if (!rotated.value) 1f else 0f,
        animationSpec = tween(500)
    ).value
    // 后面的透明度动画
    val animateBack = animateFloatAsState(
        targetValue = if (rotated.value) 1f else 0f,
        animationSpec = tween(500)
    ).value
    // 根据卡片组织选择对应的图标
    Card(
        modifier = Modifier
            .height(220.dp)
            .fillMaxWidth()
            .padding(10.dp)
            .graphicsLayer {
                rotationY = rotation  // 控制旋转动画
                cameraDistance = 8 * density  // 增加立体效果
            }
            .clickable {
                rotated.value = !rotated.value  // 点击时触发旋转
            },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // 正面内容
        if (!rotated.value) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 8.dp
                ), // 增加左右的padding
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(
                        painter = painterResource(qzwx.app.feature_my.R.drawable.openai),
                        contentDescription = "OpenAi",
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                            .padding(top = 8.dp, bottom = 6.dp)
                            .graphicsLayer { alpha = animateFront },  // 控制透明度
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(qzwx.app.feature_my.R.drawable.visa),
                        contentDescription = "Visa",
                        modifier = Modifier
                            .size(50.dp)
                            .graphicsLayer { alpha = animateFront },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = result.value.number,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .graphicsLayer { alpha = animateFront },  // 控制透明度
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp)) // 控制垂直间距
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Card Holder",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer { alpha = animateFront }
                        )
                        Text(
                            text = result.value.holder,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer { alpha = animateFront }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Current Date",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer { alpha = animateFront }
                        )
                        Text(
                            text = result.value.expire,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer { alpha = animateFront }
                        )
                    }
                }
                // 已记录的文本
                Text(
                    text = "坚持第520天,已记录500条账单",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp)
                        .graphicsLayer { alpha = animateFront }
                        .fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        // 反面内容
        else {
            Column(
                modifier = Modifier.padding(top = 20.dp),
            ) {
                Divider(
                    modifier = Modifier
                        .graphicsLayer { alpha = animateBack },  // 控制透明度
                    color = Color.Black,
                    thickness = 50.dp
                )
                Text(
                    text = "welcome To AccountBook !",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .background(Color.White)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = animateBack
                            rotationY = rotation  // 反面也进行旋转
                        }
                        .padding(10.dp),
                    fontSize = 15.sp,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Developed by QZWX",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = animateBack
                            rotationY = rotation
                        }
                        .padding(end = 15.dp),
                    fontWeight = FontWeight.Thin,
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


