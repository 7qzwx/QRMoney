package qzwx.app.feature_asset.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import qzwx.app.feature_asset.component.AssetSummaryCard
import qzwx.app.feature_asset.component.WalletOverviewCard
import qzwx.app.feature_asset.component.DebtOverviewCard

@Composable
fun AssetsPage() {
    var isVisible by remember { mutableStateOf(true) }
    var isFlipped by remember { mutableStateOf(false) }

    Scaffold(
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {

            item {
                AssetSummaryCard(
                    isVisible = isVisible,
                    isFlipped = isFlipped,
                    onVisibilityToggle = { isVisible = !isVisible },
                    onFlippedToggle = { isFlipped = !isFlipped }
                )
            }
            item {
                Crossfade(targetState = isFlipped, label = "overview_crossfade") { flipped ->
                    if (flipped) {
                        DebtOverviewCard(isVisible = isVisible)
                    } else {
                        WalletOverviewCard(isVisible = isVisible)
                    }
                }
            }
        }
    }
}