package qzwx.app.module_ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import qzwx.app.module_ui.viewmodel.SharedBillViewModel
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_ui.components.Shared_BillDayGroup
import qzwx.app.module_ui.nav3.UIRoute
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSearchPage(
    backStack: NavBackStack<NavKey>,
    walletTypeName: String? = null
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SharedBillViewModel = hiltViewModel(activity)

    val allBills by viewModel.allBills.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Date Range state
    var dateRangePickerState = rememberDateRangePickerState()
    var showDateRangePicker by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter logic
    val filteredBills = remember(
        searchQuery,
        allBills,
        dateRangePickerState.selectedStartDateMillis,
        dateRangePickerState.selectedEndDateMillis,
        walletTypeName
    ) {
        allBills.filter { bill ->
            val matchesWallet = if (walletTypeName != null) {
                bill.walletType.name == walletTypeName
            } else {
                true
            }

            val matchesQuery = if (searchQuery.isNotBlank()) {
                bill.remark?.contains(searchQuery, ignoreCase = true) == true ||
                        bill.category.contains(searchQuery, ignoreCase = true)
            } else {
                true
            }

            val startMillis = dateRangePickerState.selectedStartDateMillis
            val endMillis = dateRangePickerState.selectedEndDateMillis

            val matchesDate = if (startMillis != null && endMillis != null) {
                // dateRangePickerState millis are UTC, normalize to system default local date for comparison
                val startLocalDate =
                    Instant.ofEpochMilli(startMillis).atZone(ZoneOffset.UTC).toLocalDate()
                val endLocalDate =
                    Instant.ofEpochMilli(endMillis).atZone(ZoneOffset.UTC).toLocalDate()

                val billLocalDate =
                    Instant.ofEpochMilli(bill.createdAt).atZone(ZoneId.systemDefault())
                        .toLocalDate()

                !billLocalDate.isBefore(startLocalDate) && !billLocalDate.isAfter(endLocalDate)
            } else {
                true
            }

            matchesWallet && matchesQuery && matchesDate
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search category or remark...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDateRangePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Filter Date",
                            tint = if (dateRangePickerState.selectedStartDateMillis != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
            // Group and sorted bills
            val billsByDate = filteredBills.groupBy {
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.createdAt),
                    ZoneId.systemDefault()
                ).toLocalDate()
            }.toSortedMap(compareByDescending { it })

            if (filteredBills.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No bills found",
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
                                    // TODO: Could implement editing from search page if needed
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Editing from search is not supported yet.")
                                    }
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
    }

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dateRangePickerState.setSelection(null, null)
                    showDateRangePicker = false
                }) {
                    Text("Clear")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "Select Date Range",
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}