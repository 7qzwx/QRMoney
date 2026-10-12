package qzwx.app.feature_home.autobill

import org.junit.Assert.assertTrue
import org.junit.Test

class AsrTextNormalizerTest {

    @Test
    fun normalizesChineseDateAndAmount() {
        val text = "二零二六年五月二十一号微信花了一百二十八块"
        val normalized = AsrTextNormalizer.normalize(text)

        assertTrue(normalized.contains("2026-05-21"))
        assertTrue(normalized.contains("128元"))
    }

    @Test
    fun normalizesMoneyWithJiaoFen() {
        val text = "交通费三毛五分"
        val normalized = AsrTextNormalizer.normalize(text)

        assertTrue(normalized.contains("0.35元"))
    }

    @Test
    fun normalizesTimeExpression() {
        val text = "今天下午三点半打车"
        val normalized = AsrTextNormalizer.normalize(text)

        assertTrue(normalized.contains("下午3:30"))
    }

    @Test
    fun fixesHomophoneAmount() {
        val text = "午饭要一块钱"
        val normalized = AsrTextNormalizer.normalize(text)

        assertTrue(normalized.contains("11元"))
    }
}

