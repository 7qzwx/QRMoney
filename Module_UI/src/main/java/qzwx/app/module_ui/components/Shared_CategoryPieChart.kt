package qzwx.app.module_ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwipeDown
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import qzwx.app.module_ui.data.incomeCategoryList
import java.util.Locale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Shared_CategoryPieChart(
    bills: List<QBillEntity>,
    modifier: Modifier = Modifier,
    onCategoryClick: ((String, List<QBillEntity>) -> Unit)? = null
) {
    var isPiePercentageMode by remember { mutableStateOf(true) }
    var isPieExpanded by remember { mutableStateOf(false) }
    var isPieExpense by remember { mutableStateOf(true) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = {
                            isPiePercentageMode = !isPiePercentageMode
                        })
                    ) {
                        Text(
                            text = "分类占比",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = if (isPiePercentageMode) Icons.Default.SwipeUp else Icons.Default.SwipeDown,
                            contentDescription = "Swap Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(0.dp)
                        )
                    }
                    IconButton(
                        onClick = { isPieExpanded = !isPieExpanded },
                    ) {
                        Icon(
                            imageVector = if (isPieExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(0.dp)
                        )
                    }
                }

                // Togge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, primaryColor, RoundedCornerShape(8.dp))
                ) {
                    val expenseBgColor by animateColorAsState(
                        if (isPieExpense) primaryColor else MaterialTheme.colorScheme.surface,
                        label = "ExpenseBg"
                    )
                    val expenseTextColor by animateColorAsState(
                        if (isPieExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                        label = "ExpenseText"
                    )
                    val incomeBgColor by animateColorAsState(
                        if (!isPieExpense) primaryColor else MaterialTheme.colorScheme.surface,
                        label = "IncomeBg"
                    )
                    val incomeTextColor by animateColorAsState(
                        if (!isPieExpense) MaterialTheme.colorScheme.onPrimary else primaryColor,
                        label = "IncomeText"
                    )

                    Box(
                        modifier = Modifier
                            .background(expenseBgColor)
                            .clickable { isPieExpense = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "支出",
                            color = expenseTextColor,
                            fontWeight = if (isPieExpense) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(incomeBgColor)
                            .clickable { isPieExpense = false }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "收入",
                            color = incomeTextColor,
                            fontWeight = if (!isPieExpense) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val typeToFilter = if (isPieExpense) BillType.EXPENSE else BillType.INCOME

            val pieBills = bills.filter { bill ->
                bill.type == typeToFilter
            }
            val totalPieAmount = pieBills.sumOf { it.amount }
            val categoryGroups = pieBills.groupBy { it.category }
                .mapValues { it.value.sumOf { b -> b.amount } }
                .toList()
                .sortedByDescending { it.second }

            val colors = listOf(
                Color(0xFFF44336), Color(0xFF2196F3), Color(0xFFFF9800),
                Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFF00BCD4),
                Color(0xFFFFEB3B), Color(0xFF795548)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                if (categoryGroups.isNotEmpty() && totalPieAmount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val amountText1 =
                            String.format(Locale.getDefault(), "%.2f", totalPieAmount)
                        val amountText2 = if (isPieExpense) "总支出" else "总收入"

                        CustomDonutChart(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            data = categoryGroups,
                            colors = colors,
                            totalAmount = totalPieAmount,
                            centerTitle = amountText1,
                            centerSubtitle = amountText2,
                            isPercentageMode = isPiePercentageMode
                        )
                    }

                    AnimatedVisibility(
                        visible = isPieExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(24.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                val safeCategoryList = billCategoryList + incomeCategoryList
                                categoryGroups.forEachIndexed { index, pair ->
                                    val percent = (pair.second / totalPieAmount) * 100
                                    val cat = safeCategoryList.find { it.name == pair.first }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = onCategoryClick != null) {
                                                onCategoryClick?.invoke(
                                                    pair.first,
                                                    pieBills.filter { it.category == pair.first })
                                            }
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
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "没有记录",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDonutChart(
    modifier: Modifier = Modifier,
    data: List<Pair<String, Double>>,
    colors: List<Color>,
    totalAmount: Double,
    centerTitle: String,
    centerSubtitle: String,
    isPercentageMode: Boolean = true
) {
    val animateRotation = remember { androidx.compose.animation.core.Animatable(0f) }

    // Restart animation whenever the list of bills (or expense/income mode) changes.
    LaunchedEffect(data) {
        animateRotation.snapTo(0f)
        animateRotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // Add another alpha animation for indicator lines and texts that appears after pie is drawn
    val animateAlpha by animateFloatAsState(
        targetValue = if (animateRotation.value >= 360f) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "donut_alpha"
    )

    val textMeasurer = rememberTextMeasurer()
    val centerTitleStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    val centerSubtitleStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        color = Color.Gray
    )

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val sweepAngles = data.map { 360f * (it.second / totalAmount).toFloat() }
        val canvasSize = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        // Shrunk chart pie size slightly to prevent indicators from visually overflowing
        val radius = canvasSize / 2 - 60.dp.toPx()
        val strokeWidth = 14.dp.toPx()

        var startAngle = -90f

        data.forEachIndexed { index, pair ->
            val sweepAngle = sweepAngles[index] * (animateRotation.value / 360f)
            val color = colors[index % colors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            if (animateAlpha > 0f) {
                // Draw indicating lines and text with opacity
                val angleInRadians = (startAngle + sweepAngle / 2) * (Math.PI / 180f)
                val lineLength1 = 15.dp.toPx()
                val lineLength2 = 25.dp.toPx()

                val x1 = center.x + (radius + strokeWidth / 2) * cos(angleInRadians).toFloat()
                val y1 = center.y + (radius + strokeWidth / 2) * sin(angleInRadians).toFloat()

                val x2 =
                    center.x + (radius + strokeWidth / 2 + lineLength1) * cos(angleInRadians).toFloat()
                val y2 =
                    center.y + (radius + strokeWidth / 2 + lineLength1) * sin(angleInRadians).toFloat()

                val isLeft = x2 < center.x
                val x3 = if (isLeft) x2 - lineLength2 else x2 + lineLength2
                val y3 = y2

                drawLine(
                    color = Color.LightGray,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 2f,
                    alpha = animateAlpha
                )
                drawLine(
                    color = Color.LightGray,
                    start = Offset(x2, y2),
                    end = Offset(x3, y3),
                    strokeWidth = 2f,
                    alpha = animateAlpha
                )

                val infoText = if (isPercentageMode) {
                    val percentage = (pair.second / totalAmount) * 100
                    "${pair.first} ${String.format(Locale.getDefault(), "%.1f", percentage)}%"
                } else {
                    "${pair.first} ${String.format(Locale.getDefault(), "%.2f", pair.second)}"
                }

                val textLayoutResult = textMeasurer.measure(
                    text = infoText,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = color.copy(alpha = animateAlpha))
                )

                val textLeft =
                    if (isLeft) x3 - textLayoutResult.size.width - 4.dp.toPx() else x3 + 4.dp.toPx()
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(textLeft, y3 - textLayoutResult.size.height / 2f)
                )
            }

            startAngle += sweepAngle
        }

        // Draw center text
        if (animateAlpha > 0f) {
            val titleLayoutResult =
                textMeasurer.measure(text = centerTitle, style = centerTitleStyle.copy(color = centerTitleStyle.color.copy(alpha = animateAlpha)))
            val subtitleLayoutResult =
                textMeasurer.measure(text = centerSubtitle, style = centerSubtitleStyle.copy(color = centerSubtitleStyle.color.copy(alpha = animateAlpha)))

            val totalHeight =
                titleLayoutResult.size.height + subtitleLayoutResult.size.height + 4.dp.toPx()

            drawText(
                textLayoutResult = titleLayoutResult,
                topLeft = Offset(
                    center.x - titleLayoutResult.size.width / 2f,
                    center.y - totalHeight / 2f
                )
            )
            drawText(
                textLayoutResult = subtitleLayoutResult,
                topLeft = Offset(
                    center.x - subtitleLayoutResult.size.width / 2f,
                    center.y - totalHeight / 2f + titleLayoutResult.size.height + 4.dp.toPx()
                )
            )
        }
    }
}
