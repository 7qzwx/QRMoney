package qzwx.app.feature_asset.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import qzwx.app.feature_asset.R
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.WalletType
import qzwx.app.module_ui.viewmodel.SharedBillViewModel
import java.util.Locale

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailPage(
    walletTypeName: String,
    initialBalance: Double = 0.0,
    onBackClick: () -> Unit,
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SharedBillViewModel = hiltViewModel(activity)

    val walletType = parseWalletType(walletTypeName)
    val wallets by viewModel.wallets.collectAsState()
    val dbWallet = wallets.firstOrNull { it.type == walletType }
    val wallet = dbWallet ?: QWalletEntity(type = walletType, balance = initialBalance)

    var editedText by remember(walletTypeName) {
        mutableStateOf(
            String.format(
                Locale.US,
                "%.2f",
                initialBalance
            )
        )
    }
    var isEditedByUser by remember(walletTypeName) { mutableStateOf(false) }
    var isDefault by remember(walletTypeName) { mutableStateOf(wallet.isDefault) }

    LaunchedEffect(walletTypeName) {
        viewModel.ensureWalletsInitialized()
    }

    LaunchedEffect(dbWallet?.id, dbWallet?.updatedAt, dbWallet?.isDefault, isEditedByUser) {
        if (dbWallet != null) {
            if (!isEditedByUser) {
                editedText = String.format(Locale.US, "%.2f", dbWallet.balance)
            }
            if (!isEditedByUser) {
                isDefault = dbWallet.isDefault
            }
        }
    }

    val parsedBalance = editedText.toDoubleOrNull()
    val isValidInput = editedText.isNotBlank() && parsedBalance != null && parsedBalance >= 0.0

    var isExpanded by remember { mutableStateOf(false) }

    val walletBills by viewModel.getBillsByWallet(walletType).collectAsState(initial = emptyList())

    val billsByDate = remember(walletBills) {
        walletBills.groupBy { bill ->
            java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(bill.createdAt),
                java.time.ZoneId.systemDefault()
            ).toLocalDate()
        }.toSortedMap(compareByDescending { it })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = walletType.toDrawableRes()),
                            contentDescription = walletType.displayName,
                            modifier = Modifier.height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = walletType.displayName,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 搜索预留 */ }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { isExpanded = !isExpanded })
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("当前余额", style = MaterialTheme.typography.labelLarge)
                            IconButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "展开"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = formatAmount(wallet.balance),
                            style = MaterialTheme.typography.headlineMedium
                        )

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("本月收入", style = MaterialTheme.typography.labelMedium)
                                    Text("¥ 0.00", style = MaterialTheme.typography.bodyLarge)
                                }
                                Column {
                                    Text("本月支出", style = MaterialTheme.typography.labelMedium)
                                    Text("¥ 0.00", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("本周收入", style = MaterialTheme.typography.labelMedium)
                                    Text("¥ 0.00", style = MaterialTheme.typography.bodyLarge)
                                }
                                Column {
                                    Text("本周支出", style = MaterialTheme.typography.labelMedium)
                                    Text("¥ 0.00", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("总收入", style = MaterialTheme.typography.labelMedium)
                                    Text("¥ 0.00", style = MaterialTheme.typography.bodyLarge)
                                }
                                Column {
                                    Text("总支出", style = MaterialTheme.typography.labelMedium)
                                    Text("¥ 0.00", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("修改余额", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = editedText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                        editedText = input
                                        isEditedByUser = true
                                    }
                                },
                                label = { Text("输入余额") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                supportingText = {
                                    if (wallets.isEmpty()) {
                                        Text("钱包数据加载中...")
                                    } else if (!isValidInput && editedText.isNotBlank()) {
                                        Text("请输入数字，且小数最多 2 位", color = Color.Red)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("设为默认支付方式")
                            Switch(
                                checked = isDefault,
                                onCheckedChange = {
                                    isDefault = it
                                    isEditedByUser = true
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50), // 绿色
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray // 灰色
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.updateWalletBalance(
                                    walletType,
                                    parsedBalance ?: 0.0,
                                    isDefault
                                ) {
                                    onBackClick()
                                }
                            },
                            enabled = isValidInput,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("保存")
                        }
                    }
                }
            }

            if (walletBills.isNotEmpty()) {
                item {
                    Text(
                        "该支付方式账单",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

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

                        val expenseTotal = dailyBills.filter { it.type == qzwx.app.module_data.entity.BillType.EXPENSE }.sumOf { it.amount }
                        val incomeTotal = dailyBills.filter { it.type == qzwx.app.module_data.entity.BillType.INCOME }.sumOf { it.amount }

                        qzwx.app.module_ui.components.Shared_BillDayGroup(
                            dateStr = dateStr,
                            dayOfWeekStr = dayOfWeekStr,
                            expenseTotal = expenseTotal,
                            incomeTotal = incomeTotal,
                            bills = dailyBills,
                            onEditBill = { /* Readonly fallback mode */ },
                            onDeleteBill = { /* Disable in wallet detail to simplify flow, or link */ },
                            onItemClick = { /* Can implement intent */ }
                        )
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.CHINA, "¥ %,.2f", amount)
}

private fun parseWalletType(walletTypeName: String): WalletType {
    return runCatching { WalletType.valueOf(walletTypeName) }
        .getOrElse { WalletType.ALIPAY }
}

private fun WalletType.toDrawableRes(): Int {
    return when (this) {
        WalletType.CASH -> R.drawable.paycash
        WalletType.ALIPAY -> R.drawable.payzfb
        WalletType.WECHAT -> R.drawable.paywx
        WalletType.BANK_CARD -> R.drawable.paycard
        WalletType.OTHER -> R.drawable.payother
    }
}
