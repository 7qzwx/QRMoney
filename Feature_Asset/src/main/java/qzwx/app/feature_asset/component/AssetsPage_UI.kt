package qzwx.app.feature_asset.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import qzwx.app.feature_asset.R

@Composable
fun AssetSummaryCard(
    isVisible: Boolean,
    isFlipped: Boolean,
    onVisibilityToggle: () -> Unit,
    onFlippedToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "flipAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFlippedToggle() }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rotation <= 90f) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
            contentColor = if (rotation <= 90f) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        if (rotation <= 90f) {
            AssetSummaryCardFront(isVisible, onVisibilityToggle)
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                AssetSummaryCardBack(isVisible, onVisibilityToggle)
            }
        }
    }
}

@Composable
fun AssetSummaryCardFront(isVisible: Boolean, onVisibilityToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "总资产", fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onVisibilityToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Hide/Show Assets",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isVisible) "¥ -3.06" else "******",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Accounts",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "5个付款方式", fontSize = 12.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Income/Expense",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "本月收支: +¥600.00", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Developed by QZWX",
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun AssetSummaryCardBack(isVisible: Boolean, onVisibilityToggle: () -> Unit) {
    var showAddDebtDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "总负债", fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onVisibilityToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Hide/Show Debts",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { showAddDebtDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Debt",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isVisible) "¥ -1000.00" else "******",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Debts Count",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "2笔负债", fontSize = 12.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = "Repayment",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "本月待还: +¥200.00", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Developed by QZWX",
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(onDismiss = { showAddDebtDialog = false })
    }
}

@Composable
fun AddDebtDialog(onDismiss: () -> Unit) {
    var debtName by remember { mutableStateOf("") }
    var debtAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增负债") },
        text = {
            Column {
                OutlinedTextField(
                    value = debtName,
                    onValueChange = { debtName = it },
                    label = { Text("负债名称") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = debtAmount,
                    onValueChange = { debtAmount = it },
                    label = { Text("金额") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun WalletOverviewCard(isVisible: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "我的钱包",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OverviewItem(
                name = "现金",
                balance = if (isVisible) "¥ 100.00" else "******",
                iconRes = R.drawable.paycash
            )
            OverviewItem(
                name = "支付宝",
                balance = if (isVisible) "¥ 500.00" else "******",
                iconRes = R.drawable.payzfb
            )
            OverviewItem(
                name = "微信",
                balance = if (isVisible) "¥ 200.00" else "******",
                iconRes = R.drawable.paywx
            )
            OverviewItem(
                name = "银行卡",
                balance = if (isVisible) "¥ 1000.00" else "******",
                iconRes = R.drawable.paycard
            )
            OverviewItem(
                name = "其它",
                balance = if (isVisible) "¥ 0.00" else "******",
                iconRes = R.drawable.payother
            )
        }
    }
}

@Composable
fun DebtOverviewCard(isVisible: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "我的负债",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
            OverviewItem(name = "房贷", balance = if (isVisible) "¥ 800.00" else "******")
            OverviewItem(name = "车贷", balance = if (isVisible) "¥ 200.00" else "******")
        }
    }
}

@Composable
fun OverviewItem(name: String, balance: String, iconRes: Int? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = name,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(text = name, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = balance,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
