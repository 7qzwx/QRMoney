package qzwx.app.feature_statistic.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwipeDown
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_statistic.component.ChartData
import qzwx.app.feature_statistic.component.toChinese
import qzwx.app.feature_statistic.viewmodel.StatisticPage_VM
import qzwx.app.module_data.entity.BillType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun DailyStatisticView(
    viewModel: StatisticPage_VM = hiltViewModel(),
    backStack: NavBackStack<NavKey>? = null
) {
    val allBills by viewModel.allBills.collectAsState(initial = emptyList())

    val scrollState = rememberScrollState(initial = viewModel.dailyScrollPosition)
    LaunchedEffect(scrollState.value) {
        viewModel.dailyScrollPosition = scrollState.value
    }

    var isExpense by remember { mutableStateOf(true) }
    var isLast7Days by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    val localDates = remember(isLast7Days) {
        if (isLast7Days) {
            (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
        } else {
            val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            (0..6).map { monday.plusDays(it.toLong()) }
        }
    }

    val summaryExpense = remember(allBills, localDates) {
        allBills.filter { bill ->
            val billDate =
                Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
            localDates.contains(billDate) && bill.type == BillType.EXPENSE
        }.sumOf { it.amount }
    }

    val summaryIncome = remember(allBills, localDates) {
        allBills.filter { bill ->
            val billDate =
                Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
            localDates.contains(billDate) && bill.type == BillType.INCOME
        }.sumOf { it.amount }
    }

    val summaryBalance = summaryIncome - summaryExpense


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        qzwx.app.module_ui.components.Shared_SummaryCard(
            expenseLabel = "支出",
            expenseAmount = summaryExpense,
            incomeLabel = "收入",
            incomeAmount = summaryIncome,
            balanceLabel = if (isLast7Days) "近七日结余" else "本周的结余",
            balanceAmount = summaryBalance
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = {
                                isLast7Days = !isLast7Days
                            })
                        ) {
                            Text(
                                text =
                                    if (isLast7Days) "近七天统计" else "本周的统计",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = if (isLast7Days) Icons.Default.SwipeUp else Icons.Default.SwipeDown,
                                contentDescription = "Swap Dates",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(0.dp)
                            )
                        }
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Details",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(0.dp)
                            )
                        }
                    }

                    // Togge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, primaryColor, RoundedCornerShape(8.dp))
                    ) {
                        val expenseBgColor by animateColorAsState(
                            if (isExpense) primaryColor else MaterialTheme.colorScheme.surface,
                            label = "ExpenseBg"
                        )
                        val expenseTextColor by animateColorAsState(
                            if (isExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                            label = "ExpenseText"
                        )
                        val incomeBgColor by animateColorAsState(
                            if (!isExpense) primaryColor else MaterialTheme.colorScheme.surface,
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
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "支出",
                                color = expenseTextColor,
                                fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(incomeBgColor)
                                .clickable { isExpense = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "收入",
                                color = incomeTextColor,
                                fontWeight = if (!isExpense) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedContent(
                    targetState = Pair(isExpense, isLast7Days),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(
                            animationSpec = tween(
                                400
                            )
                        )
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
                                LocalDate.now()
                                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            (0..6).map { monday.plusDays(it.toLong()) }
                        }
                    }

                    val localChartData = remember(targetIsExpense, localDates, allBills) {
                        val typeToFilter =
                            if (targetIsExpense) BillType.EXPENSE else BillType.INCOME
                        val aggregatedData = allBills
                            .filter { it.type == typeToFilter }
                            .groupBy {
                                Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                            .mapValues { (_, bills) -> bills.sumOf { it.amount } }

                        localDates.map { date ->
                            val dayStr =
                                if (targetIsLast7Days && date == LocalDate.now()) "今天" else date.dayOfWeek.toChinese()
                            val value = aggregatedData[date] ?: 0.0
                            ChartData(dayStr, value)
                        }
                    }

                    val localTotal = localChartData.sumOf { it.value }
                    val localAvg =
                        if (localChartData.isNotEmpty()) localTotal / localChartData.size else 0.0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        // Chart Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .padding(8.dp)
                        ) {
                            if (localChartData.isNotEmpty()) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        val maxValue = localChartData.maxOfOrNull { it.value }
                                            ?.takeIf { it > 0 } ?: 1.0
                                        val barColor =
                                            if (targetIsExpense) Color(0xFFF44336) else Color(
                                                0xFF4CAF50
                                            )

                                        localChartData.forEach { data ->
                                            val fraction = (data.value / maxValue).toFloat()
                                                .coerceAtLeast(0.01f)
                                            val isZero = data.value == 0.0

                                            val animFraction by animateFloatAsState(
                                                targetValue = if (targetStartAnimation && !isZero) fraction else 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                ),
                                                label = "BarAnim"
                                            )

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                            ) {
                                                if (!isZero) {
                                                    Text(
                                                        text = if (data.value % 1.0 == 0.0) data.value.toInt()
                                                            .toString() else String.format(
                                                            Locale.getDefault(),
                                                            "%.1f",
                                                            data.value
                                                        ),
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.6f)
                                                        .weight(1f, fill = false)
                                                        .fillMaxHeight(animFraction)
                                                        .clip(
                                                            RoundedCornerShape(
                                                                topStart = 6.dp,
                                                                topEnd = 6.dp,
                                                                bottomStart = 2.dp,
                                                                bottomEnd = 2.dp
                                                            )
                                                        )
                                                        .background(barColor)
                                                )
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color.LightGray.copy(alpha = 0.5f))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        localChartData.forEach { data ->
                                            Text(
                                                text = data.label,
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                lineHeight = 12.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("总金额", color = Color.Gray, fontSize = 12.sp)
                                        Text(
                                            text = String.format("¥ %.2f", localTotal),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text("日均金额", color = Color.Gray, fontSize = 12.sp)
                                        Text(
                                            text = String.format("¥ %.2f", localAvg),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val dailyPieBills = allBills.filter { bill ->
            val billDate =
                Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault())
                    .toLocalDate()
            localDates.contains(billDate)
        }

        qzwx.app.module_ui.components.Shared_CategoryPieChart(
            bills = dailyPieBills,
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

    }
}
