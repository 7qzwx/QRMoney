package qzwx.app.feature_my.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class MyRoute : NavKey, java.io.Serializable {
    //---我的页面路由---
    @Serializable
    data object My : MyRoute()

    @Serializable
    data object DataBackUp : MyRoute()

    //---愿望单页面路由---
    @Serializable
    data object QWish : MyRoute()

    @Serializable
    data object AddNewWish : MyRoute()

    @Serializable
    data class WishDetail(val wishId: Long) : MyRoute()

    @Serializable
    data class EditWish(val wishId: Long) : MyRoute()

    //---小组件页面路由---
    @Serializable
    data object QWidgets : MyRoute()
}
