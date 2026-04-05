package qzwx.app.feature_asset.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_asset.components.WalletBalanceRow
import qzwx.app.feature_asset.components.WalletTypeHelper.formatAmount
import qzwx.app.feature_asset.nav3.AssetRoute
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.WalletType
import qzwx.app.module_ui.viewmodel.SharedBillViewModel

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

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { isExpanded = !isExpanded }),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("总资产", style = MaterialTheme.typography.labelLarge)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatAmount(totalAssets),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "共 ${WalletType.entries.size} 个支付方式",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                Text(
                                    formatAmount(monthIncome),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Column {
                                Text("本月支出", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    formatAmount(monthExpense),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Red
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("本周收入", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    formatAmount(weekIncome),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Column {
                                Text("本周支出", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    formatAmount(weekExpense),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Red
                                )
                            }
                        }
                    } else {
                        // 预留总收支，也可以走真实逻辑
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("总收入", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    formatAmount(totalIncome),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Column {
                                Text("总支出", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    formatAmount(totalExpense),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }

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
        }
    }
}

