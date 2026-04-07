package qzwx.app.feature_my.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class MyRoute : NavKey, java.io.Serializable {
    @Serializable
    data object My : MyRoute()

    @Serializable
    data object QWish : MyRoute()

    @Serializable
    data object AddNewWish : MyRoute()

    @Serializable
    data class WishDetail(val wishId: Long) : MyRoute()

    @Serializable
    data class EditWish(val wishId: Long) : MyRoute()
}
