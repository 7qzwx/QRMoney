package qzwx.app.module_ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Pie
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_ui.data.billCategoryList
import java.util.Locale

@Composable
fun Shared_CategoryPieChart(
    bills: List<QBillEntity>,
    modifier: Modifier = Modifier
) {
    var isPieChartExpense by remember { mutableStateOf(true) }
    var selectedPieCategory by remember { mutableStateOf<String?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = modifier) {
        // Pie Chart Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "分类占比", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            // Toggle for Pie Chart
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, primaryColor, RoundedCornerShape(8.dp))
            ) {
                val pieExpenseBgColor by animateColorAsState(
                    if (isPieChartExpense) primaryColor else surfaceColor,
                    label = "PieExpenseBg"
                )
                val pieExpenseTextColor by animateColorAsState(
                    if (isPieChartExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                    label = "PieExpenseText"
                )
                val pieIncomeBgColor by animateColorAsState(
                    if (!isPieChartExpense) primaryColor else surfaceColor,
                    label = "PieIncomeBg"
                )
                val pieIncomeTextColor by animateColorAsState(
                    if (!isPieChartExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                    label = "PieIncomeText"
                )

                Box(
                    modifier = Modifier
                        .background(pieExpenseBgColor)
                        .clickable { isPieChartExpense = true }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "支出",
                        color = pieExpenseTextColor,
                        fontWeight = if (isPieChartExpense) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(pieIncomeBgColor)
                        .clickable { isPieChartExpense = false }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "收入",
                        color = pieIncomeTextColor,
                        fontWeight = if (!isPieChartExpense) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = isPieChartExpense,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "PieChartContentAnim"
        ) { targetIsPieChartExpense ->

            // Filters base on pure list passed in
            val pieBills = bills.filter { bill ->
                bill.type == if (targetIsPieChartExpense) BillType.EXPENSE else BillType.INCOME
            }
            val totalPieAmount = pieBills.sumOf { it.amount }
            val categoryGroups = pieBills.groupBy { it.category }
                .mapValues { it.value.sumOf { b -> b.amount } }
                .toList()
                .sortedByDescending { it.second }

            // Reset selected category if there's no matching group
            LaunchedEffect(categoryGroups) {
                if (selectedPieCategory != null && categoryGroups.find { it.first == selectedPieCategory } == null) {
                    selectedPieCategory = null
                }
            }

            val colors = listOf(
                Color(0xFFF44336), Color(0xFF2196F3), Color(0xFFFF9800),
                Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFF00BCD4),
                Color(0xFFFFEB3B), Color(0xFF795548)
            )

            if (categoryGroups.isNotEmpty() && totalPieAmount > 0) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(180.dp)
                        ) {
                            PieChart(
                                onPieClick = { pie ->
                                    selectedPieCategory =
                                        if (selectedPieCategory == pie.label) null else pie.label
                                },
                                modifier = Modifier.fillMaxSize(),
                                selectedPaddingDegree = 4f,
                                labelHelperProperties = LabelHelperProperties(enabled = false),
                                selectedScale = 1.2f,
                                data = categoryGroups.mapIndexed { index, pair ->
                                    Pie(
                                        label = pair.first,
                                        data = pair.second,
                                        color = colors[index % colors.size],
                                        selectedColor = colors[index % colors.size].copy(alpha = 0.5f),
                                        selected = pair.first == selectedPieCategory
                                    )
                                },
                                style = Pie.Style.Fill
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            categoryGroups.forEachIndexed { index, pair ->
                                val percent = (pair.second / totalPieAmount) * 100
                                val isPieSelected = pair.first == selectedPieCategory
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPieSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable {
                                            selectedPieCategory =
                                                if (isPieSelected) null else pair.first
                                        }
                                        .padding(all = if (isPieSelected) 4.dp else 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                colors[index % colors.size],
                                                RoundedCornerShape(50)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${pair.first} (${
                                            String.format(
                                                Locale.getDefault(),
                                                "%.1f",
                                                percent
                                            )
                                        }%, ¥${
                                            String.format(
                                                Locale.getDefault(),
                                                "%.1f",
                                                pair.second
                                            )
                                        })",
                                        fontSize = 12.sp,
                                        fontWeight = if (isPieSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        categoryGroups.forEachIndexed { index, pair ->
                            val percent = (pair.second / totalPieAmount) * 100
                            val cat = billCategoryList.find { it.name == pair.first }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (cat != null) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.name,
                                        tint = colors[index % colors.size],
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                colors[index % colors.size],
                                                RoundedCornerShape(50)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = pair.first,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "¥${
                                                String.format(
                                                    Locale.getDefault(),
                                                    "%.2f",
                                                    pair.second
                                                )
                                            }", fontSize = 14.sp, fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (percent / 100).toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = colors[index % colors.size],
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${
                                            String.format(
                                                Locale.getDefault(),
                                                "%.1f",
                                                percent
                                            )
                                        }%", fontSize = 12.sp, color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "暂无数据", color = Color.Gray)
                }
            }
        }
    }
}

