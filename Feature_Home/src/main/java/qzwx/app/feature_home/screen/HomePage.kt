package qzwx.app.feature_home.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import qzwx.app.feature_home.components.Home_SnakBar
import qzwx.app.feature_home.components.Home_TopBar
import qzwx.app.feature_home.components.Home_TotalCard
import qzwx.app.feature_home.viewmodel.HomePage_VM
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_ui.components.Shared_AddNewBill
import qzwx.app.module_ui.nav3.UIRoute
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: HomePage_VM = hiltViewModel(),
    backStack: NavBackStack<NavKey>
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showBottomSheet by remember { mutableStateOf(false) }


    // Draft state for undo functionality
    var draftBill by remember { mutableStateOf<QBillEntity?>(null) }

    val allBills by viewModel.getAllBills().collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    var userSelectedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }


    LaunchedEffect(listState) {
        var prevIndex = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collectLatest { (index, offset) ->
                val isScrollingDown = index > prevIndex || (index == prevIndex && offset > prevOffset)
                val isAtTop = index == 0 && offset == 0
                fabVisible = isAtTop || !isScrollingDown
                prevIndex = index
                prevOffset = offset
            }
    }


    if (showBottomSheet) {
        Shared_AddNewBill(
            initialBill = draftBill,
            onDismissRequest = {
                showBottomSheet = false
                draftBill = null
            },
            onSaveRequest = { bill ->
                scope.launch {
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
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Home_TopBar(
                scrollBehavior = scrollBehavior,
                onSearchClick = { backStack.add(UIRoute.Search()) }
            )
        },
        snackbarHost = { Home_SnakBar(snackbarHostState) },  //SnakBar用于新增数据后重新编辑
        floatingActionButton = {

                AnimatedVisibility(
                    visible = fabVisible,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    FloatingActionButton(
                        onClick = {
                            draftBill = null
                            showBottomSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新增账单"
                        )
                    }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {

            item {
                // Determine the "current" month to display
                val defaultDate = if (allBills.isNotEmpty()) {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(allBills.first().createdAt),
                        ZoneId.systemDefault()
                    ).toLocalDate()
                } else {
                    LocalDateTime.now().toLocalDate()
                }
                val displayedDate = userSelectedDate ?: defaultDate

                val currentYearMonth = "${displayedDate.year}年${displayedDate.monthValue}月"

                val currentMonthBills = allBills.filter {
                    val billDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it.createdAt),
                        ZoneId.systemDefault()
                    ).toLocalDate()
                    billDate.year == displayedDate.year && billDate.monthValue == displayedDate.monthValue
                }

                val previousMonthDate = displayedDate.minusMonths(1)
                val previousMonthBills = allBills.filter {
                    val billDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it.createdAt),
                        ZoneId.systemDefault()
                    ).toLocalDate()
                    billDate.year == previousMonthDate.year && billDate.monthValue == previousMonthDate.monthValue
                }

                val currentExpense =
                    currentMonthBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                val currentIncome =
                    currentMonthBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }

                val previousExpense =
                    previousMonthBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                val previousIncome =
                    previousMonthBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }

                val currentNet = currentExpense - currentIncome
                val previousNet = previousExpense - previousIncome

                val trendPercentStr = if (previousNet != 0.0) {
                    val percent = if (previousNet < 0 && currentNet < 0) {
                        ((Math.abs(currentNet) - Math.abs(previousNet)) / Math.abs(previousNet)) * 100
                    } else {
                        ((currentNet - previousNet) / Math.abs(previousNet)) * 100
                    }

                    val sign = if (percent >= 0) "+" else ""
                    "${sign}${String.format(java.util.Locale.getDefault(), "%.0f", percent)}%"
                } else {
                    if (currentNet > 0) "+100%" else if (currentNet < 0) "+100%" else "0%"
                }

                Home_TotalCard(
                    currentYearMonth = currentYearMonth,
                    totalExpense = currentExpense,
                    totalIncome = currentIncome,
                    netBalance = currentNet, // pass net balance
                    trendPercent = trendPercentStr,
                    currentDate = displayedDate,
                    onMonthSelected = { newDate ->
                        userSelectedDate = newDate
                    }
                )
            }

            // Group and sorted bills
            val billsByDate = allBills.groupBy {
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.createdAt),
                    ZoneId.systemDefault()
                )
                    .toLocalDate()
            }.toSortedMap(compareByDescending { it })

            val today = java.time.LocalDate.now()

            billsByDate.forEach { (date, dailyBills) ->
                item {
                    val dateStr = when (date) {
                        today -> "今日"
                        today.minusDays(1) -> "昨日"
                        today.plusDays(1) -> "明日"
                        else -> "${date.monthValue}月${date.dayOfMonth}日"
                    }
                    val dayOfWeekStr = when (date.dayOfWeek.value) {
                        1 -> "星期一"
                        2 -> "星期二"
                        3 -> "星期三"
                        4 -> "星期四"
                        5 -> "星期五"
                        6 -> "星期六"
                        else -> "星期日"
                    }

                    val expenseTotal =
                        dailyBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                    val incomeTotal =
                        dailyBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }

                    qzwx.app.module_ui.components.Shared_BillDayGroup(
                        dateStr = dateStr,
                        dayOfWeekStr = dayOfWeekStr,
                        expenseTotal = expenseTotal,
                        incomeTotal = incomeTotal,
                        bills = dailyBills,
                        onEditBill = { billToEdit ->
                            draftBill = billToEdit
                            showBottomSheet = true
                        },
                        onDeleteBill = { billToDelete ->
                            scope.launch {
                                viewModel.deleteBill(billToDelete)
                                val result = snackbarHostState.showSnackbar(
                                    message = "已删除账单",
                                    actionLabel = "撤销",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.insertBill(billToDelete)
                                }
                            }
                        },
                        onItemClick = { clickedBill ->
                            backStack.add(UIRoute.BillDetail(clickedBill))
                        }
                    )
                }
            }
        }
    }
}
