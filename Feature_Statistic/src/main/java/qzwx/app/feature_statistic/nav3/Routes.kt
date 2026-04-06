package qzwx.app.feature_statistic.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed class StatisticRoute : NavKey, java.io.Serializable {
    @Serializable
    data object Statictic : StatisticRoute()
}


