package qzwx.app.feature_asset.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import qzwx.app.feature_asset.R
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.WalletType
import java.util.Locale

@Composable
fun WalletBalanceRow(
    wallet: QWalletEntity,
    onClick: () -> Unit,
    isObscured: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = wallet.type.let { WalletTypeHelper.toDrawableRes(it) }),
            contentDescription = wallet.type.displayName,
            modifier = Modifier.height(22.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
        Text(
            text = wallet.type.displayName,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        if (wallet.isDefault) {
            Text(
                text = "✓",
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Text(
            text = if (isObscured) "***" else WalletTypeHelper.formatAmount(wallet.balance),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

object WalletTypeHelper {
    fun parseWalletType(walletTypeName: String): WalletType {
        return runCatching { WalletType.valueOf(walletTypeName) }
            .getOrElse { WalletType.ALIPAY }
    }

    fun formatAmount(amount: Double): String {
        return String.format(Locale.CHINA, "¥ %,.2f", amount)
    }

    fun toDrawableRes(type: WalletType): Int {
        return when (type) {
            WalletType.CASH -> R.drawable.paycash
            WalletType.ALIPAY -> R.drawable.payzfb
            WalletType.WECHAT -> R.drawable.paywx
            WalletType.BANK_CARD -> R.drawable.paycard
            WalletType.OTHER -> R.drawable.payother
        }
    }
}
