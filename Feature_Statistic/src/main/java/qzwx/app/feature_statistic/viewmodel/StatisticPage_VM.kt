package qzwx.app.feature_statistic.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import qzwx.app.feature_statistic.repository.StatisticPage_RT
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.models.DrawStyle
import javax.inject.Inject

data class MonthlyChartState(
    val monthExpense: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthBalance: Double = 0.0,
    val barDataList: List<Bars> = emptyList(),
    val theLineChartData: List<Line> = emptyList(),
    val isReady: Boolean = false
)

@HiltViewModel
class StatisticPage_VM @Inject constructor(
    private val repository: StatisticPage_RT
) : ViewModel() {
    val allBills: Flow<List<QBillEntity>> = repository.allBills

    // User's selected month start date
    private val _selectedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val selectedMonth = _selectedMonth.asStateFlow()

    fun selectMonth(date: LocalDate) {
        _selectedMonth.value = date
    }

    // Load only the exact month's bills from the database using flatMapLatest
    val currentMonthBills: Flow<List<QBillEntity>> = _selectedMonth.flatMapLatest { date ->
        val startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = date.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        repository.getBillsBetween(startMillis, endMillis)
    }.flowOn(Dispatchers.IO)

    // Move heavy data processing to ViewModel running on Default dispatcher
    val monthlyChartState: Flow<MonthlyChartState> = combine(_selectedMonth, currentMonthBills) { date, bills ->
        val primaryColor = Color(0xFF6750A4) // Fallback hardcoded if we can't use MaterialTheme here
        val errorColor = Color(0xFFB3261E)

        val monthExpense = bills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
        val monthIncome = bills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
        val monthBalance = monthIncome - monthExpense

        val daysInMonth = date.lengthOfMonth()

        val expensesByDay = bills
            .filter { it.type == BillType.EXPENSE }
            .groupBy {
                Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth
            }
            .mapValues { (_, b) -> b.sumOf { it.amount } }

        val incomesByDay = bills
            .filter { it.type == BillType.INCOME }
            .groupBy {
                Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth
            }
            .mapValues { (_, b) -> b.sumOf { it.amount } }

        val barDataList = (1..daysInMonth).map { day ->
            val dailyExpense = expensesByDay[day] ?: 0.0
            val dailyIncome = incomesByDay[day] ?: 0.0
            val labelStr = if (day % 2 != 0) day.toString() else " "

            Bars(
                label = labelStr,
                values = listOf(
                    Bars.Data(label = "支出", value = dailyExpense, color = SolidColor(errorColor)),
                    Bars.Data(label = "收入", value = dailyIncome, color = SolidColor(primaryColor))
                )
            )
        }

        val expensesList = (1..daysInMonth).map { expensesByDay[it] ?: 0.0 }
        val incomesList = (1..daysInMonth).map { incomesByDay[it] ?: 0.0 }

        val theLineChartData = listOf(
            Line(
                label = "支出",
                values = expensesList,
                color = SolidColor(errorColor),
                curvedEdges = true,
                drawStyle = DrawStyle.Stroke(width = 2.dp),
                firstGradientFillColor = Color.Red.copy(alpha = .5f),
                secondGradientFillColor = Color.Transparent
            ),
            Line(
                label = "收入",
                values = incomesList,
                color = SolidColor(primaryColor),
                curvedEdges = true,
                drawStyle = DrawStyle.Stroke(width = 2.dp),
                firstGradientFillColor = Color.Green.copy(alpha = .5f),
                secondGradientFillColor = Color.Transparent
            )
        )

        MonthlyChartState(
            monthExpense = monthExpense,
            monthIncome = monthIncome,
            monthBalance = monthBalance,
            barDataList = barDataList,
            theLineChartData = theLineChartData,
            isReady = true
        )
    }.flowOn(Dispatchers.Default)
}
