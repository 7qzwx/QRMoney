package qzwx.app.module_ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_ui.components.Shared_BillDayGroup
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBillsPage(
    categoryName: String,
    bills: List<QBillEntity>,
    onBackClick: () -> Unit,
    onItemClick: (QBillEntity) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
            val billsByDate = bills.groupBy {
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.createdAt),
                    ZoneId.systemDefault()
                ).toLocalDate()
            }.toSortedMap(compareByDescending { it })

            if (bills.isEmpty()) {
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
                                onEditBill = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("编辑账单功能暂未支持")
                                    }
                                },
                                onDeleteBill = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("删除账单功能暂未支持")
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

