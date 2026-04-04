package qzwx.app.feature_calendart.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import qzwx.app.feature_calendart.compenent.CalendarDayCell
import qzwx.app.feature_calendart.compenent.CalendarTopBar
import qzwx.app.feature_calendart.compenent.DaysOfWeekTitle
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_ui.components.Shared_BillDayGroup
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.animation.*
import qzwx.app.module_ui.components.Shared_MonthYearPickerDialog
import qzwx.app.module_data.entity.QBillEntity
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import qzwx.app.module_ui.components.Shared_AddNewBill

@Composable
fun CalendarPage(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val bills by viewModel.bills.collectAsState()

    val currentMonth = remember { YearMonth.now() }
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(today) } // 默认选择今天

    val startMonth = remember { currentMonth.minusMonths(100) } // 允许向前滑动100个月
    val endMonth = remember { currentMonth.plusMonths(100) }   // 允许向后滑动100个月
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    val coroutineScope = rememberCoroutineScope()
    var showMonthPicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showBottomSheet by remember { mutableStateOf(false) }
    var draftBill by remember { mutableStateOf<QBillEntity?>(null) }

    if (showBottomSheet) {
        Shared_AddNewBill(
            initialBill = draftBill,
            defaultDate = selectedDate ?: today,
            onDismissRequest = {
                showBottomSheet = false
            },
            onSaveRequest = { bill ->
                coroutineScope.launch {
                    draftBill = bill
                    val insertedId = viewModel.insertAndReturnId(bill)
                    showBottomSheet = false
                    val result = snackbarHostState.showSnackbar(
                        message = "已新增账单",
                        actionLabel = "撤销",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.deleteBill(bill.copy(id = insertedId))
                        showBottomSheet = true
                    } else {
                        draftBill = null
                    }
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                draftBill = null
                showBottomSheet = true
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "新增账单"
                )
            }
        },
        topBar = {
            CalendarTopBar(
                currentMonth = state.firstVisibleMonth.yearMonth, // 更新为当前日历滑动到的真实月份
                onTodayClick = {
                    selectedDate = today // 选择定回今天
                    coroutineScope.launch {
                        state.animateScrollToMonth(YearMonth.now()) // 日历视图滚动回今天所在的月份
                    }
                },
                onMonthClick = { showMonthPicker = true }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 星期头部 (Sun, Mon, Tue...)
            DaysOfWeekTitle(daysOfWeek = daysOfWeek)

            // 日历主体
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    // 过滤出当天的账单
                    val dayBills = bills.filter { bill ->
                        val billDate = Instant.ofEpochMilli(bill.createdAt)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        billDate == day.date
                    }

                    // 计算当天的支出和收入总和
                    val expenseSum =
                        dayBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                    val incomeSum =
                        dayBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }

                    val finalExpense = if (expenseSum > 0) expenseSum else null
                    val finalIncome = if (incomeSum > 0) incomeSum else null

                    CalendarDayCell(
                        day = day,
                        expense = finalExpense,
                        income = finalIncome,
                        isToday = day.date == today,
                        isSelected = day.date == selectedDate,
                        onClick = { selectedDate = it.date }
                    )
                }
            )

            // 下方展示选中日期的所有账单记录
            AnimatedContent(
                targetState = selectedDate,
                transitionSpec = {
                    (slideInHorizontally { width -> width / 2 } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width / 2 } + fadeOut())
                },
                label = "daily_bills_animation",
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) { animatedDate ->
                val selectedDateBills = bills.filter { bill ->
                    val billDate = Instant.ofEpochMilli(bill.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    animatedDate != null && billDate == animatedDate
                }

                if (selectedDateBills.isNotEmpty()) {
                    val selectedDayOfWeekStr = when (animatedDate?.dayOfWeek?.value) {
                        1 -> "星期一"
                        2 -> "星期二"
                        3 -> "星期三"
                        4 -> "星期四"
                        5 -> "星期五"
                        6 -> "星期六"
                        7 -> "星期日"
                        else -> ""
                    }
                    val expenseTotal = selectedDateBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                    val incomeTotal = selectedDateBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
                    val dateStr = "${animatedDate?.monthValue}月${animatedDate?.dayOfMonth}日"

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Shared_BillDayGroup(
                                dateStr = dateStr,
                                dayOfWeekStr = selectedDayOfWeekStr,
                                expenseTotal = expenseTotal,
                                incomeTotal = incomeTotal,
                                bills = selectedDateBills,
                                onEditBill = { billToEdit ->
                                    draftBill = billToEdit
                                    showBottomSheet = true
                                },
                                onDeleteBill = { billToDelete ->
                                    coroutineScope.launch {
                                        viewModel.deleteBill(billToDelete)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "已删除账单",
                                            actionLabel = "撤销",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.insertAndReturnId(billToDelete)
                                        }
                                    }
                                }
                            )
                        }
                    }
                } else {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("当天没有记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Shared_MonthYearPickerDialog(
            visible = showMonthPicker,
            currentMonth = state.firstVisibleMonth.yearMonth.atDay(1),
            onConfirm = { newDate ->
                showMonthPicker = false
                val targetYearMonth = java.time.YearMonth.of(newDate.year, newDate.monthValue)
                coroutineScope.launch {
                    state.animateScrollToMonth(targetYearMonth)
                }
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}