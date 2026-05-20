package qzwx.app.qrmoney

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.qrmoney.widgets.QWidgetManager
import javax.inject.Inject

@HiltAndroidApp
class QZWXApplication : Application() {

    // 1. 注入 Dao
    @Inject
    lateinit var billDao: QBillDao

    // 2. 定义一个应用级别的协程作用域
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        // 3. 开始自动监听数据库更新
        observeDatabaseChanges()
    }

    private fun observeDatabaseChanges() {
        applicationScope.launch(Dispatchers.IO) { // 改为 IO 线程监听数据库
            billDao.getAllBills()
                .distinctUntilChanged() // 只有数据真变了才触发
                .collect {
                    // 打印日志确认是否触发了 collect
                    android.util.Log.d("WidgetUpdate", "数据库变动，尝试刷新小组件")

                    // 给数据库一点点写入完成的时间缓冲
                    kotlinx.coroutines.delay(300)

                    // 触发小组件刷新（直接调用管理器）
                    QWidgetManager.updateAll(this@QZWXApplication)

                    // 发送显式广播，供独立注册的 BroadcastReceiver 响应（提高在不同进程/场景下的兼容性）
                    try {
                        val intent = android.content.Intent("qzwx.app.qrmoney.ACTION_BILL_CHANGED")
                        intent.setPackage(this@QZWXApplication.packageName)
                        this@QZWXApplication.sendBroadcast(intent)
                    } catch (e: Exception) {
                        android.util.Log.w("WidgetUpdate", "发送广播失败: ${e.message}")
                    }
                }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun getContext(): Context {
            return context
        }
    }
}