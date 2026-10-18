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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import qzwx.app.module_ui.viewmodel.SharedBillViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CustomCalculatorKeyboard(
    amountValue: String,
    wasEvaluated: Boolean,
    onKeyClick: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onActionClick: () -> Unit
) {

    @Composable
    fun StyledCalcButton(
        text: String,
        modifier: Modifier = Modifier,
        containerColor: androidx.compose.ui.graphics.Color,
        contentColor: androidx.compose.ui.graphics.Color,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                fontSize = if (text.length > 1) 18.sp else 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }

    // 数字/运算键专用：display (按钮显示) 和 keyValue (实际传给 onKeyClick) 分离
    // 这样修改键位时只可能填错 display，不可能填错 keyValue (它和解析器紧绑)
    @Composable
    fun CalcKeyButton(
        display: String,
        keyValue: String,
        modifier: Modifier = Modifier,
        isOperator: Boolean = false,
        onKeyClick: (String) -> Unit
    ) {
        StyledCalcButton(
            text = display,
            modifier = modifier,
            containerColor = if (isOperator) MaterialTheme.colorScheme.secondaryContainer
                             else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (isOperator) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurface,
            onClick = { onKeyClick(keyValue) }
        )
    }

    // 动作键专用：AC、⌫、确认/取消/等于/保存 等不带具体 keyValue 的按钮
    @Composable
    fun CalcButton(
        text: String,
        modifier: Modifier = Modifier,
        isOperator: Boolean = false,
        isAction: Boolean = false,
        onClick: () -> Unit
    ) {
        StyledCalcButton(
            text = text,
            modifier = modifier,
            containerColor = when {
                isAction -> MaterialTheme.colorScheme.primary
                isOperator -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = when {
                isAction -> MaterialTheme.colorScheme.onPrimary
                isOperator -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
            onClick = onClick
        )
    }

    val hasOperator = amountValue.drop(1).any { it in listOf('+', '-', '*', '/') }
    val actionText = when {
        amountValue.isEmpty() -> "取消"
        hasOperator -> "等于"
        wasEvaluated -> "保存"
        else -> "确认"
    }

    // 整齐统一的 5 行 × 4 列网格，所有按钮强制等高 (56dp) 等宽
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: 1  2  3  +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKeyButton("1", "1", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("2", "2", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("3", "3", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("+", "+", Modifier.weight(1f), isOperator = true) { onKeyClick(it) }
        }
        // Row 2: 4  5  6  -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKeyButton("4", "4", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("5", "5", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("6", "6", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("-", "-", Modifier.weight(1f), isOperator = true) { onKeyClick(it) }
        }
        // Row 3: 7  8  9  ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKeyButton("7", "7", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("8", "8", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("9", "9", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton("×", "*", Modifier.weight(1f), isOperator = true) { onKeyClick(it) }
        }
        // Row 4: AC  0  .  ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton("AC", Modifier.weight(1f), isOperator = true) { onClear() }
            CalcKeyButton("0", "0", Modifier.weight(1f)) { onKeyClick(it) }
            CalcKeyButton(".", ".", Modifier.weight(1f), isOperator = true) { onKeyClick(it) }
            CalcKeyButton("÷", "/", Modifier.weight(1f), isOperator = true) { onKeyClick(it) }
        }
        // Row 5: 确认/取消/等于/保存 (占 3 格)  ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(actionText, Modifier.weight(3f), isAction = true) { onActionClick() }
            CalcButton("⌫", Modifier.weight(1f), isOperator = true) { onBackspace() }
        }
    }
}

fun evaluateMathExpression(str: String): Double {
    return object : Any() {
        var pos = -1;
        var ch = 0
        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar(); return true
            }
            return false
        }

        fun parse(): Double {
            nextChar();
            val x = parseExpression(); if (pos < str.length) throw RuntimeException(); return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) x /= parseFactor()
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()
            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression(); eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else throw RuntimeException()
            return x
        }
    }.parse()
}

@Composable
fun AddNewBill_AmountInput(
    amountValue: String,
    onAmountChange: (String) -> Unit,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
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
                readOnly = true,
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
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onClick()
                        }
                    },
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

            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (amountValue.isNotEmpty()) {
                    onAmountChange(amountValue.dropLast(1))
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        val existingWallet =
            initialBill?.walletType?.displayName ?: initialBill?.walletId?.let { id ->
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
    var wasEvaluated by remember(initialBill) { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            if (it == SheetValue.Hidden) isClosing else true
        }
    )
    val scope = rememberCoroutineScope()
    var showCustomKeyboard by remember { mutableStateOf(true) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                AddNewBill_AmountInput(
                    amountValue = amountValue,
                    onAmountChange = { newValue ->
                        if (newValue.length <= 15) {
                            amountValue = newValue
                            wasEvaluated = false
                        }
                    },
                    onClick = {
                        showCustomKeyboard = true
                        keyboardController?.hide()
                        focusManager.clearFocus()
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
                        .padding(bottom = 24.dp)
                        .onFocusChanged { if (it.isFocused) showCustomKeyboard = false },
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

                val parsedAmount = try {
                    evaluateMathExpression(amountValue.ifEmpty { "0" })
                } catch (e: Exception) {
                    0.0
                }
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

            androidx.compose.animation.AnimatedVisibility(visible = showCustomKeyboard) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 【颜色代码位置在哪：此处】将整体键盘的背景改为与sheet一致的background色
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = 16.dp, top = 8.dp)
                ) {
                    CustomCalculatorKeyboard(
                        amountValue = amountValue,
                        wasEvaluated = wasEvaluated,
                        onKeyClick = { key ->
                            if (amountValue.length <= 15) {
                                amountValue += key
                                wasEvaluated = false
                            }
                        },
                        onClear = {
                            amountValue = ""
                            wasEvaluated = false
                        },
                        onBackspace = {
                            if (amountValue.isNotEmpty()) {
                                amountValue = amountValue.dropLast(1)
                                wasEvaluated = false
                            }
                        },
                        onActionClick = {
                            val hasOperator =
                                amountValue.drop(1).any { it in listOf('+', '-', '*', '/') }
                            if (amountValue.isEmpty()) {
                                showCustomKeyboard = false
                            } else if (hasOperator) {
                                try {
                                    val result = evaluateMathExpression(amountValue)
                                    amountValue = if (result % 1.0 == 0.0) result.toLong()
                                        .toString() else String.format(
                                        java.util.Locale.US, "%.2f", result
                                    )
                                    wasEvaluated = true
                                } catch (e: Exception) {
                                }
                            } else {
                                showCustomKeyboard = false
                            }
                        }
                    )
                }
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
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
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
