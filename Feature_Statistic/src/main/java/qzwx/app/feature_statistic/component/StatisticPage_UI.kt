package qzwx.app.feature_statistic.component


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek

data class ChartData(val label: String, val value: Double)

fun DayOfWeek.toChinese(): String = when (this) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}


@Composable
fun StatisticThreeWayToggle(
    selectedType: Int, // 0: 支出, 1: 收入, 2: 混合
    onTypeSelected: (Int) -> Unit,
    primaryColor: Color,
    surfaceColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, primaryColor, RoundedCornerShape(8.dp))
    ) {
        val types = listOf("支出", "收入", "混合")
        types.forEachIndexed { index, title ->
            val isSelected = selectedType == index
            val bgColor by animateColorAsState(
                if (isSelected) primaryColor else surfaceColor,
                label = "ToggleBg$index"
            )
            val textColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else primaryColor,
                label = "ToggleText$index"
            )
            Box(
                modifier = Modifier
                    .background(bgColor)
                    .clickable { onTypeSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            }
        }
    }
}







