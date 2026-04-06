package qzwx.app.feature_asset.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import qzwx.app.feature_asset.components.WalletTypeHelper.formatAmount
import qzwx.app.module_data.entity.DebtType
import qzwx.app.module_data.entity.QDebtEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AssetsFrontCard(
    totalAssets: Double,
    walletCount: Int,
    monthIncome: Double,
    monthExpense: Double,
    isObscured: Boolean,
    onToggleObscured: () -> Unit,
    onToggleFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun displayAmount(amount: Double, showSign: Boolean = false): String {
        if (isObscured) return "***"
        val formatted = formatAmount(amount)
        return if (showSign && amount > 0) "+$formatted" else formatted
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onToggleFlip() },
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
                    IconButton(onClick = onToggleObscured) {
                        Icon(
                            imageVector = if (isObscured) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "隐藏/显示",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onToggleFlip) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "翻转",
                            tint = Color.White
                        )
                    }
                }
            }
            Text(
                text = displayAmount(totalAssets),
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
                        "${walletCount}个账户",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "本月收支: ${
                            displayAmount(monthIncome - monthExpense, true)
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
}

@Composable
fun AssetsBackCard(
    totalDebtAmount: Double,
    debtCount: Int,
    isObscured: Boolean,
    onToggleObscured: () -> Unit,
    onToggleFlip: () -> Unit,
    onAddDebt: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun displayAmount(amount: Double, showSign: Boolean = false): String {
        if (isObscured) return "***"
        val formatted = formatAmount(amount)
        return if (showSign && amount > 0) "+$formatted" else formatted
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
            .clickable { onToggleFlip() },
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
                    IconButton(onClick = onAddDebt) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新增负债",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onToggleObscured) {
                        Icon(
                            imageVector = if (isObscured) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "隐藏/显示",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onToggleFlip) {
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
                text = displayAmount(totalDebtAmount),
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
                        "${debtCount}个负债",
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

@Composable
fun DebtListCard(
    allDebts: List<QDebtEntity>,
    expandedDebtId: Long?,
    onDebtExpandedChange: (Long?) -> Unit,
    isObscured: Boolean,
    onEditDebt: (QDebtEntity) -> Unit,
    onDeleteDebtClick: (QDebtEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var debtFilter by remember { mutableStateOf<DebtType?>(null) }
    val filteredDebts = if (debtFilter == null) allDebts else allDebts.filter { it.type == debtFilter }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "债务列表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                ) {
                    val types = listOf(null) + DebtType.entries
                    types.forEachIndexed { index, type ->
                        val isSelected = debtFilter == type
                        val bgColor by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            label = "ToggleBg$index"
                        )
                        val textColor by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            label = "ToggleText$index"
                        )
                        Box(
                            modifier = Modifier
                                .background(bgColor)
                                .clickable { debtFilter = type }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type?.displayName ?: "全部",
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (filteredDebts.isEmpty()) {
                Text(
                    "暂无负债",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredDebts) { debt ->
                        DebtListItem(
                            debt = debt,
                            isExpanded = expandedDebtId == debt.id,
                            onExpandChange = { onDebtExpandedChange(if (expandedDebtId == debt.id) null else debt.id) },
                            isObscured = isObscured,
                            onEditDebt = { onEditDebt(debt) },
                            onDeleteDebtClick = { onDeleteDebtClick(debt) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtListItem(
    debt: QDebtEntity,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    isObscured: Boolean,
    onEditDebt: () -> Unit,
    onDeleteDebtClick: () -> Unit
) {
    val isBorrow = debt.type == DebtType.BORROW
    val themeColor = if (isBorrow) Color(0xFFF44336) else Color(0xFF4CAF50)
    val lightThemeColor = if (isBorrow) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline Date side
        Box(
            modifier = Modifier
                .width(48.dp)
                .padding(top = 13.dp, end = 8.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(debt.createdAt)),
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
                fontWeight = FontWeight.SemiBold
            )
        }

        // Timeline Line and Dot
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFEEEEEE))
            )
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    .border(2.dp, themeColor, RoundedCornerShape(50))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Card side
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .clickable { onExpandChange() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                // Left Border
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(themeColor)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = debt.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(lightThemeColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = debt.type.displayName,
                                    fontSize = 10.sp,
                                    color = themeColor
                                )
                            }
                            debt.deadline?.let {
                                val days = TimeUnit.MILLISECONDS.toDays(it - System.currentTimeMillis()).coerceAtLeast(0)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "剩 ${days}天",
                                        fontSize = 10.sp,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "创建时间: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(debt.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = if (isBorrow) "待还金额" else "待收金额",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            val sign = if (isBorrow) "-" else "+"
                            Text(
                                text = if (isObscured) "***" else sign + formatAmount(debt.remainingAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "总金额",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = if (isObscured) "***" else formatAmount(debt.totalAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = onEditDebt,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("编辑")
                                }
                                Button(
                                    onClick = onDeleteDebtClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("删除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
