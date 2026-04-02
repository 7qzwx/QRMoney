package qzwx.app.feature_asset.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class AssetRoute : NavKey {
    @Serializable
    data object Asset : AssetRoute()
}