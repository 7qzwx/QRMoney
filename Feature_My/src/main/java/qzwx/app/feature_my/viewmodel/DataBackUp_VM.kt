package qzwx.app.feature_my.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import qzwx.app.feature_my.backup.BackupMyDateJson
import qzwx.app.feature_my.backup.BackupSaver
import qzwx.app.feature_my.backup.ExportEngine
import qzwx.app.feature_my.backup.ExportFormat
import qzwx.app.feature_my.backup.ExportPayload
import qzwx.app.feature_my.backup.ExportScope
import qzwx.app.feature_my.backup.JsonImporter
import qzwx.app.feature_my.backup.toEntity
import qzwx.app.feature_my.backup.toExportPayload
import qzwx.app.feature_my.repository.DataBackUp_RT
import javax.inject.Inject

/**
 * 数据备份 ViewModel：
 * - Room 查询在 IO 线程，通过 StateFlow 暴露导出状态与待导出条数
 * - 导出：用户通过系统文件选择器（SAF / ACTION_CREATE_DOCUMENT）选择位置并重命名，再写入
 * - 分享：FileProvider + ACTION_SEND
 */
@HiltViewModel
class DataBackUp_VM @Inject constructor(
    private val repository: DataBackUp_RT,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** 当前选中范围的账单条数（预览用） */
    private val _billCount = MutableStateFlow(0)
    val billCount: StateFlow<Int> = _billCount.asStateFlow()

    /** 导出/保存状态 */
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    sealed interface ExportState {
        data object Idle : ExportState
        data object Exporting : ExportState
        data class Done(val fileName: String, val message: String) : ExportState
        data class Failed(val message: String) : ExportState
    }

    /** 导入模式 */
    enum class ImportMode(val label: String, val description: String) {
        CLEAR_THEN_IMPORT(
            "清空后导入",
            "先删除现有全部数据（账单 / 钱包 / 负债 / 愿望），再写入备份内容（整体替换）"
        ),
        OVERWRITE(
            "覆盖导入",
            "按 ID 覆盖写入各表数据，备份中未包含的记录保留"
        )
    }

    /** 导入状态 */
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    sealed interface ImportState {
        data object Idle : ImportState
        data object Importing : ImportState
        data class Done(val count: Int, val modeLabel: String, val myDateRestored: Boolean) : ImportState
        data class Failed(val message: String) : ImportState
    }

    /** 刷新预览条数（IO 线程查询） */
    fun refreshBillCount(scope: ExportScope, startMillis: Long?, endMillis: Long?) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                when (scope) {
                    ExportScope.ALL -> repository.getAllBillsOnce().size
                    ExportScope.CUSTOM -> {
                        if (startMillis == null || endMillis == null) 0
                        else repository.getBillsBetweenOnce(startMillis, endMillis).size
                    }
                }
            }
            _billCount.value = count
        }
    }

    /** 构建导出负载：读取整个数据库（账单 + 钱包/负债/愿望 + 记账天数起点） */
    private suspend fun buildPayload(scope: ExportScope, startMillis: Long?, endMillis: Long?): ExportPayload {
        val bills = when (scope) {
            ExportScope.ALL -> repository.getAllBillsOnce()
            ExportScope.CUSTOM -> repository.getBillsBetweenOnce(startMillis ?: 0L, endMillis ?: Long.MAX_VALUE)
        }
        val myDate = repository.getMyDateOnce()?.let {
            BackupMyDateJson(firstLaunchDate = it.firstLaunchDate, transactionCount = it.transactionCount)
        }
        return bills.toExportPayload(
            scope, startMillis, endMillis,
            myDate = myDate,
            wallets = repository.getAllWalletsOnce(),
            debts = repository.getAllDebtsOnce(),
            wishes = repository.getAllWishesOnce()
        )
    }

    /**
     * 生成备份文件并写入用户通过系统文件选择器选择的位置（Uri）。
     * 选择位置/重命名发生在系统文件选择器中，这里完成真正的写入动作。
     */
    fun exportToUri(uri: Uri, format: ExportFormat, scope: ExportScope, startMillis: Long?, endMillis: Long?) {
        if (_exportState.value == ExportState.Exporting) return
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val payload = withContext(Dispatchers.IO) { buildPayload(scope, startMillis, endMillis) }
                val fileName = format.defaultFileName(scope, startMillis, endMillis, payload.exportedAt)
                val bytes = withContext(Dispatchers.IO) {
                    ExportEngine.build(format, payload, ExportEngine.loadPdfFonts(context))
                }
                withContext(Dispatchers.IO) {
                    BackupSaver.writeToUri(context, uri, bytes)
                }
                _exportState.value = ExportState.Done(fileName, "导出成功：${uri.lastPathSegment ?: fileName}")
            } catch (e: Exception) {
                _exportState.value = ExportState.Failed(e.message ?: "导出失败，请重试")
            }
        }
    }

    /** 生成备份并构建分享 Intent（FileProvider + ACTION_SEND），通过回调交给 UI 启动 */
    fun share(format: ExportFormat, scope: ExportScope, startMillis: Long?, endMillis: Long?, onIntent: (Intent) -> Unit) {
        if (_exportState.value == ExportState.Exporting) return
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val payload = withContext(Dispatchers.IO) { buildPayload(scope, startMillis, endMillis) }
                val fileName = format.defaultFileName(scope, startMillis, endMillis, payload.exportedAt)
                val bytes = withContext(Dispatchers.IO) {
                    ExportEngine.build(format, payload, ExportEngine.loadPdfFonts(context))
                }
                val intent = withContext(Dispatchers.IO) {
                    BackupSaver.buildShareIntent(context, fileName, format.mimeType, bytes)
                }
                _exportState.value = ExportState.Done(fileName, "分享文件已生成：$fileName")
                onIntent(intent)
            } catch (e: Exception) {
                _exportState.value = ExportState.Failed(e.message ?: "分享失败，请重试")
            }
        }
    }

    /**
     * 从用户通过系统文件选择器选中的 JSON 备份文件导入整个数据库。
     * 解析与写库均在 IO 线程；清空模式由单事务保证原子性（账单/钱包/负债/愿望整体替换）。
     * 同时恢复“坚持记账的第 X 天”起点（备份中的首次启动时间）。
     */
    fun importFromUri(uri: Uri, mode: ImportMode) {
        if (_importState.value == ImportState.Importing) return
        viewModelScope.launch {
            _importState.value = ImportState.Importing
            try {
                val backup = withContext(Dispatchers.IO) {
                    val bytes = context.contentResolver.openInputStream(uri)
                        ?.use { it.readBytes() }
                        ?: error("无法读取所选文件")
                    JsonImporter.parse(bytes)
                }
                val bills = backup.bills.map { it.toEntity() }
                withContext(Dispatchers.IO) {
                    repository.importAllData(
                        bills = bills,
                        wallets = backup.wallets,
                        debts = backup.debts,
                        wishes = backup.wishes,
                        myDate = backup.myDate,
                        clearFirst = mode == ImportMode.CLEAR_THEN_IMPORT
                    )
                }
                _importState.value = ImportState.Done(bills.size, mode.label, backup.myDate != null)
                refreshBillCount(ExportScope.ALL, null, null)
            } catch (e: Exception) {
                _importState.value = ImportState.Failed(e.message ?: "导入失败，请重试")
            }
        }
    }
}
