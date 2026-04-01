给你压缩成一版**能直接照着做的总结**👇

---

# 🧱 模块职责（一句话版）

### 🟦 `:app`

👉 **启动 + 总导航 + 组装**

* MainActivity
* NavHost（总路由）
* Theme入口
* DI入口

---

### 🟩 `:feature:*`

👉 **具体功能实现（页面 + ViewModel）**

* Compose UI
* ViewModel
* 子导航

👉 例：

* home（首页）
* add（记账）
* stats（统计）
* settings（设置）

---

### 🟨 `:data`

👉 **所有数据来源**

* Room（DB / DAO / Entity）
* Repository实现
* 数据转换

---

### 🟪 `:core`

👉 **公共能力（建议拆）**

* `core:common` → 工具类、常量
* `core:ui` → 主题、通用组件

---

# 🚦 导航放哪（重点）

👉 **分层放：**

* `app` 👉 总 NavHost
* `feature` 👉 各自 NavGraph

---

# 🧠 架构关系（核心逻辑）

```
feature → 调用 → repository（接口）
                ↓
             data（实现）
```

👉 feature 不直接碰数据库

---

# ⚠️ 四个原则（避免踩坑）

* ❌ app不写业务
* ❌ feature不碰DAO
* ❌ core不变垃圾桶
* ❌ 导航不集中一坨

---

# 🎯 一句话记住

👉 **app管路由，feature管页面，data管数据，core管复用**

---

如果你下一步卡住了（比如 Nav3 怎么拆 or Room 怎么接 ViewModel），我可以直接帮你搭一版最小可运行模板。
