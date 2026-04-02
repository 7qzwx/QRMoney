package qzwx.app.feature_asset.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_asset.screen.AssetsPage

fun EntryProviderScope<NavKey>.AssetRouteBuilder() {
    entry<AssetRoute.Asset> { AssetsPage() }
}
