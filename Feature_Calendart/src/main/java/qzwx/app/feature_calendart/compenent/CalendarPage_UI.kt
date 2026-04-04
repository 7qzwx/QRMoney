package qzwx.app.feature_calendart.compenent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.DayOfWeek
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    currentMonth: YearMonth,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${currentMonth.year}年${currentMonth.monthValue}月",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onTodayClick) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "回到今天"
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun CalendarDayCell(
    day: CalendarDay,
    expense: Double? = null,
    income: Double? = null,
    isToday: Boolean = false,
    isSelected: Boolean = false,
    onClick: (CalendarDay) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (day.position != DayPosition.MonthDate) {
        // 如果不是当月日期（如上个月或下个月），渲染空白区域隐藏
        Spacer(modifier = modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = 1.dp))
        return
    }

    val isSelectedBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val highlightBorder = if (isToday) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)) else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(1.dp) // 外边距避免边框粘连
            .clip(RoundedCornerShape(4.dp))
            .background(isSelectedBg) // 选中日期用实心边框(相当于background)
            .then(highlightBorder)     // 今天日期用空心边框
            .clickable { onClick(day) }
            .padding(vertical = 2.dp, horizontal = 1.dp), // 减少了 inner padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top // 默认紧凑排列
    ) {
        val textColor = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else if (isToday) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        // 日期
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )

        val hasData = expense != null || income != null

        // 取消默认字体内边距以最大程度减小行间距
        val compactTextStyle = TextStyle(
            fontSize = 10.sp,
            lineHeight = 10.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )

        // 第一行：支出(红) 或 “无”(灰) 或 “-0”(红，当只有收入时) 或 空白占位
        Text(
            text = if (expense != null) "-${expense}"
            else if (income != null) "-0"
            else if (!hasData) "无"
            else " ",
            color = if (expense != null || income != null) Color.Red else Color.Gray,
            style = compactTextStyle,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        // 第二行：收入(绿) 或 空白占位
        Text(
            text = if (income != null) "+${income}" else " ",
            color = if (income != null) Color(0xFF4CAF50) else Color.Transparent,
            style = compactTextStyle,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
