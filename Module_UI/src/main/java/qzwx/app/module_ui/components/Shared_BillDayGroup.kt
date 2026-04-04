package qzwx.app.module_ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_ui.data.billCategoryList
import kotlin.collections.forEachIndexed

@Composable
fun Shared_BillItem(bill: QBillEntity) {
    val categoryInfo =
        billCategoryList.find { it.name == bill.category } ?: billCategoryList.first()
    val isExpense = bill.type == BillType.EXPENSE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = categoryInfo.icon,
            contentDescription = bill.category,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Category and Remark
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bill.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!bill.remark.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bill.remark!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Amount
        val amountStr = if (isExpense) "-${
            String.format(
                java.util.Locale.getDefault(),
                "%.2f",
                bill.amount
            )
        }" else "+${
            String.format(
                java.util.Locale.getDefault(),
                "%.2f",
                bill.amount
            )
        }"
        val amountColor =
            if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amountStr,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = amountColor
            )
        }
    }
}

@Composable
fun Shared_BillDayGroup(
    dateStr: String,
    dayOfWeekStr: String,
    expenseTotal: Double,
    incomeTotal: Double,
    bills: List<QBillEntity>,
    onEditBill: (QBillEntity) -> Unit = {},
    onDeleteBill: (QBillEntity) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)

    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Date and Day of Week
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dayOfWeekStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Right: Expense & Income totals
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (expenseTotal > 0) {
                    Text(
                        text = "支出: ${
                            String.format(
                                java.util.Locale.getDefault(),
                                "%.2f",
                                expenseTotal
                            )
                        }",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error // Red text
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (incomeTotal > 0) {
                    Text(
                        text = "收入: ${
                            String.format(
                                java.util.Locale.getDefault(),
                                "%.2f",
                                incomeTotal
                            )
                        }",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50) // Green text
                    )
                }
            }
        }

        // Bill Items wrapped in Surface container
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                bills.forEachIndexed { index, bill ->
                    val editAction = SwipeAction(
                        icon = rememberVectorPainter(Icons.Default.Edit),
                        background = Color.Green.copy(alpha = 0.5f),
                        onSwipe = { onEditBill(bill) }
                    )

                    val deleteAction = SwipeAction(
                        icon = rememberVectorPainter(Icons.Default.Delete),
                        background = Color.Red.copy(alpha = 0.5f),
                        isUndo = true,
                        onSwipe = { onDeleteBill(bill) }
                    )
                    val cancelAction = SwipeAction(
                        icon = {
                            Text(
                                "取消",
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                            )
                        },
                        background = Color.Gray,
                        onSwipe = { },
                        isUndo = true
                    )
                    SwipeableActionsBox(
                        swipeThreshold = 0.dp,
                        startActions = listOf(cancelAction, deleteAction),
                        endActions = listOf(cancelAction, editAction)
                    ) {
                        Shared_BillItem(bill = bill)
                    }
                    if (index < bills.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
