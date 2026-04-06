package qzwx.app.feature_my.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class MyRoute : NavKey, java.io.Serializable {
    @Serializable
    data object My : MyRoute()
}


