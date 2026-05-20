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
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
// ...existing imports...
import androidx.room.Room
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import android.content.Intent
import qzwx.app.qrmoney.MainActivity
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
    val animStepKey = intPreferencesKey("anim_step")
}


class ToggleWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val keyName = parameters[ActionParameters.Key<String>("key")]
        val explicitValue = parameters[ActionParameters.Key<String>("value")]
        // Update the requested preference
        updateAppWidgetState(context, glanceId) { prefs ->
            when (keyName) {
                "isExpense" -> {
                    if (explicitValue != null) {
                        prefs[QWidgetPrefs.isExpenseKey] = explicitValue.toBoolean()
                    } else {
                        val current = prefs[QWidgetPrefs.isExpenseKey] ?: true
                        prefs[QWidgetPrefs.isExpenseKey] = !current
                    }
                }

                "isLast7Days" -> {
                    if (explicitValue != null) {
                        prefs[QWidgetPrefs.isLast7DaysKey] = explicitValue.toBoolean()
                    } else {
                        val current = prefs[QWidgetPrefs.isLast7DaysKey] ?: false
                        prefs[QWidgetPrefs.isLast7DaysKey] = !current
                    }
                }

                "isExpanded" -> {
                    if (explicitValue != null) {
                        prefs[QWidgetPrefs.isExpandedKey] = explicitValue.toBoolean()
                    } else {
                        val current = prefs[QWidgetPrefs.isExpandedKey] ?: false
                        prefs[QWidgetPrefs.isExpandedKey] = !current
                    }
                }
            }
            // reset animation step to 0 to start animation
            prefs[QWidgetPrefs.animStepKey] = 0
        }

        // trigger a short animation by incrementing animStep a few times
        val ANIM_STEPS = 6
        val ANIM_DELAY_MS = 60L
        for (i in 1..ANIM_STEPS) {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[QWidgetPrefs.animStepKey] = i
            }
            QBillHistogram().update(context, glanceId)
            kotlinx.coroutines.delay(ANIM_DELAY_MS)
        }
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
    // Note: SizeMode.Responsive not used here to keep compatibility; widget supports resizing via provider xml

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
            val animStep = prefs[QWidgetPrefs.animStepKey] ?: 6

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
                QBillHistogramUI(chartData, isExpense, isLast7Days, isExpanded, animStep)
            }
        }
    }
}

data class ChartData(val label: String, val value: Double)
class QReceiver_BillHistogram : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = QBillHistogram()
}

// 点击打开 APP 的 Action
class OpenAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun QBillHistogramUI(
    data: List<ChartData>,
    isExpense: Boolean,
    isLast7Days: Boolean,
    isExpanded: Boolean,
    animStep: Int = 6
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(surfaceColor)
            .cornerRadius(16.dp)
            .clickable(actionRunCallback<OpenAppAction>()) // 点击打开 APP
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
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Column(
                        modifier = GlanceModifier.clickable(
                            actionRunCallback<ToggleWidgetAction>(
                                actionParametersOf(
                                    ActionParameters.Key<String>("key") to "isLast7Days"
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

                    Spacer(GlanceModifier.width(6.dp))

                    // 展开/收起 按钮
                    Box(
                        modifier = GlanceModifier.clickable(
                            actionRunCallback<ToggleWidgetAction>(
                                actionParametersOf(
                                    ActionParameters.Key<String>("key") to "isExpanded"
                                )
                            )
                        ).padding(4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "收起" else "详情",
                            style = TextStyle(fontSize = 12.sp, color = primaryColor)
                        )
                    }
                }

                Spacer(GlanceModifier.width(4.dp))
            }

            // 右侧 支出/收入 切换器
            Row(
                modifier = GlanceModifier
                    .cornerRadius(8.dp)
                    .background(
                        ColorProvider(
                            day = Color.LightGray.copy(alpha = 0.2f),
                            night = Color.LightGray.copy(alpha = 0.2f)
                        )
                    )
            ) {
                // 支出/收入 两个按钮，传入明确 value
                Box(
                    modifier = GlanceModifier
                        .background(
                            if (isExpense) primaryColor else ColorProvider(day = Color.Transparent, night = Color.Transparent)
                        )
                        .cornerRadius(8.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable(
                            actionRunCallback<ToggleWidgetAction>(
                                actionParametersOf(
                                    ActionParameters.Key<String>("key") to "isExpense",
                                    ActionParameters.Key<String>("value") to "true"
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "支出",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = if (isExpense) ColorProvider(day = Color.White, night = Color.White) else primaryColor
                        )
                    )
                }

                Box(
                    modifier = GlanceModifier
                        .background(if (!isExpense) primaryColor else ColorProvider(day = Color.Transparent, night = Color.Transparent))
                        .cornerRadius(8.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable(
                            actionRunCallback<ToggleWidgetAction>(
                                actionParametersOf(
                                    ActionParameters.Key<String>("key") to "isExpense",
                                    ActionParameters.Key<String>("value") to "false"
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "收入",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = if (!isExpense) ColorProvider(day = Color.White, night = Color.White) else primaryColor
                        )
                    )
                }
            }
        }

        // --- 柱状图区域：样式靠近 DailyStatisticView ---
        val maxValue = data.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1.0
        Row(
            modifier = GlanceModifier.fillMaxWidth().height(210.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Vertical.Bottom
        ) {
                data.forEach { item ->
                // 将 animStep 用作动画进度因子（0..ANIM_STEPS）
                val ANIM_STEPS = 6f
                val animFactor = (animStep.coerceAtLeast(0).coerceAtMost(ANIM_STEPS.toInt()) / ANIM_STEPS)
                val fraction = (item.value / maxValue).toFloat().coerceAtLeast(0.01f) * animFactor
                val isZero = item.value == 0.0

                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.Vertical.Bottom,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    if (!isZero) {
                        // 显示数值，整数则显示整数，否则保留一位小数
                        val valueText = if (item.value % 1.0 == 0.0) item.value.toInt().toString()
                        else String.format(java.util.Locale.getDefault(), "%.1f", item.value)
                        Text(
                            text = valueText,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = ColorProvider(day = Color.Gray, night = Color.Gray)
                            )
                        )
                    }

                    Spacer(GlanceModifier.height(6.dp))

                    Box(
                        modifier = GlanceModifier
                            .width(18.dp)
                            .height((140 * fraction).dp)
                            .cornerRadius(6.dp)
                            .background(barColor)
                    ) {}

                    Spacer(GlanceModifier.height(6.dp))
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
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(day = Color.Gray, night = Color.Gray)
                        )
                    )
                    Text(
                        "¥${String.format(java.util.Locale.getDefault(), "%.2f", total)}",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        "日均",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(day = Color.Gray, night = Color.Gray)
                        )
                    )
                    Text(
                        "¥${String.format(java.util.Locale.getDefault(), "%.2f", total / 7)}",
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



