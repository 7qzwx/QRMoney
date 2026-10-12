package qzwx.app.feature_home.autobill.voice

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import qzwx.app.feature_home.autobill.AsrTextNormalizer

/**
 * Vosk 离线语音识别实现 - 简化版
 *
 * ✅ 改进：
 * - 直接使用全局 VoskModelManager 的模型（无需重复加载）
 * - 毫秒级可用（没有加载延迟）
 * - 仅管理识别会话，不管理模型生命周期
 *
 * 使用流程：
 * 1. Application 启动时自动初始化模型（VoskModelManager.initialize）
 * 2. 创建 OfflineVoskTranscriber 实例
 * 3. 直接调用 startListening（无需 ensureModelLoaded）
 */
class OfflineVoskTranscriber(
    private val context: Context
) : Closeable {

    companion object {
        private const val TAG = "OfflineVoskTranscriber"
        private const val ENDPOINTER_START_DELAY = 1.2f
        private const val ENDPOINTER_END_DELAY = 0.6f
        private const val ENDPOINTER_MAX_END_DELAY = 12.0f
        private const val MAX_ALTERNATIVES = 5

        private val domainHints = listOf(
            "花", "用", "支付", "付款", "消费", "买", "收", "赚", "入账", "转入", "转出", "充值",
            "元", "块", "人民币", "微信", "支付宝", "现金", "银行卡", "零钱", "云闪付", "花呗", "信用卡",
            "早餐", "午餐", "晚餐", "外卖", "打车", "地铁", "公交", "房租", "电影", "医疗", "药",
            "今天", "昨天", "前天", "明天", "中午", "下午", "晚上"
        )
    }

    private data class TranscriptCandidate(
        val text: String,
        val confidence: Double? = null
    )

    // 当前会话状态
    private var speechService: SpeechService? = null
    private var onPartialTranscript: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    @Volatile
    private var sessionTranscript: String = ""

    @Volatile
    private var latestPartialTranscript: String = ""

    @Volatile
    private var finalTranscriptSnapshot: String? = null

    @Volatile
    private var stopSignal: CountDownLatch? = null

    @Volatile
    private var isListening: Boolean = false

    /**
     * 检查模型是否已准备好
     * （实际上检查的是全局 VoskModelManager）
     */
    fun isModelReady(): Boolean {
        return VoskModelManager.isReady()
    }

    /**
     * 等待并获取全局模型
     * 如果模型未加载，等待最多 timeoutSeconds
     */
    suspend fun ensureModelLoaded(timeoutSeconds: Int = 60): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                if (VoskModelManager.isReady()) {
                    return@runCatching Unit
                }

                Log.d(TAG, "模型尚未加载，等待全局初始化完成（最多 ${timeoutSeconds}s）...")

                var waited = 0
                while (!VoskModelManager.isReady() && waited < timeoutSeconds * 10) {
                    kotlinx.coroutines.delay(100)
                    waited++
                }

                if (!VoskModelManager.isReady()) {
                    val error = VoskModelManager.getInitError() ?: "模型加载超时"
                    throw IllegalStateException(error)
                }

                Log.d(TAG, "模型已就绪")
                Unit
            }
        }
    }

    /**
     * 启动语音识别
     * ✅ 前提条件：模型必须已加载（由 Application 在启动时自动完成）
     */
    @SuppressLint("MissingPermission")
    fun startListening(
        onPartialTranscript: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // 获取全局模型
        val model = VoskModelManager.getModel()
        if (model == null) {
            Log.e(TAG, "模型未加载，无法启动识别")
            onError("语音识别初始化失败：模型加载中或加载失败，请稍后重试")
            return
        }

        stopInternal() // 清理前一个会话

        sessionTranscript = ""
        this.onPartialTranscript = onPartialTranscript
        this.onError = onError
        isListening = true

        try {
            val recognizer = createRecognizer(model)
            configureRecognizer(recognizer)

            speechService = SpeechService(recognizer, 16000F).apply {
                startListening(object : RecognitionListener {
                    override fun onPartialResult(hypothesis: String?) {
                        if (!hypothesis.isNullOrBlank()) {
                            val text = extractJsonText(hypothesis)
                            if (text.isNotBlank()) {
                                latestPartialTranscript = text
                                this@OfflineVoskTranscriber.onPartialTranscript?.invoke(
                                    appendTranscript(sessionTranscript, text)
                                )
                            }
                        }
                    }

                    override fun onResult(result: String?) {
                        if (!result.isNullOrBlank()) {
                            val text = extractBestText(result)
                            if (text.isNotBlank()) {
                                sessionTranscript = appendTranscript(sessionTranscript, text)
                                finalTranscriptSnapshot = sessionTranscript
                                this@OfflineVoskTranscriber.onPartialTranscript?.invoke(
                                    sessionTranscript
                                )
                            }
                        }
                    }

                    override fun onFinalResult(result: String?) {
                        if (!result.isNullOrBlank()) {
                            val text = extractBestText(result)
                            if (text.isNotBlank()) {
                                sessionTranscript = appendTranscript(sessionTranscript, text)
                                finalTranscriptSnapshot = sessionTranscript
                                this@OfflineVoskTranscriber.onPartialTranscript?.invoke(
                                    sessionTranscript
                                )
                            }
                        }
                        stopSignal?.countDown()
                    }

                    override fun onError(exception: Exception?) {
                        this@OfflineVoskTranscriber.onError?.invoke(
                            exception?.message ?: "Vosk 语音识别发生错误"
                        )
                        isListening = false
                        stopSignal?.countDown()
                    }

                    override fun onTimeout() {
                        this@OfflineVoskTranscriber.onError?.invoke("语音识别超时，请检查是否有输入")
                        isListening = false
                        stopSignal?.countDown()
                    }
                })
            }

            Log.d(TAG, "语音识别已启动")
        } catch (securityException: SecurityException) {
            this.onError?.invoke(securityException.message ?: "麦克风权限不足")
            stopInternal()
            isListening = false
            Log.e(TAG, "麦克风权限错误", securityException)
        } catch (e: Exception) {
            this.onError?.invoke("启动语音识别失败：${e.message}")
            stopInternal()
            isListening = false
            Log.e(TAG, "启动识别失败", e)
        }
    }

    private fun createRecognizer(model: org.vosk.Model): Recognizer {
        val grammar = VoskGrammarProvider.buildAutoBillGrammarJson()
        return runCatching {
            Recognizer(model, 16000.0f, grammar)
        }.onFailure {
            Log.w(TAG, "语法约束识别器创建失败，回退到默认识别器: ${it.message}")
        }.getOrElse {
            Recognizer(model, 16000.0f)
        }
    }

    /**
     * 停止识别并获取结果
     */
    suspend fun stopListening(): String? = withContext(Dispatchers.Default) {
        isListening = false
        val signal = CountDownLatch(1)
        stopSignal = signal

        speechService?.let {
            runCatching { it.stop() }
        }

        // 等待最终结果
        runCatching { signal.await(700, TimeUnit.MILLISECONDS) }

        // 优先级：最终结果 > 完整结果 > 部分结果
        val transcript = finalTranscriptSnapshot?.trim()?.takeIf { it.isNotBlank() }
            ?: sessionTranscript.trim().takeIf { it.isNotBlank() }
            ?: latestPartialTranscript.trim().takeIf { it.isNotBlank() }

        stopInternal()
        Log.d(TAG, "语音识别已停止，获得结果: $transcript")
        transcript
    }

    /**
     * 取消识别（不等待结果）
     */
    fun cancelListening() {
        isListening = false
        stopInternal()
        Log.d(TAG, "语音识别已取消")
    }

    /**
     * 重置当前会话（保留模型）
     */
    fun resetSession() {
        sessionTranscript = ""
        latestPartialTranscript = ""
        finalTranscriptSnapshot = null
    }

    override fun close() {
        stopInternal()
    }

    private fun stopInternal() {
        speechService?.let {
            runCatching { it.stop() }
        }
        speechService = null
        onPartialTranscript = null
        onError = null
        latestPartialTranscript = ""
        stopSignal = null
    }

    /**
     * Recognizer 高级配置
     */
    private fun configureRecognizer(recognizer: Recognizer) {
        // 启用词汇表
        runCatching {
            recognizer.javaClass.getMethod("setWords", Boolean::class.javaPrimitiveType)
                .invoke(recognizer, true)
        }
        // 启用部分词汇
        runCatching {
            recognizer.javaClass.getMethod("setPartialWords", Boolean::class.javaPrimitiveType)
                .invoke(recognizer, true)
        }
        // 设置最大候选项数
        runCatching {
            recognizer.javaClass.getMethod("setMaxAlternatives", Int::class.javaPrimitiveType)
                .invoke(recognizer, MAX_ALTERNATIVES)
        }
        // 设置端点检测模式
        runCatching {
            recognizer.javaClass.getMethod("setEndpointerMode", Int::class.javaPrimitiveType)
                .invoke(recognizer, 1)
        }
        // 设置端点延迟
        runCatching {
            recognizer.javaClass
                .getMethod("setEndpointerDelays", Float::class.javaPrimitiveType, Float::class.javaPrimitiveType, Float::class.javaPrimitiveType)
                .invoke(recognizer, ENDPOINTER_START_DELAY, ENDPOINTER_END_DELAY, ENDPOINTER_MAX_END_DELAY)
        }
    }

    // ====== 文本处理方法 ======

    private fun extractJsonText(json: String): String {
        return try {
            val obj = JSONObject(json)
            val text = obj.optString("text")
            when {
                text.isNotBlank() -> normalizeTranscript(text)
                obj.has("partial") -> normalizeTranscript(obj.optString("partial"))
                obj.has("alternatives") -> {
                    val alternatives = obj.optJSONArray("alternatives")
                    val first = alternatives?.optJSONObject(0)
                    normalizeTranscript(first?.optString("text").orEmpty())
                }
                else -> ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "JSON 解析失败: ${e.message}")
            ""
        }
    }

    private fun extractBestText(json: String): String {
        val candidates = extractCandidates(json)
        return if (candidates.isEmpty()) "" else selectBestTranscript(candidates)
    }

    private fun extractCandidates(json: String): List<TranscriptCandidate> {
        return try {
            val obj = JSONObject(json)
            val candidates = mutableListOf<TranscriptCandidate>()

            // 提取 alternatives 列表
            val alternatives = obj.optJSONArray("alternatives")
            if (alternatives != null) {
                for (i in 0 until alternatives.length()) {
                    val item = alternatives.optJSONObject(i) ?: continue
                    val text = normalizeTranscript(item.optString("text"))
                    if (text.isBlank()) continue
                    val confidence = item.optDouble("confidence").takeIf { !it.isNaN() }
                    candidates += TranscriptCandidate(text, confidence)
                }
            }

            // 提取主文本
            val text = normalizeTranscript(obj.optString("text"))
            if (text.isNotBlank()) {
                candidates += TranscriptCandidate(text, obj.optDouble("confidence").takeIf { !it.isNaN() })
            }

            candidates
        } catch (e: Exception) {
            Log.w(TAG, "候选项提取失败: ${e.message}")
            emptyList()
        }
    }

    private fun selectBestTranscript(candidates: List<TranscriptCandidate>): String {
        return candidates
            .sortedWith(
                compareByDescending<TranscriptCandidate> { scoreTranscript(it) }
                    .thenByDescending { it.text.length }
            )
            .firstOrNull()
            ?.text
            .orEmpty()
    }

    private fun scoreTranscript(candidate: TranscriptCandidate): Double {
        val text = candidate.text
        if (text.isBlank()) return 0.0

        var score = text.length.coerceAtMost(30) * 0.1

        // 数字/金额相关权重提升
        if (text.any { it.isDigit() } || text.contains(Regex("[零〇一二两三四五六七八九十百千万亿点]+"))) {
            score += 0.8
        }

        // 货币单位权重提升
        if (text.contains("元") || text.contains("块") || text.contains("人民币")) {
            score += 1.2
        }

        // domain hint 权重提升
        if (domainHints.any { text.contains(it) }) {
            score += 0.9
        }

        // 模型置信度
        candidate.confidence?.let { score += it * 2.0 }

        return score
    }

    private fun normalizeTranscript(text: String): String {
        return AsrTextNormalizer.normalize(text)
    }

    private fun appendTranscript(base: String, addition: String): String {
        return listOf(base, addition)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

