package qzwx.app.feature_asset.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
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

    var editedText by remember(walletTypeName) { mutableStateOf(String.format(Locale.US, "%.2f", initialBalance)) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = walletType.displayName) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Image(
                        painter = painterResource(id = walletType.toDrawableRes()),
                        contentDescription = walletType.displayName,
                        modifier = Modifier.height(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("当前余额", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatAmount(wallet.balance),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isDefault,
                            onCheckedChange = {
                                isDefault = it
                                isEditedByUser = true
                            }
                        )
                        Text("设为默认支付方式 (✅)")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.updateWalletBalance(walletType, parsedBalance ?: 0.0, isDefault) {
                                // 模拟设为默认支出的接口操作等
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
