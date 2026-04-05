package qzwx.app.feature_asset.nav3

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_asset.screen.AssetsPage
import qzwx.app.feature_asset.screen.WalletDetailPage

fun EntryProviderScope<NavKey>.AssetRouteBuilder(backStack: NavBackStack<NavKey>) {
    entry<AssetRoute.Asset> { AssetsPage(backStack = backStack) }
    entry<AssetRoute.WalletDetail> { route ->
        WalletDetailPage(
            walletTypeName = route.walletTypeName,
            initialBalance = route.initialBalance,
            onBackClick = { backStack.removeLastOrNull() }
        )
    }

}
