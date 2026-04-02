package qzwx.app.feature_statistic.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed class StatisticRoute : NavKey {
    @Serializable
    data object Statictic : StatisticRoute()
}


