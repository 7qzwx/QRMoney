package qzwx.app.qrmoney.scheduler
//负责计算今天 12:00 和 17:00 的毫秒时间戳
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class MealReminderScheduler(private val context: Context) {

    companion object {
        const val ACTION_LUNCH = "com.example.ACTION_LUNCH_REMINDER"
        const val ACTION_DINNER = "com.example.ACTION_DINNER_REMINDER"
        const val REQUEST_CODE_LUNCH = 1001
        const val REQUEST_CODE_DINNER = 1002
    }

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 设置今天的午餐(12:00)和晚餐(17:00)提醒
     */
    fun scheduleToday() {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // 午餐 12:00
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1) // 已过则设明天
        }
        setExactAlarm(REQUEST_CODE_LUNCH, ACTION_LUNCH, calendar.timeInMillis)

        // 晚餐 17:00
        calendar.set(Calendar.HOUR_OF_DAY, 17)
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        setExactAlarm(REQUEST_CODE_DINNER, ACTION_DINNER, calendar.timeInMillis)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun setExactAlarm(requestCode: Int, action: String, triggerAt: Long) {
        val intent = Intent(context, MealAlarmReceiver::class.java).apply {
            this.action = action
        }
        // FLAG_IMMUTABLE 是 Android 12+ 的安全要求
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 关键：setExactAndAllowWhileIdle 能穿透 Doze 模式
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }
}