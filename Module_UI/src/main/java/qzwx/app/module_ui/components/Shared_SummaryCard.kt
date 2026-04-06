package qzwx.app.module_ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun Shared_SummaryCard(
    expenseLabel: String,
    expenseAmount: Double,
    incomeLabel: String,
    incomeAmount: Double,
    balanceLabel: String,
    balanceAmount: Double,
    modifier: Modifier = Modifier
) {
    val total = expenseAmount + incomeAmount
    val expFraction = if (total > 0) (expenseAmount / total).toFloat() else 0f
    val incFraction = if (total > 0) (incomeAmount / total).toFloat() else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Balance Label and Balance Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = balanceLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "¥ ${String.format(Locale.CHINA, "%.2f", balanceAmount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (balanceAmount > 0) Color(0xFF4CAF50) else Color(0xFFF06292),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expenseLabel,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.width(60.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(expFraction)
                            .background(Color(0xFFF44336), RoundedCornerShape(4.dp))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (expenseAmount > 0) "¥ ${
                        String.format(
                            Locale.CHINA,
                            "%.2f",
                            expenseAmount
                        )
                    }" else "暂无",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Income Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = incomeLabel,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.width(60.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(incFraction)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (incomeAmount > 0) "¥ ${
                        String.format(
                            Locale.CHINA,
                            "%.2f",
                            incomeAmount
                        )
                    }" else "暂无",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
