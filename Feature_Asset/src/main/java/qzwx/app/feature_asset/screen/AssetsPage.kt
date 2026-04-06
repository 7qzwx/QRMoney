package qzwx.app.feature_asset.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
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
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

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

    var isExpanded by remember { mutableStateOf(false) }

    // Bill aggregations
    val now = java.time.LocalDateTime.now()
    val startOfMonth =
        now.withDayOfMonth(1).with(java.time.LocalTime.MIN).atZone(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    val startOfWeek = now.with(java.time.DayOfWeek.MONDAY).with(java.time.LocalTime.MIN)
        .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    val totalIncome = allBills.filter { it.type == qzwx.app.module_data.entity.BillType.INCOME }
        .sumOf { it.amount }
    val totalExpense = allBills.filter { it.type == qzwx.app.module_data.entity.BillType.EXPENSE }
        .sumOf { it.amount }

    val monthIncome =
        allBills.filter { it.type == qzwx.app.module_data.entity.BillType.INCOME && it.createdAt >= startOfMonth }
            .sumOf { it.amount }
    val monthExpense =
        allBills.filter { it.type == qzwx.app.module_data.entity.BillType.EXPENSE && it.createdAt >= startOfMonth }
            .sumOf { it.amount }

    val weekIncome =
        allBills.filter { it.type == qzwx.app.module_data.entity.BillType.INCOME && it.createdAt >= startOfWeek }
            .sumOf { it.amount }
    val weekExpense =
        allBills.filter { it.type == qzwx.app.module_data.entity.BillType.EXPENSE && it.createdAt >= startOfWeek }
            .sumOf { it.amount }

    val totalDebtAmount = allDebts.filter { it.type == DebtType.BORROW }.sumOf { -it.remainingAmount }

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
        var totalAmount by remember { mutableStateOf(debtToEdit?.totalAmount?.takeIf { it > 0 }?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
        var remainingAmount by remember { mutableStateOf(debtToEdit?.remainingAmount?.takeIf { it > 0 }?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
        var expanded by remember { mutableStateOf(false) }

        var createdAt by remember { mutableStateOf(debtToEdit?.createdAt ?: System.currentTimeMillis()) }
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
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = deadline ?: System.currentTimeMillis())
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
                        value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(createdAt)),
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
                        value = deadline?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "未设置",
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
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isFlipped = true },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00BFA5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "总资产",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Row {
                                    IconButton(onClick = { isObscured = !isObscured }) {
                                        Icon(
                                            imageVector = if (isObscured) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "隐藏/显示",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = { isFlipped = !isFlipped }) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "翻转",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${displayAmount(totalAssets)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${WalletType.entries.size}个账户",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "本月收支: ${
                                            displayAmount(
                                                monthIncome - monthExpense,
                                                true
                                            )
                                        }", color = Color.White, fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = "Developed by QZWX",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Back side (负债)
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                            .clickable { isFlipped = false },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "总债务",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Row {
                                    IconButton(onClick = {
                                        debtToEdit = null
                                        showDebtDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "新增负债",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = { isObscured = !isObscured }) {
                                        Icon(
                                            imageVector = if (isObscured) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "隐藏/显示",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = { isFlipped = !isFlipped }) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "翻转",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${displayAmount(totalDebtAmount)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${allDebts.size}个负债",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = "Developed by QZWX",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (!isFlipped) {
                Card(modifier = Modifier.fillMaxWidth()) {
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "债务列表",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (allDebts.isEmpty()) {
                            Text(
                                "暂无负债",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            allDebts.forEach { debt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedDebtId = if (expandedDebtId == debt.id) null else debt.id
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = debt.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(if (debt.type == DebtType.BORROW) Color(0xFFFFEBEE) else Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = debt.type.displayName,
                                                    fontSize = 10.sp,
                                                    color = if (debt.type == DebtType.BORROW) Color(0xFFD32F2F) else Color(0xFF388E3C)
                                                )
                                            }
                                            debt.deadline?.let {
                                                val days = TimeUnit.MILLISECONDS.toDays(it - System.currentTimeMillis()).coerceAtLeast(0)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("剩 ${days}天", fontSize = 10.sp, color = Color(0xFFE65100))
                                                }
                                            }
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        val sign = if (debt.type == DebtType.BORROW) "-" else "+"
                                        val amountColor = if (debt.type == DebtType.BORROW) Color(0xFFF44336) else Color(0xFF4CAF50)

                                        if (debt.type == DebtType.BORROW) {
                                            Text(
                                                text = "待还: ${
                                                    if (isObscured) "***" else sign + formatAmount(
                                                        debt.remainingAmount
                                                    )
                                                }",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = amountColor
                                            )
                                        }
                                        Text(
                                            text = "总计: ${
                                                if (isObscured) "***" else sign + formatAmount(
                                                    debt.totalAmount
                                                )
                                            }",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Green
                                        )
                                    }
                                }
                                AnimatedVisibility(visible = expandedDebtId == debt.id) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                                    ) {
                                        Text(
                                            text = "创建时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(debt.createdAt))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        debt.deadline?.let {
                                            val days = TimeUnit.MILLISECONDS.toDays(it - System.currentTimeMillis()).coerceAtLeast(0)
                                            Text(
                                                text = "最后期限: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))} (剩 ${days}天)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    debtToEdit = debt
                                                    showDebtDialog = true
                                                },
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Text("编辑")
                                            }
                                            Button(
                                                onClick = { showDeleteConfirmDialog = debt },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("删除")
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
