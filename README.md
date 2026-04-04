2026年4月4日13:39:41抽取数据类 BillCategoryData 由于 Feature_Home 与 Feature_Calendart
各自是独立的模块，想要共享底层的样式分类和
Icon 数据，就把原本位于 Feature_Home 的相关账单分类数据提取到了 Module_UI。
创建共享 UI 组件 Shared_BillDayGroup 和 Shared_BillItem 参考 HomePage_UI 里面有关展示一日所有账单的整块
UI，我将其抽离到了公共 UI 库 Module_UI 中的
components\Shared_BillDayGroup.kt。包含了左滑删除、右滑拉出的编辑功能逻辑外放以及金额加减的统一样式。因为
Module_UI 需要用到 QBillEntity 等数据结构，所以我也对应在 Module_UI 的 build.gradle.kts 里注册了对
Module_Data 的 api 依赖。
重构 HomePage 对旧代码的处理 为了不破坏首页原来的结构，我巧妙地使用代理：在 HomePage_UI 里原本的
Home_BillItem 与 Home_BillDayGroup 里把内部庞大的长串 Compose 代码删除了，直接分别调用
Shared_BillItem 与 Shared_BillDayGroup。这样不仅完全解耦出了页面逻辑本身，同时让未来只需维护一套代码。
日历页面 CalendarPage 底部呈现点击日期的详情 如果您现在点击日历中的某天，它的记录账单能够用
LazyColumn 动态渲染在日历模块偏下方，这直接复用了刚抽出来的那套
UI（Shared_BillDayGroup）。如果没有账单，则显示一个"当天没有记录"的反馈占位文本。

