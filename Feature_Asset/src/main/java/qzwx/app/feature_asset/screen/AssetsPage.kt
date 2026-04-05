package qzwx.app.feature_asset.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_asset.R
import qzwx.app.feature_asset.nav3.AssetRoute
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.WalletType
import qzwx.app.module_ui.viewmodel.SharedBillViewModel
import java.util.Locale

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

    var isExpanded by remember { mutableStateOf(false) }

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
                        // 预留总收支，也可以走真实逻辑
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

@Composable
private fun WalletBalanceRow(
    wallet: QWalletEntity,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = wallet.type.toDrawableRes()),
            contentDescription = wallet.type.displayName,
            modifier = Modifier.height(22.dp)
        )
        Text(
            text = wallet.type.displayName,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        if (wallet.isDefault) {
            Text(
                text = "✅",
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Text(
            text = formatAmount(wallet.balance),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.CHINA, "¥ %,.2f", amount)
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
