1. 图表第三方库 https://ehsannarmani.github.io/ComposeCharts/charts/pie-chart/     （使用文档）
2. 时间日期选择器 https://github.com/maxkeppeler/sheets-compose-dialogs


1. 在钱包页面解决数据没有及时更新问题：该将 SharedBillViewModel 直接绑定到 Activity（而不是在每个组合页面单独保留）。当
   AssetsPage 和 WalletDetailPage 都依赖同一个持久化、Activity 作用域的 ViewMode。

