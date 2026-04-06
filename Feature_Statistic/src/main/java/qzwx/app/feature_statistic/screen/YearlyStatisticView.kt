package qzwx.app.feature_statistic.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_statistic.viewmodel.StatisticPage_VM
import qzwx.app.module_data.entity.BillType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@Composable
fun YearlyStatisticView(
    viewModel: StatisticPage_VM = hiltViewModel(),
    backStack: NavBackStack<NavKey>? = null
) {
    val allBills by viewModel.allBills.collectAsState(initial = emptyList())

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.yearlyListIndex,
        initialFirstVisibleItemScrollOffset = viewModel.yearlyListOffset
    )
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.yearlyListIndex = listState.firstVisibleItemIndex
        viewModel.yearlyListOffset = listState.firstVisibleItemScrollOffset
    }

    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    var isHeatmapExpense by remember { mutableStateOf(true) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgoundColor = MaterialTheme.colorScheme.background
    val yearsList = remember {
        (0..19).map { LocalDate.now().year - it }
    }



    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(backgoundColor)
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
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(
                        animationSpec = tween(
                            400
                        )
                    )
                },
                label = "YearlyContentAnim"
            ) { targetYear ->

                val targetYearlyBills = allBills.filter { bill ->
                    val date = Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    date.year == targetYear
                }
                val targetYearExpense =
                    targetYearlyBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                val targetYearIncome =
                    targetYearlyBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
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
                            // Heatmap Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "收支热力",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

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
                                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(
                                        animationSpec = tween(
                                            400
                                        )
                                    )
                                },
                                label = "HeatmapContentAnim"
                            ) { targetIsExpense ->
                                val typeToFilter =
                                    if (targetIsExpense) BillType.EXPENSE else BillType.INCOME
                                val activeColor =
                                    if (targetIsExpense) Color(0xFFF44336) else Color(0xFF4CAF50)
                                val inactiveColor = Color(0xFFEEEEEE)

                                val heatBills = targetYearlyBills.filter { it.type == typeToFilter }
                                val billDates = heatBills.map {
                                    Instant.ofEpochMilli(it.createdAt)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
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
                                                val isLeap =
                                                    java.time.Year.isLeap(targetYear.toLong())
                                                val daysInMonth =
                                                    java.time.Month.of(monthIndex).length(isLeap)
                                                val firstDateOfMonth =
                                                    LocalDate.of(targetYear, monthIndex, 1)
                                                val firstDayOfWeek =
                                                    firstDateOfMonth.dayOfWeek.value // 1 (Mon) - 7 (Sun)

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.Start
                                                ) {
                                                    Text(
                                                        text = "${monthIndex}月",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(
                                                            bottom = 6.dp,
                                                            start = 2.dp
                                                        )
                                                    )
                                                    // Draw month mini calendar map
                                                    val weeks =
                                                        Math.ceil((daysInMonth + firstDayOfWeek - 1) / 7.0)
                                                            .toInt()
                                                    for (w in 0 until 6) { // Draw exactly 6 rows to keep all month grids equal height
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            for (d in 1..7) {
                                                                val dayIndex = w * 7 + d
                                                                val dateDay =
                                                                    dayIndex - firstDayOfWeek + 1
                                                                if (dateDay in 1..daysInMonth) {
                                                                    val currentDate = LocalDate.of(
                                                                        targetYear,
                                                                        monthIndex,
                                                                        dateDay
                                                                    )
                                                                    val cellColor =
                                                                        if (billDates.contains(
                                                                                currentDate
                                                                            )
                                                                        ) activeColor else inactiveColor
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .aspectRatio(1f)
                                                                            .padding(1.dp)
                                                                            .clip(
                                                                                RoundedCornerShape(
                                                                                    50
                                                                                )
                                                                            )
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
                        }
                    }


                    Spacer(modifier = Modifier.height(24.dp))


                    qzwx.app.module_ui.components.Shared_CategoryPieChart(
                        bills = targetYearlyBills,
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

                    // Multi-year Bar Chart Header
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
                                    text = "历年收支",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                qzwx.app.module_ui.components.Shared_ChartLegend()
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val maxAmount = remember(allBills) {
                                yearsList.maxOfOrNull { year ->
                                    val bills = allBills.filter {
                                        Instant.ofEpochMilli(it.createdAt)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate().year == year
                                    }
                                    val e = bills.filter { it.type == BillType.EXPENSE }
                                        .sumOf { it.amount }
                                    val i = bills.filter { it.type == BillType.INCOME }
                                        .sumOf { it.amount }
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
                                    val bills = allBills.filter {
                                        Instant.ofEpochMilli(it.createdAt)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate().year == year
                                    }
                                    val expense =
                                        bills.filter { it.type == BillType.EXPENSE }
                                            .sumOf { it.amount }
                                    val income =
                                        bills.filter { it.type == BillType.INCOME }
                                            .sumOf { it.amount }

                                    val expFraction =
                                        if (startChartAnimation && expense > 0) (expense / maxAmount).toFloat()
                                            .coerceAtLeast(0.02f) else 0f
                                    val incFraction =
                                        if (startChartAnimation && income > 0) (income / maxAmount).toFloat()
                                            .coerceAtLeast(0.02f) else 0f

                                    val expAnim by animateFloatAsState(
                                        targetValue = expFraction,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "ExpAnim"
                                    )
                                    val incAnim by animateFloatAsState(
                                        targetValue = incFraction,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
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
                                                        text = if (expense % 1.0 == 0.0) expense.toInt()
                                                            .toString() else String.format(
                                                            Locale.getDefault(),
                                                            "%.1f",
                                                            expense
                                                        ),
                                                        fontSize = 9.sp,
                                                        color = Color.Gray,
                                                        modifier = Modifier.wrapContentWidth(
                                                            unbounded = true
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight(expAnim)
                                                        .background(
                                                            Color(0xFFF44336),
                                                            RoundedCornerShape(
                                                                topStart = 4.dp,
                                                                topEnd = 4.dp
                                                            )
                                                        )
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
                                                        text = if (income % 1.0 == 0.0) income.toInt()
                                                            .toString() else String.format(
                                                            Locale.getDefault(),
                                                            "%.1f",
                                                            income
                                                        ),
                                                        fontSize = 9.sp,
                                                        color = Color.Gray,
                                                        modifier = Modifier.wrapContentWidth(
                                                            unbounded = true
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight(incAnim)
                                                        .background(
                                                            Color(0xFF4CAF50),
                                                            RoundedCornerShape(
                                                                topStart = 4.dp,
                                                                topEnd = 4.dp
                                                            )
                                                        )
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
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                }
            }
        }
    }
}
