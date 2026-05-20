package qzwx.app.qrmoney.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // 简单地触发更新所有小组件实例
        CoroutineScope(Dispatchers.IO).launch {
            try {
                QWidgetManager.updateAll(context.applicationContext)
            } catch (e: Exception) {
                android.util.Log.w("BillChangeReceiver", "更新小组件失败: ${e.message}")
            }
        }
    }
}

