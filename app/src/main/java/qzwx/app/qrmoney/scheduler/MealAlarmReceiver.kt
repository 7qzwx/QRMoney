package qzwx.app.qrmoney.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import qzwx.app.qrmoney.notification.NotificationHelper

//这是广播接收器，闹钟触发时系统会回调这里：
class MealAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // ① 发通知
        val title = when (intent.action) {
            MealReminderScheduler.ACTION_LUNCH -> "午餐时间到！"
            MealReminderScheduler.ACTION_DINNER -> "晚餐时间到！"
            else -> return
        }
        val message = when (intent.action) {
            MealReminderScheduler.ACTION_LUNCH -> "七种文学提醒您：天大地大，吃饭最大🍚"
            MealReminderScheduler.ACTION_DINNER -> "七种文学提醒您：天大地大，吃饭最大🍜"
            else -> "天天开心！"
        }
        NotificationHelper.showNotification(context, title, message)

        // ② 重设下一次（明天同一时间）
        // 注意：必须在 onReceive 里重设，否则闹钟只响一次
        MealReminderScheduler(context).scheduleToday()
    }
}