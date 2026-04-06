package qzwx.app.feature_asset.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class AssetRoute : NavKey, java.io.Serializable {
    @Serializable
    data object Asset : AssetRoute()

    @Serializable
    data class WalletDetail(
        val walletTypeName: String,
        val initialBalance: Double = 0.0
    ) : AssetRoute()

}
