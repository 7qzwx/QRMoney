package qzwx.app.module_ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import kotlinx.coroutines.launch
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_data.entity.WalletType
import qzwx.app.module_ui.data.billCategoryList
import qzwx.app.module_ui.data.incomeCategoryList
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import qzwx.app.module_ui.viewmodel.SharedBillViewModel

@Composable
fun AddNewBill_AmountInput(
    amountValue: String,
    onAmountChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "TOTAL AMOUNT",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "¥",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline()
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = amountValue,
                onValueChange = onAmountChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .alignByBaseline()
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (amountValue.isEmpty()) {
                            Text(
                                text = "0",
                                style = TextStyle(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewBill_TypeToggle(
    isExpense: Boolean,
    onExpenseChange: (Boolean) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = isExpense,
            onClick = { onExpenseChange(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) {
            Text(
                "Expense",
                fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal
            )
        }
        SegmentedButton(
            selected = !isExpense,
            onClick = { onExpenseChange(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) {
            Text(
                "Income",
                fontWeight = if (!isExpense) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun AddNewBill_CategoryGrid(
    isExpense: Boolean,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val currentList = if (isExpense) billCategoryList else incomeCategoryList
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.heightIn(max = 250.dp)
    ) {
        items(currentList) { category ->
            val isSelected = selectedCategory == category.name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onCategorySelected(category.name) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .padding(2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddNewBill_DateTimeRow(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    selectedWalletName: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onWalletClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .clickable { onDateClick() }
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 4.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("M/d")),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .clickable { onTimeClick() }
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 4.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .clickable { onWalletClick() }
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 4.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedWalletName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Shared_AddNewBill(
    initialBill: QBillEntity? = null,
    defaultDate: LocalDate = LocalDate.now(),
    onDismissRequest: () -> Unit = {},
    onSaveRequest: (QBillEntity) -> Unit = {},
    viewModel: SharedBillViewModel = hiltViewModel()
) {
    val defaultWalletName by viewModel.defaultWalletName.collectAsState()
    val allWallets by viewModel.wallets.collectAsState()

    var selectedWallet by remember(initialBill) {
        // Prefer walletType snapshot when editing old bills, fallback to linked wallet id.
        val existingWallet = initialBill?.walletType?.displayName ?: initialBill?.walletId?.let { id ->
            allWallets.find { it.id == id }?.type?.displayName
        }
        mutableStateOf<String?>(existingWallet)
    }

    val effectiveWalletName = selectedWallet ?: defaultWalletName

    Shared_AddNewBill_Content(
        initialBill = initialBill,
        defaultDate = defaultDate,
        selectedWallet = effectiveWalletName,
        onWalletChanged = { selectedWallet = it },
        onDismissRequest = onDismissRequest,
        onSaveRequest = { bill ->
            val matchingWallet = allWallets.find { it.type.displayName == effectiveWalletName }
            val fallbackWallet = allWallets.find { it.isDefault } ?: allWallets.firstOrNull()
            val selectedType = WalletType.fromDisplayName(effectiveWalletName)
            val billWithWallet = bill.copy(
                walletId = matchingWallet?.id ?: fallbackWallet?.id,
                walletType = selectedType
            )
            onSaveRequest(billWithWallet)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Shared_AddNewBill_Content(
    initialBill: QBillEntity? = null,
    defaultDate: LocalDate = LocalDate.now(),
    selectedWallet: String = "现金",
    onWalletChanged: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onSaveRequest: (QBillEntity) -> Unit = {}
) {
    val calendarState = rememberUseCaseState()
    val clockState = rememberUseCaseState()
    var selectedDate by remember(initialBill, defaultDate) {
        mutableStateOf(initialBill?.let {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it.createdAt),
                ZoneId.systemDefault()
            ).toLocalDate()
        } ?: defaultDate)
    }
    var selectedTime by remember(initialBill) {
        mutableStateOf(initialBill?.let {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it.createdAt),
                ZoneId.systemDefault()
            ).toLocalTime()
        } ?: LocalTime.now())
    }

    var amountValue by remember(initialBill) {
        mutableStateOf(initialBill?.let { if (it.amount > 0) it.amount.toString() else "" } ?: "")
    }
    var isExpense by remember(initialBill) { mutableStateOf(initialBill?.type != BillType.INCOME) }
    var selectedCategory by remember(initialBill, isExpense) {
        mutableStateOf(
            initialBill?.category ?: if (isExpense) {
                billCategoryList.firstOrNull()?.name ?: ""
            } else {
                incomeCategoryList.firstOrNull()?.name ?: ""
            }
        )
    }
    var remarkValue by remember(initialBill) { mutableStateOf(initialBill?.remark ?: "") }

    var showWalletDialog by remember { mutableStateOf(false) }

    var isClosing by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            if (it == SheetValue.Hidden) isClosing else true
        }
    )
    val scope = rememberCoroutineScope()

    BackHandler {
        isClosing = true
        scope.launch {
            sheetState.hide()
            onDismissRequest()
        }
    }

    CalendarDialog(
        state = calendarState,
        selection = CalendarSelection.Date(
            selectedDate = selectedDate
        ) { newDate ->
            selectedDate = newDate
        },
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true,
            style = CalendarStyle.MONTH
        )
    )

    ClockDialog(
        state = clockState,
        selection = ClockSelection.HoursMinutes { hours, minutes ->
            selectedTime = LocalTime.of(hours, minutes)
        },
        config = ClockConfig(
            defaultTime = selectedTime,
            is24HourFormat = true
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AddNewBill_AmountInput(
                amountValue = amountValue,
                onAmountChange = { newValue ->
                    if (newValue.length <= 12 && newValue.count { it == '.' } <= 1) {
                        amountValue = newValue
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            AddNewBill_TypeToggle(
                isExpense = isExpense,
                onExpenseChange = { newIsExpense ->
                    isExpense = newIsExpense
                    selectedCategory = if (newIsExpense) {
                        billCategoryList.firstOrNull()?.name ?: ""
                    } else {
                        incomeCategoryList.firstOrNull()?.name ?: ""
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Clear all button
                IconButton(
                    onClick = {
                        amountValue = ""
                        isExpense = true
                        selectedCategory = billCategoryList.firstOrNull()?.name ?: ""
                        remarkValue = ""
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Clear All",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AddNewBill_CategoryGrid(
                isExpense = isExpense,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = remarkValue,
                onValueChange = { remarkValue = it },
                placeholder = { Text("备注（可选）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            AddNewBill_DateTimeRow(
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                selectedWalletName = selectedWallet,
                onDateClick = { calendarState.show() },
                onTimeClick = { clockState.show() },
                onWalletClick = { showWalletDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val parsedAmount = amountValue.toDoubleOrNull() ?: 0.00
            Button(
                onClick = {
                    val createdAtMillis = LocalDateTime.of(selectedDate, selectedTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                    onSaveRequest(
                        QBillEntity(
                            id = initialBill?.id ?: 0L,
                            amount = parsedAmount,
                            type = if (isExpense) BillType.EXPENSE else BillType.INCOME, // 存储类型（支?收入?
                            category = selectedCategory, // 存储选中的种?
                            remark = remarkValue.takeIf { it.isNotBlank() },
                            createdAt = createdAtMillis,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    isClosing = true
                    scope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                enabled = parsedAmount > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (showWalletDialog) {
        val wallets = WalletType.entries.map { it.displayName }
        AlertDialog(
            onDismissRequest = { showWalletDialog = false },
            title = { Text("选择支付类型", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    wallets.forEach { wallet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onWalletChanged(wallet)
                                    showWalletDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = wallet, style = MaterialTheme.typography.bodyLarge)
                            if (wallet == selectedWallet) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWalletDialog = false }) { Text("关闭") }
            }
        )
    }
}

@Composable
fun Shared_SnakBar(
    snackbarHostState: SnackbarHostState,
) {
    SnackbarHost(hostState = snackbarHostState) { data ->
        Snackbar(
            modifier = Modifier.padding(12.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = data.visuals.message,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Action Button
                if (data.visuals.actionLabel != null) {
                    val isUndo = data.visuals.actionLabel == "撤销"
                    IconButton(
                        onClick = { data.performAction() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isUndo) Icons.AutoMirrored.Filled.Undo else Icons.Default.Edit,
                            contentDescription = data.visuals.actionLabel,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { data.dismiss() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "确认",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
