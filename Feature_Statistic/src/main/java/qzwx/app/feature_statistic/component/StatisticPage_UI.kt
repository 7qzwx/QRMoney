package qzwx.app.feature_statistic.component

import android.R.attr.scaleX
import android.R.attr.scaleY
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
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
                val expenseBgColor by animateColorAsState(
                    if (isExpense) primaryColor else surfaceColor,
                    label = "ExpenseBg"
                )
                val expenseTextColor by animateColorAsState(
                    if (isExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                    label = "ExpenseText"
                )
                val incomeBgColor by animateColorAsState(
                    if (!isExpense) primaryColor else surfaceColor,
                    label = "IncomeBg"
                )
                val incomeTextColor by animateColorAsState(
                    if (!isExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                    label = "IncomeText"
                )

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
                    val monday =
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    (0..6).map { monday.plusDays(it.toLong()) }
                }
            }

            val localChartData = remember(targetIsExpense, localDates, allBills) {
                val typeToFilter = if (targetIsExpense) BillType.EXPENSE else BillType.INCOME
                val aggregatedData = allBills
                    .filter { it.type == typeToFilter }
                    .groupBy {
                        Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    .mapValues { (_, bills) -> bills.sumOf { it.amount } }

                localDates.map { date ->
                    val label =
                        if (targetIsLast7Days && date == LocalDate.now()) "今天" else date.dayOfWeek.toChinese()
                    val value = aggregatedData[date] ?: 0.0
                    ChartData(label, value)
                }
            }

            val localTotal = localChartData.sumOf { it.value }
            val localAvg =
                if (localChartData.isNotEmpty()) localTotal / localChartData.size else 0.0
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
                                animationSpec = tween(
                                    durationMillis = 800,
                                    easing = FastOutSlowInEasing
                                ),
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
                                                shape = RoundedCornerShape(
                                                    topStart = 6.dp,
                                                    topEnd = 6.dp
                                                )
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
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
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

            AnimatedContent(
                targetState = selectedDate,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "MonthlyContentAnim"
            ) { targetDate ->
                Column {
                    // Summary Card
                    qzwx.app.module_ui.components.Shared_SummaryCard(
                        expenseLabel = "月支出",
                        expenseAmount = monthlyState.monthExpense, // Note: these are derived from state based on selected month
                        incomeLabel = "月收入",
                        incomeAmount = monthlyState.monthIncome,
                        balanceLabel = "月结余",
                        balanceAmount = monthlyState.monthBalance
                    )

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
                        qzwx.app.module_ui.components.Shared_ChartLegend()
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
                                    enabled = false,
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
                        qzwx.app.module_ui.components.Shared_ChartLegend()
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
                                    enabled = false,
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

                    val monthlyBillsForPie = allBills.filter { bill ->
                        val date = Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                        date.year == targetDate.year && date.month == targetDate.month
                    }
                    qzwx.app.module_ui.components.Shared_CategoryPieChart(
                        bills = monthlyBillsForPie
                    )
                }
            }
        }
    }
}

@Composable
fun YearlyStatisticView(viewModel: StatisticPage_VM = hiltViewModel()) {
    val allBills by viewModel.allBills.collectAsState(initial = emptyList())

    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    var isHeatmapExpense by remember { mutableStateOf(true) }
    var isYearlyPieChartExpense by remember { mutableStateOf(true) }
    var selectedYearlyPieCategory by remember { mutableStateOf<String?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val errorColor = MaterialTheme.colorScheme.error

    val yearsList = remember {
        (0..19).map { LocalDate.now().year - it }
    }

    val yearlyBills = remember(allBills, selectedYear) {
        allBills.filter { bill ->
            val date = Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
            date.year == selectedYear
        }
    }

    val yearExpense = remember(yearlyBills) {
        yearlyBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
    }
    val yearIncome = remember(yearlyBills) {
        yearlyBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
    }
    val yearBalance = yearIncome - yearExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(vertical = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(yearsList) { _, year ->
                        val isSelected = selectedYear == year
                        val currentYear = LocalDate.now().year
                        val titleStr = when (year) {
                            currentYear -> "今年"
                            currentYear - 1 -> "去年"
                            else -> "${year}年"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) primaryColor else surfaceColor)
                                .clickable {
                                    selectedYear = year
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

            AnimatedContent(
                targetState = selectedYear,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "YearlyContentAnim"
            ) { targetYear ->

                val targetYearlyBills = allBills.filter { bill ->
                    val date = Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                    date.year == targetYear
                }
                val targetYearExpense = targetYearlyBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                val targetYearIncome = targetYearlyBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
                val targetYearBalance = targetYearIncome - targetYearExpense

                Column {
                    // Summary Card
                    qzwx.app.module_ui.components.Shared_SummaryCard(
                        expenseLabel = "年支出",
                        expenseAmount = targetYearExpense,
                        incomeLabel = "年收入",
                        incomeAmount = targetYearIncome,
                        balanceLabel = "年结余",
                        balanceAmount = targetYearBalance
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Heatmap Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "收支热力", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, primaryColor, RoundedCornerShape(8.dp))
                        ) {
                            val heatExpenseBgColor by animateColorAsState(
                                if (isHeatmapExpense) primaryColor else surfaceColor,
                                label = "HeatExpenseBg"
                            )
                            val heatExpenseTextColor by animateColorAsState(
                                if (isHeatmapExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                                label = "HeatExpenseText"
                            )
                            val heatIncomeBgColor by animateColorAsState(
                                if (!isHeatmapExpense) primaryColor else surfaceColor,
                                label = "HeatIncomeBg"
                            )
                            val heatIncomeTextColor by animateColorAsState(
                                if (!isHeatmapExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                                label = "HeatIncomeText"
                            )

                            Box(
                                modifier = Modifier
                                    .background(heatExpenseBgColor)
                                    .clickable { isHeatmapExpense = true }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "支出",
                                    color = heatExpenseTextColor,
                                    fontWeight = if (isHeatmapExpense) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(heatIncomeBgColor)
                                    .clickable { isHeatmapExpense = false }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "收入",
                                    color = heatIncomeTextColor,
                                    fontWeight = if (!isHeatmapExpense) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Heatmap Grid
                    AnimatedContent(
                        targetState = isHeatmapExpense,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "HeatmapContentAnim"
                    ) { targetIsExpense ->
                        val typeToFilter = if (targetIsExpense) BillType.EXPENSE else BillType.INCOME
                        val activeColor = if (targetIsExpense) Color(0xFFF44336) else Color(0xFF4CAF50)
                        val inactiveColor = Color(0xFFEEEEEE)

                        val heatBills = targetYearlyBills.filter { it.type == typeToFilter }
                        val billDates = heatBills.map {
                            Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                        }.toSet()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (row in 0 until 3) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    for (col in 0 until 4) {
                                        val monthIndex = row * 4 + col + 1
                                        val isLeap = java.time.Year.isLeap(targetYear.toLong())
                                        val daysInMonth = java.time.Month.of(monthIndex).length(isLeap)
                                        val firstDateOfMonth = LocalDate.of(targetYear, monthIndex, 1)
                                        val firstDayOfWeek = firstDateOfMonth.dayOfWeek.value // 1 (Mon) - 7 (Sun)

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                text = "${monthIndex}月",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                            )
                                            // Draw month mini calendar map
                                            val weeks = Math.ceil((daysInMonth + firstDayOfWeek - 1) / 7.0).toInt()
                                            for (w in 0 until 6) { // Draw exactly 6 rows to keep all month grids equal height
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    for (d in 1..7) {
                                                        val dayIndex = w * 7 + d
                                                        val dateDay = dayIndex - firstDayOfWeek + 1
                                                        if (dateDay in 1..daysInMonth) {
                                                            val currentDate = LocalDate.of(targetYear, monthIndex, dateDay)
                                                            val cellColor = if (billDates.contains(currentDate)) activeColor else inactiveColor
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .aspectRatio(1f)
                                                                    .padding(1.dp)
                                                                    .clip(RoundedCornerShape(50))
                                                                    .background(cellColor)
                                                            )
                                                        } else {
                                                            // Empty cell space maintaining proportion
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .aspectRatio(1f)
                                                                    .padding(1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (row < 2) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))


                    qzwx.app.module_ui.components.Shared_CategoryPieChart(
                        bills = targetYearlyBills
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Multi-year Bar Chart Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "历年收支", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        qzwx.app.module_ui.components.Shared_ChartLegend()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val maxAmount = remember(allBills) {
                        yearsList.maxOfOrNull { year ->
                            val bills = allBills.filter { Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate().year == year }
                            val e = bills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                            val i = bills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
                            maxOf(e, i)
                        }?.takeIf { it > 0 } ?: 1.0
                    }

                    var startChartAnimation by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { startChartAnimation = true }

                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                    val itemWidth = screenWidth / 5

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Bottom,
                        reverseLayout = true
                    ) {
                        items(yearsList) { year ->
                            val bills = allBills.filter { Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate().year == year }
                            val expense = bills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                            val income = bills.filter { it.type == BillType.INCOME }.sumOf { it.amount }

                            val expFraction = if (startChartAnimation && expense > 0) (expense / maxAmount).toFloat().coerceAtLeast(0.02f) else 0f
                            val incFraction = if (startChartAnimation && income > 0) (income / maxAmount).toFloat().coerceAtLeast(0.02f) else 0f

                            val expAnim by animateFloatAsState(
                                targetValue = expFraction,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "ExpAnim"
                            )
                            val incAnim by animateFloatAsState(
                                targetValue = incFraction,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "IncAnim"
                            )

                            Column(
                                modifier = Modifier
                                    .width(itemWidth)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    // Expense Bar
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier
                                            .width(20.dp)
                                            .fillMaxHeight()
                                    ) {
                                        if (expense > 0) {
                                            Text(
                                                text = if (expense % 1.0 == 0.0) expense.toInt().toString() else String.format(Locale.getDefault(), "%.1f", expense),
                                                fontSize = 9.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.wrapContentWidth(unbounded = true)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(expAnim)
                                                .background(Color(0xFFF44336), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        )
                                    }

                                    // Income Bar
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier
                                            .width(20.dp)
                                            .fillMaxHeight()
                                    ) {
                                        if (income > 0) {
                                            Text(
                                                text = if (income % 1.0 == 0.0) income.toInt().toString() else String.format(Locale.getDefault(), "%.1f", income),
                                                fontSize = 9.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.wrapContentWidth(unbounded = true)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(incAnim)
                                                .background(Color(0xFF4CAF50), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color.LightGray.copy(alpha = 0.5f))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${year}年",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}