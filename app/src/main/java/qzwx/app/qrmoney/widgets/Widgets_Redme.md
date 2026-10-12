下面是针对你给出的四个 widget 的详细建议与实现说明（包含扩展建议和优先级）。
快速记账输入栏（推荐的首选实现）
目的：最小交互成本，直接在 widget 就能输入自然语言或金额并“记账”，按确认跳转到 App 完成解析或由 widget 后台直接提交。
建议尺寸：2x1 或 4x1（条形）/ 2x2（方块带输入）
布局（条形）：
左侧：单行文本输入（placeholder: “输入，如：午餐 35” 或 “35 餐饮”）
右侧：主动作按钮 “记账”
额外：小设置按钮（小图标）用于切换快速分类/最近快捷项
交互：
文本输入后点击“记账”： widget 触发 PendingIntent → 打开 App 的一个接收 Intent 的 Activity 或服务（或直接由 widget 后台通过 WorkManager 提交）。
若想在 widget 内直接提交并返回结果：使用 Glance + WorkManager 做异步识别并反馈短暂 Toast / 更新 widget 显示（例如变为“已保存：35 餐饮”）。
深度链接/Intent 约定（示例）：
Intent action: com.qrmoney.action.WIDGET_QUICK_ADD
extras: { "text": "午餐 35", "source": "widget_quick_input" }
App 端解析：Feature_Home.autobill 的接收器或某个 IntentHandler 解析自然语言并保存账单，然后导航到详情页或显示确认 UI。
实现方案推荐：
推荐使用 Android Glance（Compose for Widgets）若项目使用 Compose；优点：布局更灵活、易于维护。
兼容方案：如果不想引入新库，使用传统 AppWidgetProvider + RemoteViews，但输入控件在 RemoteViews 中受限 —— 实现“文本输入”通常需要打开一个配置 Activity 来输入，或使用 RemoteViews 的 EditText（limited support）。因此为了实现“直接在 widget 输入自然语言”，更可靠的方法是：widget 中放一个文本字段触发一个小悬浮 / 快速输入 Activity（透明背景）或直接打开 App 的输入界面并带上 source=widget。
后台/权限：
不需要特殊权限；若直接在 widget 后台提交网络，则需要 WorkManager/Foreground service 根据 Android 限制。
文件与代码位置建议：
Feature_Home/src/main/java/.../widget/QuickInputGlance.kt（如果使用 Glance）
Feature_Home/src/main/java/.../widget/QuickInputReceiver.kt（若使用 AppWidgetProvider）
Manifest: 注册 widget provider / service，在 Feature_Home module 的 AndroidManifest 或 module manifest 合并。
优先级：高（优先实现）
估时（实现到可用的 MVP）：1-2 天（如果用 Glance：1 天；用传统 RemoteViews 且需要配置 Activity：1.5–2 天）
今日收支速览（Daily Overview）
目的：在桌面快速查看今日/本周 收入与支出总览，点击可跳转到 App 的详情页或某个统计页
建议尺寸：2x2 或 4x2（信息更多时用 4x2）
布局：
标题行：今日收支
两个数字块并列：总支出（红色），总收入（绿色）
可选：下方小行显示本周/本月按钮切换（或按滑动切换）
交互：
点击 widget 或某个数字块 → 深度链接到 App 的统计/明细页（例如 action: com.qrmoney.action.OPEN_OVERVIEW，extra: "period"="today"|"week"）
数据更新：
更新频率：推模式优先（当 App 有账单变动时，通过 Broadcast/WorkManager update widget）；否则定期更新（例如每 30 分钟）以节省电量。
实现：
Glance 可很好地实现布局 + click handling。
数据来源：本地数据库（Room）或 ViewModel 接口。Widget 通过 WorkManager 或 Glance 的 updateInBackground 拉取汇总数据。
文件建议：
Feature_Home/src/.../widget/DailyOverviewGlance.kt
Feature_Home/src/.../widget/OverviewUpdateWorker.kt
优先级：中高
估时：0.5 - 1 天
快捷固定类别记账栏（4个常用分类按钮）
目的：一键带分类打开快速添加界面，仅需输入金额即保存
建议尺寸：4x1（横向一排）或 2x2（双列两行）
布局：
一排四个大圆形按钮（图标 + 文本：餐饮、交通、购物、工资）
交互：
点击某个分类按钮 → 触发 PendingIntent（带 category extra）：
Option A（更快捷）：直接弹出一个微输入窗口/Activity 只输入金额后提交（带 category）
Option B：打开 App 的添加账单页面并预置分类
技术点：
在 RemoteViews 中可放置四个按钮，各自 PendingIntent 指向同一个 Activity/Service（带不同 extras）
实现建议：
用 Glance/RemoteViews 都可。若要极简实现可先做 PendingIntent 跳转到 AddBillActivity 带 extra category。
文件建议：
Feature_Home/src/.../widget/QuickCategoriesGlance.kt
优先级：中
估时：0.5 - 1 天
空状态“鼓励记账”微件
目的：当当天没有记账记录时，用轻量提示与大“+”按钮引导打开 App 记一笔
建议尺寸：2x2 或 2x1（根据文案）
布局：
文本行：“今天还没记账哦，快来记一笔~”
大号“+”按钮（中间）
交互：
点击“+” → 打开 AddBillActivity 或弹出快速输入
若有当天账单则显示不同视图（比如展示最新一条）
数据逻辑：
需要检查当天记录是否存在（Room 查询），根据结果更新 widget 显示
实现：
Glance 或 RemoteViews + Worker 来更新 UI
文件建议：
Feature_Home/src/.../widget/EncourageWidgetGlance.kt
优先级：低到中（易实现）
额外推荐 widget（可选）
最近 3 笔账单（1x2 或 2x2）：直接在桌面显示最近的三个笔记，快速滑动查看详情。
周消费目标进度条（2x2）：显示本周或本月目标进度，增加成就感。
分类热度/饼图微件（4x2）：简单图形化展示（不过 widget 中复杂绘图受限，需生成位图后显示）
实现技术选择说明（Glance vs RemoteViews）
Glance（androidx.glance:glance）：
优点：用 Compose 风格声明 UI、易于维护、点击处理简单、更现代。
缺点：需要添加依赖（如果项目还没使用），某些 Launcher 兼容性问题要注意，但主流 Launcher 支持良好。

数据 / Deep Link / Intent 约定（建议统一规范）
Quick add Intent:
action: com.qrmoney.action.WIDGET_QUICK_ADD
extras:
"text": String (自然语言或金额)
"category": String? (可选)
"source": "widget_quick_input"|"widget_category"|"widget_plus"
Open overview:
action: com.qrmoney.action.OPEN_OVERVIEW
extras: { "period": "today"|"week"|"month" }
Add bill activity deep link URI（也可用于外部调用）:
qrmoney://add?source=widget&category=餐饮&amount=35&text=午餐%2035

新策略（性能考虑）
优先使用事件驱动刷新：当 App 内账单被创建/删除时，触发 widget 更新（通过 WorkManager 或 AppWidgetManager.updateAppWidget）
备用：定时刷新（建议最小间隔 30 分钟或更长），避免频繁唤醒
避免在主线程做耗时网络/数据库操作：使用 WorkManager / Coroutine + Dispatchers.IO
开发/文件结构建议（在你的工程里）
Module: Feature_Home
新增包：Feature_Home/src/main/java/com/qrmoney/featurehome/widget/
建议文件：
QuickInputGlance.kt 或 QuickInputWidgetProvider.kt
OverviewGlance.kt
QuickCategoriesGlance.kt
EncourageWidgetGlance.kt
widget_provider_info.xml（AppWidgetProviderInfo，如果用 RemoteViews）
AndroidManifest.xml（在 module 中注册 provider / receiver）
AddBillActivity.kt（如果需要弹出轻量输入 Activity，或复用已有添加页）
如果使用 Glance，需要在 module 的 Gradle 添加：
implementation "androidx.glance:glance:1.x.x"（选稳定版本，随项目兼容性）