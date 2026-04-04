package qzwx.app.module_data.utils

import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity
import kotlin.random.Random

object MockDataUtils {

    /**
     * 生成大量随机测试账单
     * @param count 要生成的数量
     * @return 账单列表
     */
    fun generateRandomBills(count: Int = 1000): List<QBillEntity> {
        val expenseCategories = listOf("餐饮", "交通", "购物", "娱乐", "生活", "医疗")
        val incomeCategories = listOf("工资", "兼职", "理财", "奖金", "其他")
        val remarks = listOf("测试数据", "日常开销", "补充", null)

        val currentTime = System.currentTimeMillis()
        val oneYearInMillis = 365L * 24 * 60 * 60 * 1000

        return (1..count).map {
            val isExpense = Random.nextBoolean()
            val type = if (isExpense) BillType.EXPENSE else BillType.INCOME
            val category = if (isExpense) expenseCategories.random() else incomeCategories.random()

            // 随机生成 1 到 1000 内金额，保留两位小数
            val amount = (Random.nextDouble(1.0, 1000.0) * 100).toLong() / 100.0

            // 随机时间在过去一年内
            val randomTime = currentTime - Random.nextLong(1, oneYearInMillis)

            QBillEntity(
                amount = amount,
                type = type,
                category = category,
                remark = remarks.random(),
                createdAt = randomTime,
                updatedAt = randomTime
            )
        }
    }
}

