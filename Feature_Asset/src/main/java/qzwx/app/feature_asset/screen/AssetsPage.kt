package qzwx.app.feature_asset.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_asset.components.WalletBalanceRow
import qzwx.app.feature_asset.components.WalletTypeHelper.formatAmount
import qzwx.app.feature_asset.nav3.AssetRoute
import qzwx.app.module_data.entity.DebtType
import qzwx.app.module_data.entity.QDebtEntity
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.WalletType
import qzwx.app.module_ui.viewmodel.SharedBillViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import qzwx.app.feature_asset.components.AssetsFrontCard
import qzwx.app.feature_asset.components.AssetsBackCard
import qzwx.app.feature_asset.components.DebtListCard

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsPage(
    backStack: NavBackStack<NavKey>,
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SharedBillViewModel = hiltViewModel(activity)
    val wallets by viewModel.wallets.collectAsState()

    val walletByType = wallets.associateBy { it.type }
    val displayWallets = WalletType.entries.map { type ->
        walletByType[type] ?: QWalletEntity(type = type, balance = 0.0)
    }
    val totalAssets = displayWallets.sumOf { it.balance }

    val allBills by viewModel.allBills.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()

    // Bill aggregations
    val now = java.time.LocalDateTime.now()
    val startOfMonth =
        now.withDayOfMonth(1).with(java.time.LocalTime.MIN).atZone(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()

    val monthIncome =
        allBills.filter { it.type == qzwx.app.module_data.entity.BillType.INCOME && it.createdAt >= startOfMonth }
            .sumOf { it.amount }
    val monthExpense =
        allBills.filter { it.type == qzwx.app.module_data.entity.BillType.EXPENSE && it.createdAt >= startOfMonth }
            .sumOf { it.amount }

    val totalDebtAmount =
        allDebts.filter { it.type == DebtType.BORROW }.sumOf { -it.remainingAmount }

    var isObscured by remember { mutableStateOf(false) }
    var isFlipped by remember { mutableStateOf(false) }

    var debtToEdit by remember { mutableStateOf<QDebtEntity?>(null) }
    var showDebtDialog by remember { mutableStateOf(false) }

    var expandedDebtId by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<QDebtEntity?>(null) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    fun displayAmount(amount: Double, showSign: Boolean = false): String {
        if (isObscured) return "***"
        val formatted = formatAmount(amount)
        return if (showSign && amount > 0) "+$formatted" else formatted
    }

    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("确认删除") },
            text = { Text("您确定要删除此项债务吗？此操作不可逆！") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDebt(showDeleteConfirmDialog!!)
                    showDeleteConfirmDialog = null
                }) { Text("删除", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) { Text("取消") }
            }
        )
    }

    if (showDebtDialog) {
        var debtName by remember { mutableStateOf(debtToEdit?.name ?: "") }
        var debtType by remember { mutableStateOf(debtToEdit?.type ?: DebtType.BORROW) }
        var totalAmount by remember {
            mutableStateOf(debtToEdit?.totalAmount?.takeIf { it > 0 }
                ?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "")
        }
        var remainingAmount by remember {
            mutableStateOf(debtToEdit?.remainingAmount?.takeIf { it > 0 }
                ?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "")
        }
        var expanded by remember { mutableStateOf(false) }

        var createdAt by remember {
            mutableStateOf(
                debtToEdit?.createdAt ?: System.currentTimeMillis()
            )
        }
        var deadline by remember { mutableStateOf<Long?>(debtToEdit?.deadline) }
        var showCreatedDatePicker by remember { mutableStateOf(false) }
        var showDeadlinePicker by remember { mutableStateOf(false) }

        val decimalRegex = "^\\d*\\.?\\d{0,2}$".toRegex()
        val isSaveEnabled = debtName.isNotBlank() && (totalAmount.toDoubleOrNull() ?: 0.0) > 0.0

        if (showCreatedDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = createdAt)
            DatePickerDialog(
                onDismissRequest = { showCreatedDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { createdAt = it }
                        showCreatedDatePicker = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatedDatePicker = false }) { Text("取消") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showDeadlinePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = deadline ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDeadlinePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { deadline = it }
                        showDeadlinePicker = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeadlinePicker = false }) { Text("取消") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        AlertDialog(
            onDismissRequest = { showDebtDialog = false },
            title = { Text(if (debtToEdit == null) "新增债务" else "编辑债务") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = debtName,
                            onValueChange = { debtName = it },
                            label = { Text("债务名称") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            FilledTonalButton(
                                onClick = { expanded = true },
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(debtType.displayName)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                DebtType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.displayName) },
                                        onClick = { debtType = type; expanded = false }
                                    )

                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(decimalRegex)) {
                                totalAmount = it
                                remainingAmount = it
                            }
                        },
                        label = { Text("总金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = remainingAmount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(decimalRegex)) {
                                remainingAmount = it
                            }
                        },
                        label = { Text("剩余待还金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                            Date(
                                createdAt
                            )
                        ),
                        onValueChange = {},
                        label = { Text("创建时间") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showCreatedDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deadline?.let {
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).format(Date(it))
                        } ?: "未设置",
                        onValueChange = {},
                        label = {
                            val daysRemaining = deadline?.let {
                                val diff = it - System.currentTimeMillis()
                                TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
                            }
                            Text(if (daysRemaining != null) "最后期限 (剩余 ${daysRemaining}天)" else "最后期限")
                        },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDeadlinePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val tAmount = totalAmount.toDoubleOrNull() ?: 0.0
                        val rAmount = remainingAmount.toDoubleOrNull() ?: 0.0
                        if (debtName.isNotBlank() && tAmount > 0) {
                            val newDebt = QDebtEntity(
                                id = debtToEdit?.id ?: 0L,
                                name = debtName,
                                type = debtType,
                                totalAmount = tAmount,
                                remainingAmount = rAmount,
                                createdAt = createdAt,
                                deadline = deadline
                            )
                            if (debtToEdit == null) {
                                viewModel.insertDebt(newDebt)
                            } else {
                                viewModel.updateDebt(newDebt)
                            }
                        }
                        showDebtDialog = false
                    },
                    enabled = isSaveEnabled
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDebtDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
            ) {
                if (rotation <= 90f) {
                    // Front side (资产)
                    AssetsFrontCard(
                        totalAssets = totalAssets,
                        walletCount = WalletType.entries.size,
                        monthIncome = monthIncome,
                        monthExpense = monthExpense,
                        isObscured = isObscured,
                        onToggleObscured = { isObscured = !isObscured },
                        onToggleFlip = { isFlipped = true },
                        modifier = Modifier
                    )
                } else {
                    // Back side (负债)
                    AssetsBackCard(
                        totalDebtAmount = totalDebtAmount,
                        debtCount = allDebts.size,
                        isObscured = isObscured,
                        onToggleObscured = { isObscured = !isObscured },
                        onToggleFlip = { isFlipped = false },
                        onAddDebt = {
                            debtToEdit = null
                            showDebtDialog = true
                        },
                        modifier = Modifier
                    )
                }
            }

            if (!isFlipped) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "我的钱包",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        displayWallets.forEach { wallet ->
                            WalletBalanceRow(
                                wallet = wallet,
                                isObscured = isObscured,
                                onClick = {
                                    backStack.add(
                                        AssetRoute.WalletDetail(
                                            walletTypeName = wallet.type.name,
                                            initialBalance = wallet.balance
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                DebtListCard(
                    allDebts = allDebts,
                    expandedDebtId = expandedDebtId,
                    onDebtExpandedChange = { expandedDebtId = it },
                    isObscured = isObscured,
                    onEditDebt = { debt ->
                        debtToEdit = debt
                        showDebtDialog = true
                    },
                    onDeleteDebtClick = { debt ->
                        showDeleteConfirmDialog = debt
                    }
                )
            }
        }
    }
}
