package qzwx.app.feature_statistic.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.*
import qzwx.app.feature_statistic.viewmodel.MonthlyChartState
import qzwx.app.feature_statistic.viewmodel.StatisticPage_VM
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_ui.data.billCategoryList
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

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
fun DailyStatisticView(viewModel: StatisticPage_VM = hiltViewModel()) {
    val allBills by viewModel.allBills.collectAsState(initial = emptyList())

    var isExpense by remember { mutableStateOf(true) }
    var isLast7Days by remember { mutableStateOf(true) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isLast7Days = !isLast7Days }
                    .padding(4.dp)
            ) {
                Text(
                    text = if (isLast7Days) "近7日统计" else "本周统计",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap Dates",
                    tint = Color.Gray
                )
            }

            // Togge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, primaryColor, RoundedCornerShape(8.dp))
            ) {
                val expenseBgColor by animateColorAsState(if (isExpense) primaryColor else surfaceColor, label = "ExpenseBg")
                val expenseTextColor by animateColorAsState(if (isExpense) MaterialTheme.colorScheme.onPrimary else primaryColor, label = "ExpenseText")
                val incomeBgColor by animateColorAsState(if (!isExpense) primaryColor else surfaceColor, label = "IncomeBg")
                val incomeTextColor by animateColorAsState(if (!isExpense) MaterialTheme.colorScheme.onPrimary else primaryColor, label = "IncomeText")

                Box(
                    modifier = Modifier
                        .background(expenseBgColor)
                        .clickable { isExpense = true }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "支出",
                        color = expenseTextColor,
                        fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Box(
                    modifier = Modifier
                        .background(incomeBgColor)
                        .clickable { isExpense = false }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "收入",
                        color = incomeTextColor,
                        fontWeight = if (!isExpense) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedContent(
            targetState = Pair(isExpense, isLast7Days),
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "DailyContentAnim"
        ) { (targetIsExpense, targetIsLast7Days) ->

            var targetStartAnimation by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                targetStartAnimation = true
            }

            val localDates = remember(targetIsLast7Days) {
                if (targetIsLast7Days) {
                    (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
                } else {
                    val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    (0..6).map { monday.plusDays(it.toLong()) }
                }
            }

            val localChartData = remember(targetIsExpense, localDates, allBills) {
                val typeToFilter = if (targetIsExpense) BillType.EXPENSE else BillType.INCOME
                val aggregatedData = allBills
                    .filter { it.type == typeToFilter }
                    .groupBy { Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate() }
                    .mapValues { (_, bills) -> bills.sumOf { it.amount } }

                localDates.map { date ->
                    val label = if (targetIsLast7Days && date == LocalDate.now()) "今天" else date.dayOfWeek.toChinese()
                    val value = aggregatedData[date] ?: 0.0
                    ChartData(label, value)
                }
            }

            val localTotal = localChartData.sumOf { it.value }
            val localAvg = if (localChartData.isNotEmpty()) localTotal / localChartData.size else 0.0
            val localMaxValue = localChartData.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1.0

            Column(modifier = Modifier.fillMaxWidth()) {
                // Chart Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    // Bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        localChartData.forEach { data ->
                            val targetFraction = if (targetStartAnimation && data.value > 0) {
                                (data.value / localMaxValue).toFloat().coerceAtLeast(0.02f)
                            } else 0f

                            val animatedFraction by animateFloatAsState(
                                targetValue = targetFraction,
                                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                                label = "BarAnimation"
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                if (data.value > 0) {
                                    Text(
                                        text = if (data.value % 1.0 == 0.0) data.value.toInt()
                                            .toString() else data.value.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                if (animatedFraction > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .fillMaxHeight(animatedFraction)
                                            .background(
                                                color = if (targetIsExpense) errorColor else primaryColor,
                                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                            )
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height(0.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        localChartData.forEach { data ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(text = data.label, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Custom Summary Line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (targetIsExpense) "总支出" else "总收入",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", localTotal),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (targetIsExpense) "日均支出" else "日均收入",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", localAvg),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyStatisticView(viewModel: StatisticPage_VM = hiltViewModel()) {
    val selectedDate by viewModel.selectedMonth.collectAsState()
    val monthlyState by viewModel.monthlyChartState.collectAsState(initial = MonthlyChartState())
    val allBills by viewModel.allBills.collectAsState(initial = emptyList())

    var isPieChartExpense by remember { mutableStateOf(true) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    val monthsList = remember {
        (0..24).map { LocalDate.now().withDayOfMonth(1).minusMonths(it.toLong()) }
    }

    var selectedPieCategory by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(vertical = 16.dp)
    ) {
        item {
            // Year and Month LazyRow Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "${selectedDate.year}年",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(monthsList) { _, monthDate ->
                        val isSelected = selectedDate == monthDate
                        val todayM = LocalDate.now().withDayOfMonth(1)
                        val titleStr = when {
                            monthDate == todayM -> "本月"
                            monthDate == todayM.minusMonths(1) -> "上月"
                            monthDate.year == todayM.year - 1 && monthDate.monthValue == 12 -> "去年12月"
                            else -> "${monthDate.monthValue}月"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) primaryColor else surfaceColor)
                                .clickable {
                                    viewModel.selectMonth(monthDate)
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) primaryColor else Color.LightGray,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = titleStr,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "月支出",
                                color = onPrimaryContainer.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    monthlyState.monthExpense
                                ),
                                color = onPrimaryContainer,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "月收入",
                                color = onPrimaryContainer.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    monthlyState.monthIncome
                                ),
                                color = onPrimaryContainer,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "月结余",
                                color = onPrimaryContainer.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    monthlyState.monthBalance
                                ),
                                color = onPrimaryContainer,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "收支统计", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (monthlyState.isReady && monthlyState.barDataList.isNotEmpty()) {
                    ColumnChart(
                        animationDelay = 5,
                        modifier = Modifier.fillMaxSize(),
                        data = monthlyState.barDataList,
                        barProperties = BarProperties(
                            spacing = 2.dp,
                            cornerRadius = Bars.Data.Radius.Rectangle(
                                topRight = 4.dp,
                                topLeft = 4.dp
                            ),
                            thickness = 4.dp
                        ),
                        labelProperties = LabelProperties(
                            enabled = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp),
                            rotation = LabelProperties.Rotation(
                                degree = 0f
                            )
                        ),
                        indicatorProperties = HorizontalIndicatorProperties(
                            enabled = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp)
                        ),
                        gridProperties = GridProperties(
                            xAxisProperties = GridProperties.AxisProperties(enabled = false),
                            yAxisProperties = GridProperties.AxisProperties(
                                enabled = true, color = SolidColor(
                                    Color.LightGray.copy(alpha = 0.5f)
                                )
                            )
                        ),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Line Chart Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "收支趋势", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (monthlyState.isReady && monthlyState.theLineChartData.isNotEmpty()) {
                    LineChart(
                        modifier = Modifier.fillMaxSize(),
                        data = monthlyState.theLineChartData,
                        labelProperties = LabelProperties(
                            rotation = LabelProperties.Rotation(
                                degree = 0f
                            ),
                            enabled = true,
                            labels = (1..selectedDate.lengthOfMonth()).map { if (it % 2 != 0) it.toString() else " " },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp)
                        ),
                        indicatorProperties = HorizontalIndicatorProperties(
                            enabled = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp)
                        ),
                        gridProperties = GridProperties(
                            xAxisProperties = GridProperties.AxisProperties(enabled = false),
                            yAxisProperties = GridProperties.AxisProperties(
                                enabled = true, color = SolidColor(
                                    Color.LightGray.copy(alpha = 0.5f)
                                )
                            )
                        ),
                        curvedEdges = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pie Chart Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "分类占比", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // Toggle for Pie Chart
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, primaryColor, RoundedCornerShape(8.dp))
                ) {
                    val pieExpenseBgColor by animateColorAsState(if (isPieChartExpense) primaryColor else surfaceColor, label = "PieExpenseBg")
                    val pieExpenseTextColor by animateColorAsState(if (isPieChartExpense) MaterialTheme.colorScheme.onPrimary else primaryColor, label = "PieExpenseText")
                    val pieIncomeBgColor by animateColorAsState(if (!isPieChartExpense) primaryColor else surfaceColor, label = "PieIncomeBg")
                    val pieIncomeTextColor by animateColorAsState(if (!isPieChartExpense) MaterialTheme.colorScheme.onPrimary else primaryColor, label = "PieIncomeText")

                    Box(
                        modifier = Modifier
                            .background(pieExpenseBgColor)
                            .clickable { isPieChartExpense = true }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "支出",
                            color = pieExpenseTextColor,
                            fontWeight = if (isPieChartExpense) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(pieIncomeBgColor)
                            .clickable { isPieChartExpense = false }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "收入",
                            color = pieIncomeTextColor,
                            fontWeight = if (!isPieChartExpense) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = isPieChartExpense,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "PieChartContentAnim"
            ) { targetIsPieChartExpense ->

                // Pie Chart Content
                val pieBills = allBills.filter { bill ->
                    val date = Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    date.year == selectedDate.year && date.month == selectedDate.month &&
                            bill.type == if (targetIsPieChartExpense) BillType.EXPENSE else BillType.INCOME
                }
                val totalPieAmount = pieBills.sumOf { it.amount }
                val categoryGroups =
                    pieBills.groupBy { it.category }.mapValues { it.value.sumOf { b -> b.amount } }
                        .toList().sortedByDescending { it.second }

                // Reset selected category if there's no matching group
                LaunchedEffect(categoryGroups) {
                    if (selectedPieCategory != null && categoryGroups.find { it.first == selectedPieCategory } == null) {
                        selectedPieCategory = null
                    }
                }

                val colors = listOf(
                    Color(0xFFF44336),
                    Color(0xFF2196F3),
                    Color(0xFFFF9800),
                    Color(0xFF4CAF50),
                    Color(0xFF9C27B0),
                    Color(0xFF00BCD4),
                    Color(0xFFFFEB3B),
                    Color(0xFF795548)
                )

                if (categoryGroups.isNotEmpty() && totalPieAmount > 0) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                            ) {
                                PieChart(
                                    onPieClick = { pie ->
                                        selectedPieCategory =
                                            if (selectedPieCategory == pie.label) null else pie.label
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    selectedPaddingDegree = 4f,
                                    labelHelperProperties = LabelHelperProperties(enabled = false),
                                    selectedScale = 1.2f,
                                    data = categoryGroups.mapIndexed { index, pair ->
                                        Pie(
                                            label = pair.first,
                                            data = pair.second,
                                            color = colors[index % colors.size],
                                            selectedColor = colors[index % colors.size].copy(alpha = 0.5f),
                                            selected = pair.first == selectedPieCategory
                                        )
                                    },
                                    style = Pie.Style.Fill
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                categoryGroups.forEachIndexed { index, pair ->
                                    val percent = (pair.second / totalPieAmount) * 100
                                    val isPieSelected = pair.first == selectedPieCategory
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isPieSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable {
                                                selectedPieCategory =
                                                    if (isPieSelected) null else pair.first
                                            }
                                            .padding(all = if (isPieSelected) 4.dp else 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    colors[index % colors.size],
                                                    RoundedCornerShape(50)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${pair.first} (${
                                                String.format(
                                                    Locale.getDefault(),
                                                    "%.1f",
                                                    percent
                                                )
                                            }%, ¥${
                                                String.format(
                                                    Locale.getDefault(),
                                                    "%.1f",
                                                    pair.second
                                                )
                                            })",
                                            fontSize = 12.sp,
                                            fontWeight = if (isPieSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            categoryGroups.forEachIndexed { index, pair ->
                                val percent = (pair.second / totalPieAmount) * 100
                                val cat = billCategoryList.find { it.name == pair.first }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (cat != null) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = cat.name,
                                            tint = colors[index % colors.size],
                                            modifier = Modifier.size(32.dp)
                                )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    colors[index % colors.size],
                                                    RoundedCornerShape(50)
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = pair.first,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "¥${
                                                    String.format(
                                                        Locale.getDefault(),
                                                        "%.2f",
                                                        pair.second
                                                    )
                                                }", fontSize = 14.sp, fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (percent / 100).toFloat() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = colors[index % colors.size],
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${
                                                String.format(
                                                    Locale.getDefault(),
                                                    "%.1f",
                                                    percent
                                                )
                                            }%", fontSize = 12.sp, color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "暂无数据", color = Color.Gray)
                    }
                }
            }
        }
    }
}
