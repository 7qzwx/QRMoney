package qzwx.app.qrmoney.widgets


import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class QWidget_Demo : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QWidgetContent()   //Widget内容，每个Widget只能有一个组件
        }
    }
}

class QWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = QWidget_Demo()  //widget接收器，只能接受一个widget
}


@Composable
private fun QWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .background(Color.LightGray)
            .cornerRadius(8.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "这是要给文本测试！",
            style = TextStyle(fontSize = 24.sp)
        )
        Text(
            text = "dateFormat.format(now)",
            style = TextStyle(fontSize = 12.sp)
        )
    }
}


// ✅ Widget1.kt
class Widget1 : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text("Widget 1 内容")
        }
    }
}

class Widget1Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = Widget1()
}

// ✅ Widget2.kt
class Widget2 : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text("Widget 2 内容")
        }
    }
}

class Widget2Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = Widget2()
}