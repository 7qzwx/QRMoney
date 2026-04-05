package qzwx.app.module_ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_ui.components.Shared_AddNewBill
import qzwx.app.module_ui.components.Shared_BillDayGroup
import qzwx.app.module_ui.viewmodel.CategoryBills_VM
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBillsPage(
    categoryName: String,
    bills: List<QBillEntity>,
    onBackClick: () -> Unit,
    onItemClick: (QBillEntity) -> Unit,
    viewModel: CategoryBills_VM = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var localBills by remember(bills) { mutableStateOf(bills) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var draftBill by remember { mutableStateOf<QBillEntity?>(null) }

    if (showBottomSheet) {
        Shared_AddNewBill(
            initialBill = draftBill,
            defaultDate = LocalDate.now(),
            onDismissRequest = {
                showBottomSheet = false
                draftBill = null
            },
            onSaveRequest = { modifiedBill ->
                coroutineScope.launch {
                    viewModel.insertBill(modifiedBill)

                    // Update local ui list manually
                    localBills = localBills.filterNot { it.id == modifiedBill.id } + modifiedBill

                    showBottomSheet = false
                    draftBill = null

                    snackbarHostState.showSnackbar(
                        message = "已更新账单",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$categoryName 账单详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val billsByDate = localBills.groupBy {
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.createdAt),
                    ZoneId.systemDefault()
                ).toLocalDate()
            }.toSortedMap(compareByDescending { it })

            if (localBills.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
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
                                dailyBills.filter { it.type == BillType.EXPENSE }
                                    .sumOf { it.amount }
                            val incomeTotal =
                                dailyBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }

                            Shared_BillDayGroup(
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
                                    coroutineScope.launch {
                                        viewModel.deleteBill(billToDelete)
                                        localBills = localBills.filterNot { it.id == billToDelete.id }
                                        val result = snackbarHostState.showSnackbar(
                                            message = "已删除账单",
                                            actionLabel = "撤销",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.insertBill(billToDelete)
                                            localBills = localBills + billToDelete
                                        }
                                    }
                                },
                                onItemClick = onItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}
