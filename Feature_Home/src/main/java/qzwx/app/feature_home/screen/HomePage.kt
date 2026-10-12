package qzwx.app.feature_home.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import qzwx.app.feature_home.autobill.BillTextParser
import qzwx.app.feature_home.components.AutoBillVoiceBottomSheet
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
    var showVoiceSheet by remember { mutableStateOf(false) }
    var showVoiceConfirmDialog by remember { mutableStateOf(false) }
    var pendingVoiceTranscript by remember { mutableStateOf<String?>(null) }
    var isParsingVoiceTranscript by remember { mutableStateOf(false) }

    // Draft state for undo functionality
    var draftBill by remember { mutableStateOf<QBillEntity?>(null) }

    val allBills by viewModel.getAllBills().collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val recordAudioPermission = Manifest.permission.RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showVoiceSheet = true
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("需要麦克风权限才能使用离线语音记账")
            }
        }
    }

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

    if (showVoiceConfirmDialog && !pendingVoiceTranscript.isNullOrBlank()) {
        var editableConfirmText by remember(pendingVoiceTranscript) {
            mutableStateOf(pendingVoiceTranscript.orEmpty())
        }

        AlertDialog(
            onDismissRequest = {
                if (!isParsingVoiceTranscript) {
                    showVoiceConfirmDialog = false
                    pendingVoiceTranscript = null
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("确认识别内容", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "例子：昨天 9：26 支付宝 花了7块钱 买包子",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        val accentBrush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                            )
                        )
                        OutlinedTextField(
                            value = editableConfirmText,
                            onValueChange = { editableConfirmText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(accentBrush)
                                .padding(8.dp),
                            placeholder = { Text("可在此修改识别文本") },
                            singleLine = false,
                            maxLines = 8
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isParsingVoiceTranscript,
                    onClick = {
                        val transcript = editableConfirmText.trim()
                        if (transcript.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请输入识别内容")
                            }
                            return@Button
                        }
                        isParsingVoiceTranscript = true
                        scope.launch {
                            runCatching { BillTextParser.parse(transcript) }
                                .onSuccess { parsed ->
                                    draftBill = parsed.toEntity()
                                    showBottomSheet = true
                                    showVoiceConfirmDialog = false
                                    pendingVoiceTranscript = null
                                }
                                .onFailure { throwable ->
                                    snackbarHostState.showSnackbar(
                                        "语音解析失败：${throwable.message ?: "请重试"}"
                                    )
                                }
                            isParsingVoiceTranscript = false
                        }
                    }
                ) {
                    Text(if (isParsingVoiceTranscript) "解析中..." else "确认")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isParsingVoiceTranscript,
                    onClick = {
                        showVoiceConfirmDialog = false
                        pendingVoiceTranscript = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    if (showVoiceSheet) {
        AutoBillVoiceBottomSheet(
            onDismissRequest = {
                showVoiceSheet = false
            },
            onTranscriptReady = { transcript ->
                showVoiceSheet = false
                pendingVoiceTranscript = transcript
                showVoiceConfirmDialog = true
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
        bottomBar = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                recordAudioPermission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            showVoiceSheet = true
                        } else {
                            permissionLauncher.launch(recordAudioPermission)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "语音记账"
                        )
                    }

                    FloatingActionButton(onClick = {
                        draftBill = null
                        showBottomSheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新增账单"
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
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
