package qzwx.app.feature_calendart.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class CalendarRoute : NavKey {
    @Serializable
    data object Calendar : CalendarRoute()
}
