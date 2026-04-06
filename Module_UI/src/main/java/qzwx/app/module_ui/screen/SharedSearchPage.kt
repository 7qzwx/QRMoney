package qzwx.app.module_ui.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_ui.components.Shared_AddNewBill
import qzwx.app.module_ui.components.Shared_BillDayGroup
import qzwx.app.module_ui.nav3.UIRoute
import qzwx.app.module_ui.viewmodel.SharedBillViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

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
                        placeholder = { Text("在此搜索种类或者备注...") },
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
                                onEditBill = { clickedBill ->
                                    draftBill = clickedBill
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
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
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
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                    subheadContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationContentColor = MaterialTheme.colorScheme.onSurface,
                    yearContentColor = MaterialTheme.colorScheme.onSurface,
                    currentYearContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledSelectedDayContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    disabledSelectedDayContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                title = {
                    Text(
                        text = "选择日期范围",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 24.dp),
                        textAlign = TextAlign.Center
                    )
                },
                headline = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val startText = dateRangePickerState.selectedStartDateMillis?.let {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        } ?: "开始日期"
                        val endText = dateRangePickerState.selectedEndDateMillis?.let {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        } ?: "结束日期"

                        Text(startText, fontSize = 14.sp)
                        Text("-", fontSize = 14.sp)
                        Text(endText, fontSize = 14.sp)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}