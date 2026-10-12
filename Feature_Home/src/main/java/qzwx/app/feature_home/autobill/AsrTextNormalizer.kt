package qzwx.app.feature_home.autobill

import java.time.LocalDate

/**
 * 语音识别文本纠错与统一化：
 * - 修正常见 ASR 同音词
 * - 统一金额/钱包/动作词的写法，提升解析命中率
 */
object AsrTextNormalizer {
    private val moneyUnitFix = Regex("([零〇一二两三四五六七八九十百千万亿点\\d.]+)\\s*(快|块钱|园|圆)")
    private val rmbFix = Regex("(人民币|rmb|RMB|￥|¥)")
    private val cnNumberToken = "[零〇一二两三四五六七八九十百千万亿点\\d]+"
    private val fullDatePattern = Regex("(${cnNumberToken})年(${cnNumberToken})月(${cnNumberToken})(?:日|号)?")
    private val pointTimePattern = Regex("(凌晨|清晨|早上|上午|中午|下午|傍晚|晚上|夜里|今晚)?\\s*(${cnNumberToken})点(半|一刻|三刻|(${cnNumberToken})分?)?")

    private val moneyYuanJiaoFenPattern = Regex("(${cnNumberToken})(?:块|元)(${cnNumberToken})(?:毛|角)(${cnNumberToken})?分?")
    private val moneyYuanJiaoPattern = Regex("(${cnNumberToken})(?:块|元)(${cnNumberToken})(?:毛|角)")
    private val moneyYuanTailDigitPattern = Regex("(${cnNumberToken})(?:块|元)(${cnNumberToken})(?=(?:\\D|$))")
    private val moneyOnlyJiaoFenPattern = Regex("(${cnNumberToken})(?:毛|角)(${cnNumberToken})?分?")
    private val moneyOnlyFenPattern = Regex("(${cnNumberToken})分")
    private val moneyOnlyYuanPattern = Regex("(${cnNumberToken})(?:块|元)")

    private val walletFixMap = mapOf(
        "支福宝" to "支付宝",
        "支付寶" to "支付宝",
        "威信" to "微信",
        "微 信" to "微信",
        "微信支付" to "微信",
        "云闪付" to "云闪付",
        "花贝" to "花呗",
        "花被" to "花呗"
    )
    private val actionFixMap = mapOf(
        "转帐" to "转账",
        "冲值" to "充值",
        "付钱" to "付款",
        "付 款" to "付款",
        "消费了" to "消费",
        "收入了" to "收入"
    )
    private val homophoneFixMap = mapOf(
        "是一" to "十一",
        "要一" to "十一",
        "室友" to "十五",
        "林" to "零",
        "令" to "零",
        "两点钟" to "两点",
        "一点钟" to "一点",
        "四块" to "十块"
    )
    private val fillerWordsPattern = Regex("[啊呀哦额呃嗯唔呢嘛吧啦哈嘿]")
    private val cnDigitMap = mapOf(
        '零' to 0, '〇' to 0,
        '一' to 1, '二' to 2, '两' to 2, '三' to 3, '四' to 4, '五' to 5,
        '六' to 6, '七' to 7, '八' to 8, '九' to 9
    )

    fun normalize(text: String): String {
        if (text.isBlank()) return text
        var result = text
            .replace('：', ':')
            .replace('（', '(')
            .replace('）', ')')
            .replace('　', ' ')

        result = moneyUnitFix.replace(result) { match ->
            val number = match.groupValues[1]
            "${number}块"
        }
        result = rmbFix.replace(result, "元")

        walletFixMap.forEach { (from, to) ->
            result = result.replace(from, to)
        }
        actionFixMap.forEach { (from, to) ->
            result = result.replace(from, to)
        }
        homophoneFixMap.forEach { (from, to) ->
            result = result.replace(from, to)
        }

        result = normalizeDate(result)
        result = normalizeTime(result)
        result = normalizeMoney(result)

        result = fillerWordsPattern.replace(result, "")

        result = result
            .replace(Regex("\\s+"), "")
            .trim()

        return result
    }

    private fun normalizeDate(text: String): String {
        return fullDatePattern.replace(text) { match ->
            val year = parseYearToken(match.groupValues[1]) ?: return@replace match.value
            val month = parseIntegerToken(match.groupValues[2]) ?: return@replace match.value
            val day = parseIntegerToken(match.groupValues[3]) ?: return@replace match.value
            if (month !in 1..12 || day !in 1..31) return@replace match.value
            "%04d-%02d-%02d".format(year, month, day)
        }.replace("今日", "今天")
            .replace("昨日", "昨天")
    }

    private fun normalizeTime(text: String): String {
        return pointTimePattern.replace(text) { match ->
            val period = match.groupValues[1]
            val hour = parseIntegerToken(match.groupValues[2]) ?: return@replace match.value
            val minuteToken = match.groupValues[3]
            val minute = when (minuteToken) {
                "半" -> 30
                "一刻" -> 15
                "三刻" -> 45
                else -> parseIntegerToken(match.groupValues[4]) ?: 0
            }
            "${period}${hour.coerceIn(0, 23)}:${minute.coerceIn(0, 59).toString().padStart(2, '0')}"
        }
    }

    private fun normalizeMoney(text: String): String {
        var result = text

        result = moneyYuanJiaoFenPattern.replace(result) { match ->
            val yuan = parseNumberToken(match.groupValues[1]) ?: return@replace match.value
            val jiao = parseNumberToken(match.groupValues[2]) ?: 0.0
            val fen = parseNumberToken(match.groupValues[3]) ?: 0.0
            formatAmount(yuan + jiao / 10.0 + fen / 100.0)
        }

        result = moneyYuanJiaoPattern.replace(result) { match ->
            val yuan = parseNumberToken(match.groupValues[1]) ?: return@replace match.value
            val jiao = parseNumberToken(match.groupValues[2]) ?: return@replace match.value
            formatAmount(yuan + jiao / 10.0)
        }

        result = moneyYuanTailDigitPattern.replace(result) { match ->
            val yuan = parseNumberToken(match.groupValues[1]) ?: return@replace match.value
            val tailRaw = parseIntegerToken(match.groupValues[2]) ?: return@replace formatAmount(yuan)
            val tail = tailRaw.coerceAtLeast(0)
            val decimal = when {
                tail < 10 -> tail / 10.0
                tail < 100 -> tail / 100.0
                else -> 0.0
            }
            formatAmount(yuan + decimal)
        }

        result = moneyOnlyJiaoFenPattern.replace(result) { match ->
            val jiao = parseNumberToken(match.groupValues[1]) ?: return@replace match.value
            val fen = parseNumberToken(match.groupValues[2]) ?: 0.0
            formatAmount(jiao / 10.0 + fen / 100.0)
        }

        result = moneyOnlyFenPattern.replace(result) { match ->
            val fen = parseNumberToken(match.groupValues[1]) ?: return@replace match.value
            formatAmount(fen / 100.0)
        }

        result = moneyOnlyYuanPattern.replace(result) { match ->
            val amount = parseNumberToken(match.groupValues[1]) ?: return@replace match.value
            formatAmount(amount)
        }

        return result
    }

    private fun parseYearToken(token: String): Int? {
        val digits = token.mapNotNull { ch ->
            when {
                ch.isDigit() -> ch
                cnDigitMap.containsKey(ch) -> (cnDigitMap[ch] ?: 0).digitToChar()
                else -> null
            }
        }.joinToString(separator = "")

        val value = when (digits.length) {
            2 -> 2000 + digits.toInt()
            3, 4 -> digits.toInt()
            else -> parseIntegerToken(token)
        }
        return value?.takeIf { it in 1970..LocalDate.now().year + 3 }
    }

    private fun parseNumberToken(token: String): Double? {
        val normalized = token.trim()
        if (normalized.isBlank()) return null
        normalized.toDoubleOrNull()?.let { return it }

        if (normalized.contains('点')) {
            val parts = normalized.split('点', limit = 2)
            val intPart = parseIntegerToken(parts[0]) ?: 0
            val fraction = parts.getOrNull(1)
                ?.mapNotNull { cnDigitMap[it] ?: it.digitToIntOrNull() }
                ?.joinToString(separator = "")
                .orEmpty()
            return if (fraction.isBlank()) intPart.toDouble() else "$intPart.$fraction".toDoubleOrNull()
        }

        return parseIntegerToken(normalized)?.toDouble()
    }

    private fun parseIntegerToken(token: String): Int? {
        val normalized = token.trim()
        if (normalized.isBlank()) return null
        normalized.toIntOrNull()?.let { return it }

        var result = 0
        var section = 0
        var number = 0
        var hasValue = false

        fun emit(unit: Int) {
            section += (if (number == 0) 1 else number) * unit
            number = 0
        }

        normalized.forEach { ch ->
            when {
                cnDigitMap.containsKey(ch) -> {
                    number = cnDigitMap[ch] ?: 0
                    hasValue = true
                }
                ch == '十' -> {
                    emit(10)
                    hasValue = true
                }
                ch == '百' -> {
                    emit(100)
                    hasValue = true
                }
                ch == '千' -> {
                    emit(1000)
                    hasValue = true
                }
                ch == '万' -> {
                    result += (section + number) * 10000
                    section = 0
                    number = 0
                    hasValue = true
                }
                ch == '亿' -> {
                    result += (section + number) * 100000000
                    section = 0
                    number = 0
                    hasValue = true
                }
            }
        }

        val value = result + section + number
        return if (hasValue || value > 0) value else null
    }

    private fun formatAmount(value: Double): String {
        val normalized = "%.2f".format(value)
            .trimEnd('0')
            .trimEnd('.')
        return "${normalized}元"
    }
}
