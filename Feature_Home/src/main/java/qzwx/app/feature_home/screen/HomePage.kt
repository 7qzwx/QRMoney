package qzwx.app.feature_home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import qzwx.app.feature_home.components.*
import qzwx.app.feature_home.viewmodel.HomePage_VM
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: HomePage_VM = hiltViewModel()
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    // Draft state for undo functionality
    var draftBill by remember { mutableStateOf<QBillEntity?>(null) }

    val allBills by viewModel.getAllBills().collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        AddNewBill(
            initialBill = draftBill,
            onDismissRequest = {
                showBottomSheet = false
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
        topBar = { Home_TopBar() },
        snackbarHost = { Home_SnakBar(snackbarHostState) },  //SnakBar用于新增数据后重新编辑
        containerColor = MaterialTheme.colorScheme.background, // 在 Scaffold 级别设置整体背景色,
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding()
                )
        ) {

            item {
                Button(onClick = {
                    showBottomSheet = true
                }) {
                    Text(text = "新增账单")
                }
            }
            item {
                Home_TotalCard()
            }

            // Group and sorted bills
            val billsByDate = allBills.groupBy {
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.createdAt),
                    ZoneId.systemDefault()
                )
                    .toLocalDate()
            }.toSortedMap(compareByDescending { it })

            billsByDate.forEach { (date, dailyBills) ->
                item {
                    val dateStr = "${date.monthValue}月${date.dayOfMonth}日"
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

                    Home_BillDayGroup(
                        dateStr = dateStr,
                        dayOfWeekStr = dayOfWeekStr,
                        expenseTotal = expenseTotal,
                        incomeTotal = incomeTotal,
                        bills = dailyBills
                    )
                }
            }
        }
    }
}
