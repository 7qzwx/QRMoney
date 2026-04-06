package qzwx.app.feature_statistic.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import qzwx.app.feature_statistic.component.StatisticThreeWayToggle
import qzwx.app.feature_statistic.viewmodel.MonthlyChartState
import qzwx.app.feature_statistic.viewmodel.StatisticPage_VM
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun MonthlyStatisticView(
    viewModel: StatisticPage_VM = hiltViewModel(),
    backStack: NavBackStack<NavKey>? = null
) {
    val selectedDate by viewModel.selectedMonth.collectAsState()
    val monthlyState by viewModel.monthlyChartState.collectAsState(initial = MonthlyChartState())
    val allBills by viewModel.allBills.collectAsState(initial = emptyList())

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.monthlyListIndex,
        initialFirstVisibleItemScrollOffset = viewModel.monthlyListOffset
    )
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.monthlyListIndex = listState.firstVisibleItemIndex
        viewModel.monthlyListOffset = listState.firstVisibleItemScrollOffset
    }

    var lineChartType by remember {
        mutableStateOf(0) // 0: 支出, 1: 收入, 2: 混合
    }
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface


    val monthsList = remember {
        (0..24).map { LocalDate.now().withDayOfMonth(1).minusMonths(it.toLong()) }
    }

    var showMonthYearPicker by remember { mutableStateOf(false) }

    qzwx.app.module_ui.components.Shared_MonthYearPickerDialog(
        visible = showMonthYearPicker,
        currentMonth = selectedDate,
        onConfirm = { newDate ->
            viewModel.selectMonth(newDate)
            showMonthYearPicker = false
        },
        onDismiss = { showMonthYearPicker = false }
    )

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        item {
            // Year and Month LazyRow Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { showMonthYearPicker = true }
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedDate.year}年",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Year Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

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
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(
                        animationSpec = tween(
                            400
                        )
                    )
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

                    val monthlyBillsForPie = allBills.filter { bill ->
                        val date =
                            Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        date.year == targetDate.year && date.month == targetDate.month
                    }
                    qzwx.app.module_ui.components.Shared_CategoryPieChart(
                        bills = monthlyBillsForPie,
                        onCategoryClick = { categoryName, categoryBills ->
                            backStack?.add(
                                qzwx.app.module_ui.nav3.UIRoute.CategoryBills(
                                    categoryName,
                                    categoryBills
                                )
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Line Chart Header
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "收支统计",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                StatisticThreeWayToggle(
                                    selectedType = lineChartType,
                                    onTypeSelected = { lineChartType = it },
                                    primaryColor = primaryColor,
                                    surfaceColor = surfaceColor
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val filteredLineChartData =
                                remember(monthlyState.theLineChartData, lineChartType) {
                                    val baseData = when (lineChartType) {
                                        0 -> monthlyState.theLineChartData.filter { it.label == "支出" }
                                        1 -> monthlyState.theLineChartData.filter { it.label == "收入" }
                                        else -> monthlyState.theLineChartData
                                    }
                                    baseData.map { line ->
                                        if (line.label == "收入") {
                                            line.copy(
                                                color = SolidColor(Color(0xFF4CAF50)),
                                                firstGradientFillColor = Color(0xFF4CAF50).copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                        } else {
                                            line.copy(
                                                color = SolidColor(Color(0xFFF44336)),
                                                firstGradientFillColor = Color(0xFFF44336).copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                        }
                                    }
                                }

                            // Line Chart
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                if (monthlyState.isReady && filteredLineChartData.isNotEmpty()) {
                                    LineChart(
                                        modifier = Modifier.fillMaxSize(),
                                        data = filteredLineChartData,
                                        labelProperties = LabelProperties(
                                            rotation = LabelProperties.Rotation(
                                                degree = 0f
                                            ),
                                            enabled = true,
                                            labels = (1..selectedDate.lengthOfMonth()).map { if (it % 2 != 0) it.toString() else " " },
                                            textStyle = TextStyle(fontSize = 9.sp)
                                        ),
                                        indicatorProperties = HorizontalIndicatorProperties(
                                            enabled = false,
                                            textStyle = TextStyle(fontSize = 9.sp)
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
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                }
            }
        }
    }
}