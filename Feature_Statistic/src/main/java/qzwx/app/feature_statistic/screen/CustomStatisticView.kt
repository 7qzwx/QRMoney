package qzwx.app.feature_statistic.screen

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import qzwx.app.feature_statistic.component.StatisticThreeWayToggle
import qzwx.app.feature_statistic.viewmodel.StatisticPage_VM
import qzwx.app.module_data.entity.BillType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomStatisticView(
    viewModel: StatisticPage_VM = hiltViewModel(),
    backStack: NavBackStack<NavKey>? = null
) {
    val allBills by viewModel.allBills.collectAsState(initial = emptyList())

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.customListIndex,
        initialFirstVisibleItemScrollOffset = viewModel.customListOffset
    )
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.customListIndex = listState.firstVisibleItemIndex
        viewModel.customListOffset = listState.firstVisibleItemScrollOffset
    }

    val calendarState = rememberUseCaseState()

    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    var lineChartType by remember {
        mutableStateOf(0) // 0: 支出, 1: 收入, 2: 混合
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background
    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            yearSelection = true,
            monthSelection = true
        ),
        selection = CalendarSelection.Period { start, end ->
            startDate = start
            endDate = end
        }
    )

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(vertical = 16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${startDate.year}年${startDate.monthValue}月${startDate.dayOfMonth}日 - ${endDate.year}年${endDate.monthValue}月${endDate.dayOfMonth}日",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date Range",
                    tint = primaryColor,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { calendarState.show() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val filteredBills = remember(allBills, startDate, endDate) {
                allBills.filter { bill ->
                    val date = Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    !date.isBefore(startDate) && !date.isAfter(endDate)
                }
            }

            val totalExpense = remember(filteredBills) {
                filteredBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
            }

            val totalIncome = remember(filteredBills) {
                filteredBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
            }

            val totalBalance = totalIncome - totalExpense

            qzwx.app.module_ui.components.Shared_SummaryCard(
                expenseLabel = "总支出",
                expenseAmount = totalExpense,
                incomeLabel = "总收入",
                incomeAmount = totalIncome,
                balanceLabel = "总结余",
                balanceAmount = totalBalance
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
                        Text(text = "收支趋势", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        StatisticThreeWayToggle(
                            selectedType = lineChartType,
                            onTypeSelected = { lineChartType = it },
                            primaryColor = primaryColor,
                            surfaceColor = surfaceColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val dateList = remember(startDate, endDate) {
                        val list = mutableListOf<LocalDate>()
                        var current = startDate
                        while (!current.isAfter(endDate)) {
                            list.add(current)
                            current = current.plusDays(1)
                        }
                        list
                    }

                    val expenseDataList = remember(filteredBills, dateList) {
                        dateList.map { date ->
                            filteredBills.filter { bill ->
                                val billDate =
                                    Instant.ofEpochMilli(bill.createdAt)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                billDate == date && bill.type == BillType.EXPENSE
                            }.sumOf { it.amount }
                        }
                    }

                    val incomeDataList = remember(filteredBills, dateList) {
                        dateList.map { date ->
                            filteredBills.filter { bill ->
                                val billDate =
                                    Instant.ofEpochMilli(bill.createdAt)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                billDate == date && bill.type == BillType.INCOME
                            }.sumOf { it.amount }
                        }
                    }

                    val lineChartDataList =
                        remember(expenseDataList, incomeDataList, lineChartType) {
                            val data = mutableListOf<Line>()
                            if (lineChartType == 0 || lineChartType == 2) {
                                data.add(
                                    Line(
                                        label = "支出",
                                        values = expenseDataList,
                                        color = SolidColor(Color(0xFFF44336)),
                                        firstGradientFillColor = Color(0xFFF44336).copy(alpha = 0.5f),
                                        secondGradientFillColor = Color.Transparent,
                                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                        gradientAnimationDelay = 1000,
                                        drawStyle = DrawStyle.Stroke(width = 2.dp)
                                    )
                                )
                            }
                            if (lineChartType == 1 || lineChartType == 2) {
                                data.add(
                                    Line(
                                        label = "收入",
                                        values = incomeDataList,
                                        color = SolidColor(Color(0xFF4CAF50)),
                                        firstGradientFillColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                                        secondGradientFillColor = Color.Transparent,
                                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                        gradientAnimationDelay = 1000,
                                        drawStyle = DrawStyle.Stroke(width = 2.dp)
                                    )
                                )
                            }
                            data
                        }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        if (lineChartDataList.isNotEmpty() && dateList.isNotEmpty()) {
                            val labels = List(dateList.size) { index ->
                                if (index == 0) "${dateList.first().monthValue}-${dateList.first().dayOfMonth}"
                                else if (index == dateList.size - 1) "${dateList.last().monthValue}-${dateList.last().dayOfMonth}"
                                else " "
                            }

                            LineChart(
                                modifier = Modifier.fillMaxSize(),
                                data = lineChartDataList,
                                labelProperties = LabelProperties(
                                    rotation = LabelProperties.Rotation(degree = 0f),
                                    enabled = true,
                                    labels = labels,
                                    textStyle = TextStyle(
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                ),
                                indicatorProperties = HorizontalIndicatorProperties(
                                    enabled = false,
                                ),
                                gridProperties = GridProperties(
                                    xAxisProperties = GridProperties.AxisProperties(enabled = false),
                                    yAxisProperties = GridProperties.AxisProperties(enabled = false)
                                ),
                                curvedEdges = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            qzwx.app.module_ui.components.Shared_CategoryPieChart(
                bills = filteredBills,
                onCategoryClick = { categoryName, categoryBills ->
                    backStack?.add(
                        qzwx.app.module_ui.nav3.UIRoute.CategoryBills(
                            categoryName,
                            categoryBills
                        )
                    )
                }
            )
        }
    }
}

