package qzwx.app.module_ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Shared_ChartLegend(
    expenseColor: Color = Color(0xFFF44336),
    incomeColor: Color = Color(0xFF4CAF50),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(expenseColor, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("支出", fontSize = 12.sp, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(incomeColor, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("收入", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

