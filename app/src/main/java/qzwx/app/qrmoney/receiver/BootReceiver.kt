package qzwx.app.qrmoney.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import qzwx.app.qrmoney.scheduler.MealReminderScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 开机后重新设置今天的闹钟
            MealReminderScheduler(context).scheduleToday()
        }
    }
}