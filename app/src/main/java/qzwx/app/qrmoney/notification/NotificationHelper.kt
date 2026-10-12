package qzwx.app.qrmoney.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import qzwx.app.qrmoney.MainActivity
import qzwx.app.qrmoney.R

object NotificationHelper {

    private const val CHANNEL_ID = "meal_reminder_channel"

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Android 8+ 创建通知渠道（声音/震动由渠道控制）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "用餐提醒",
                NotificationManager.IMPORTANCE_HIGH // HIGH 才会弹横幅 + 响铃
            ).apply {
                description = "每天 12:00 和 17:00 的用餐提醒"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500) // 震动节奏
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. 点击意图：打开 MainActivity
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // 携带标记，告诉 MainActivity 是从通知点进来的
            putExtra("from_notification", true)
            putExtra("reminder_type", if (title == "午餐时间") "lunch" else "dinner")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.applogo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)                // 点击后自动消失
            .setContentIntent(pendingIntent)    // 绑定点击跳转
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            .build()

        // 4. 显示（不同 id 避免午餐/晚餐互相覆盖）
        val notificationId = if (title == "午餐时间") 1 else 2
        notificationManager.notify(notificationId, notification)
    }
}