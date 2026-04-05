package qzwx.app.module_ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BillCategory(
    val name: String,
    val icon: ImageVector
)

val billCategoryList = listOf(
    BillCategory(
        name = "饮食",
        icon = Icons.Rounded.LocalDining
    ),
    BillCategory(
        name = "购物",
        icon = Icons.Rounded.ShoppingBag
    ),
    BillCategory(
        name = "交通",
        icon = Icons.Rounded.Commute
    ),
    BillCategory(
        name = "住宿",
        icon = Icons.Rounded.HomeWork
    ),
    BillCategory(
        name = "娱乐",
        icon = Icons.Rounded.SportsEsports
    ),
    BillCategory(
        name = "医药",
        icon = Icons.Rounded.LocalHospital
    ),
    BillCategory(
        name = "教育",
        icon = Icons.Rounded.School
    ),
    BillCategory(
        name = "其它",
        icon = Icons.Rounded.LocalPlay
    ),
)

val incomeCategoryList = listOf(
    BillCategory(
        name = "工资",
        icon = Icons.Rounded.Work
    ),
    BillCategory(
        name = "奖金",
        icon = Icons.Rounded.ModeNight
    ),
    BillCategory(
        name = "兼职",
        icon = Icons.Rounded.AccessTime
    ),
    BillCategory(
        name = "借入",
        icon = Icons.Rounded.AccountBalance
    ),
    BillCategory(
        name = "其他",
        icon = Icons.Rounded.LocalPlay
    ),
)