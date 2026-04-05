package qzwx.app.feature_home.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class HomeRoute : NavKey {
    @Serializable
    data object Home : HomeRoute()

}
