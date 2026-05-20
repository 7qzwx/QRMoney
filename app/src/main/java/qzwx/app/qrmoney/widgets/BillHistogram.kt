package qzwx.app.qrmoney.widgets

// 确保导入 Glance 的 API


import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import qzwx.app.module_data.QBillDatabase
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

object QWidgetPrefs {
    val isExpenseKey = booleanPreferencesKey("is_expense")
    val isLast7DaysKey = booleanPreferencesKey("is_last_7_days")
    val isExpandedKey = booleanPreferencesKey("is_expanded")
}


class ToggleWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val keyName = parameters[ActionParameters.Key<String>("key")]

        updateAppWidgetState(context, glanceId) { prefs ->
            when (keyName) {
                "isExpense" -> {
                    val current = prefs[QWidgetPrefs.isExpenseKey] ?: true
                    prefs[QWidgetPrefs.isExpenseKey] = !current
                }

                "isLast7Days" -> {
                    val current = prefs[QWidgetPrefs.isLast7DaysKey] ?: false
                    prefs[QWidgetPrefs.isLast7DaysKey] = !current
                }

                "isExpanded" -> {
                    val current = prefs[QWidgetPrefs.isExpandedKey] ?: false
                    prefs[QWidgetPrefs.isExpandedKey] = !current
                }
            }
        }
        QBillHistogram().update(context, glanceId)
    }
}

// 工具类：用于触发所有小组件实例的刷新
object QWidgetManager {
    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(QBillHistogram::class.java)
        ids.forEach { id ->
            QBillHistogram().update(context, id)
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QWidgetEntryPoint {
    fun qBillDao(): QBillDao
}


// 账单近七日收支柱状图 Widget
class QBillHistogram : GlanceAppWidget() {
    // 关键：指定使用 Preferences 管理状态
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint =
            EntryPointAccessors.fromApplication(appContext, QWidgetEntryPoint::class.java)
        val billDao = entryPoint.qBillDao()

        provideContent {
            val prefs = currentState<Preferences>()
            val isExpense = prefs[QWidgetPrefs.isExpenseKey] ?: true
            val isLast7Days = prefs[QWidgetPrefs.isLast7DaysKey] ?: false
            val isExpanded = prefs[QWidgetPrefs.isExpandedKey] ?: false

            // 计算日期
            val localDates = if (isLast7Days) {
                (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
            } else {
                val monday =
                    LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                (0..6).map { monday.plusDays(it.toLong()) }
            }

            // 获取数据 (挂起函数在 provideGlance 中安全调用)
            val chartData = localDates.map { date ->
                val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val end =
                    date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val bills = runBlocking { billDao.getBillsBetweenOnce(start, end) }

                val typeToFilter = if (isExpense) BillType.EXPENSE else BillType.INCOME
                val amount = bills.filter { it.type == typeToFilter }.sumOf { it.amount }

                val label =
                    if (isLast7Days && date == LocalDate.now()) "今天" else toChinese(date.dayOfWeek)
                ChartData(label, amount)
            }

            GlanceTheme {
                QBillHistogramUI(chartData, isExpense, isLast7Days, isExpanded)
            }
        }
    }
}

data class ChartData(val label: String, val value: Double)
class QReceiver_BillHistogram : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = QBillHistogram()
}

@SuppressLint("RestrictedApi")
@Composable
private fun QBillHistogramUI(
    data: List<ChartData>,
    isExpense: Boolean,
    isLast7Days: Boolean,
    isExpanded: Boolean
) {
    val primaryColor = ColorProvider(
        day = MaterialTheme.colorScheme.primary,
        night = MaterialTheme.colorScheme.primary
    ) // 根据你的主题修改
    val surfaceColor = ColorProvider(
        day = MaterialTheme.colorScheme.surface,
        night = MaterialTheme.colorScheme.surface
    )
    val barColor = if (isExpense) Color(0xFFF44336) else Color(0xFF4CAF50)

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(surfaceColor)
            .cornerRadius(16.dp)
    ) {
        // --- Header ---
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // 左侧标题和展开逻辑
            Row(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.clickable(
                        actionRunCallback<ToggleWidgetAction>(
                            actionParametersOf(
                                ActionParameters.Key<String>(
                                    "key"
                                ) to "isLast7Days"
                            )
                        )
                    )
                ) {
                    Text(
                        text = if (isLast7Days) "近七天统计" else "本周统计",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )
                }

                Spacer(GlanceModifier.width(4.dp))
            }

            // 右侧 支出/收入 切换器
            Row(
                modifier = GlanceModifier
                    .cornerRadius(8.dp)
                    .background(ColorProvider(Color.LightGray.copy(alpha = 0.2f)))
            ) {
                listOf(true to "支出", false to "收入").forEach { (type, label) ->
                    val isActive = isExpense == type
                    Box(
                        modifier = GlanceModifier
                            .background(if (isActive) primaryColor else ColorProvider(Color.Transparent))
                            .cornerRadius(8.dp)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .clickable(
                                actionRunCallback<ToggleWidgetAction>(
                                    actionParametersOf(
                                        ActionParameters.Key<String>("key") to "isExpense"
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isActive) ColorProvider(Color.White) else primaryColor
                            )
                        )
                    }
                }
            }
        }

        // --- 柱状图区域 ---
        val maxValue = data.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1.0
        Row(
            modifier = GlanceModifier.fillMaxWidth().height(120.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Vertical.Bottom
        ) {
            data.forEach { item ->
                val fraction = (item.value / maxValue).toFloat().coerceIn(0.05f, 1f)
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.Vertical.Bottom,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    if (item.value > 0) {
                        Text(text = "${item.value.toInt()}", style = TextStyle(fontSize = 9.sp))
                    }
                    Box(
                        modifier = GlanceModifier
                            .width(14.dp)
                            .height((80 * fraction).dp)
                            .background(barColor)

                    ) {}
                    Spacer(GlanceModifier.height(4.dp))
                    Text(text = item.label, style = TextStyle(fontSize = 10.sp))
                }
            }
        }

        // --- 展开详情部分 ---
        if (isExpanded) {
            val total = data.sumOf { it.value }
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        "总金额",
                        style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.Gray))
                    )
                    Text(
                        "¥${String.format("%.2f", total)}",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        "日均",
                        style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.Gray))
                    )
                    Text(
                        "¥${String.format("%.2f", total / 7)}",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

private fun toChinese(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "一"
    DayOfWeek.TUESDAY -> "二"
    DayOfWeek.WEDNESDAY -> "三"
    DayOfWeek.THURSDAY -> "四"
    DayOfWeek.FRIDAY -> "五"
    DayOfWeek.SATURDAY -> "六"
    DayOfWeek.SUNDAY -> "日"
}


// --- 仓库实现 ---
interface BillRepository {
    suspend fun getDailyAmounts(dates: List<LocalDate>, isExpense: Boolean): List<Double>
}

class BillRepositoryImpl(private val billDao: QBillDao) : BillRepository {
    override suspend fun getDailyAmounts(dates: List<LocalDate>, isExpense: Boolean): List<Double> {
        return dates.map { date ->
            val start = date.atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8)) * 1000
            val end = date.plusDays(1).atStartOfDay()
                .toEpochSecond(ZoneOffset.ofHours(8)) * 1000
            val bills: List<QBillEntity> =
                billDao.getBillsBetweenOnce(start, end)
            val filtered = if (isExpense) {
                bills.filter { it.type == BillType.EXPENSE }
            } else {
                bills.filter { it.type == BillType.INCOME }
            }
            filtered.sumOf { it.amount }
        }
    }
}

fun getBillRepository(context: Context): BillRepository {
    val db = Room.databaseBuilder(
        context.applicationContext,
        QBillDatabase::class.java,
        "QBill数据库"
    ).build()
    return BillRepositoryImpl(db.qBillDao())
}

