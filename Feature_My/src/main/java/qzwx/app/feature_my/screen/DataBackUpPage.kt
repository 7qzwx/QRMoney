package qzwx.app.feature_my.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import qzwx.app.feature_my.backup.ExportFormat
import qzwx.app.feature_my.backup.ExportScope
import qzwx.app.feature_my.backup.formatDate
import qzwx.app.feature_my.viewmodel.DataBackUp_VM
import java.time.LocalDate
import java.time.ZoneId

/**
 * 数据本地备份页：
 * - 范围：全部账单 / 自定义日期范围
 * - 格式：CSV / JSON / XLSX / PDF
 * - 保存：系统下载目录（MediaStore）/ 自定义位置（SAF）/ 分享（FileProvider + ACTION_SEND）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBackUpPage() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val viewModel: DataBackUp_VM = hiltViewModel(activity)
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // ---- 用户选择状态 ----
    var selectedFormatName by rememberSaveable { mutableStateOf(ExportFormat.CSV.name) }
    var selectedScopeName by rememberSaveable { mutableStateOf(ExportScope.ALL.name) }
    var rangeStart by remember { mutableStateOf<LocalDate?>(null) }
    var rangeEnd by remember { mutableStateOf<LocalDate?>(null) }
    var showRangePicker by remember { mutableStateOf(false) }
    var selectedImportModeName by rememberSaveable { mutableStateOf(DataBackUp_VM.ImportMode.OVERWRITE.name) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val format = ExportFormat.valueOf(selectedFormatName)
    val scope = ExportScope.valueOf(selectedScopeName)
    val importMode = DataBackUp_VM.ImportMode.valueOf(selectedImportModeName)

    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()
    val billCount by viewModel.billCount.collectAsState()

    // 范围转毫秒：开始当天 0 点，结束次日 0 点（含结束日全天）
    val startMillis = rangeStart?.let { it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
    val endMillis = rangeEnd?.let { it.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }

    // 切换范围/日期时刷新预览条数
    LaunchedEffect(selectedScopeName, rangeStart, rangeEnd) {
        viewModel.refreshBillCount(scope, startMillis, endMillis)
    }

    // 系统文件选择器（SAF / ACTION_CREATE_DOCUMENT）：选择保存位置并允许重命名，返回后写入
    val createDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(format.mimeType)
    ) { uri ->
        if (uri != null) {
            viewModel.exportToUri(uri, format, scope, startMillis, endMillis)
        }
    }

    // 系统文件选择器（SAF / ACTION_OPEN_DOCUMENT）：选择本软件导出的 JSON 备份文件
    val openDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importFromUri(uri, importMode)
        }
    }

    // 导入入口：清空模式需二次确认，确认后再打开文件选择器；覆盖模式直接打开
    fun onImportClicked() {
        if (importMode == DataBackUp_VM.ImportMode.CLEAR_THEN_IMPORT) {
            showClearConfirm = true
        } else {
            openDocLauncher.launch(arrayOf("application/json"))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("数据备份") },
                navigationIcon = {
                    IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== 范围选择 =====
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "导出范围",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedScopeName = ExportScope.ALL.name },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = scope == ExportScope.ALL,
                                onClick = { selectedScopeName = ExportScope.ALL.name }
                            )
                            Text(ExportScope.ALL.label, style = MaterialTheme.typography.bodyLarge)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedScopeName = ExportScope.CUSTOM.name },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = scope == ExportScope.CUSTOM,
                                onClick = { selectedScopeName = ExportScope.CUSTOM.name }
                            )
                            Text(ExportScope.CUSTOM.label, style = MaterialTheme.typography.bodyLarge)
                        }

                        if (scope == ExportScope.CUSTOM) {
                            HorizontalDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showRangePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(rangeStart?.let { formatDate(it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) } ?: "开始日期")
                                }
                                Text("~", color = Color.Gray)
                                OutlinedButton(
                                    onClick = { showRangePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(rangeEnd?.let { formatDate(it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) } ?: "结束日期")
                                }
                            }
                            if (rangeStart == null || rangeEnd == null) {
                                Text(
                                    text = "请选择完整的开始与结束日期",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ===== 格式选择 =====
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "导出格式",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider()
                        ExportFormat.entries.forEach { f ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedFormatName = f.name },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = format == f,
                                    onClick = { selectedFormatName = f.name }
                                )
                                Column(Modifier.padding(vertical = 8.dp)) {
                                    Text("${f.label}（.${f.extension}）", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = f.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ===== 导出信息 =====
            item {
                Text(
                    text = when (scope) {
                        ExportScope.ALL -> "将备份全部数据（账单、钱包、负债、愿望、记账天数），共 $billCount 条账单记录"
                        ExportScope.CUSTOM -> {
                            if (rangeStart != null && rangeEnd != null) "该日期范围共 $billCount 条账单记录"
                            else "请先选择日期范围"
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (scope == ExportScope.CUSTOM && (rangeStart == null || rangeEnd == null)) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // ===== 操作按钮（导出 / 分享 同一行） =====
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 导出：打开系统文件选择器，可重命名并选择保存位置
                    Button(
                        onClick = {
                            if (scope == ExportScope.CUSTOM && (rangeStart == null || rangeEnd == null)) return@Button
                            val suggestName = format.defaultFileName(
                                scope, startMillis, endMillis, System.currentTimeMillis()
                            )
                            createDocLauncher.launch(suggestName)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = exportState !is DataBackUp_VM.ExportState.Exporting
                    ) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("导出")
                    }
                    OutlinedButton(
                        onClick = {
                            if (scope == ExportScope.CUSTOM && (rangeStart == null || rangeEnd == null)) return@OutlinedButton
                            viewModel.share(format, scope, startMillis, endMillis) { intent ->
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = exportState !is DataBackUp_VM.ExportState.Exporting
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("分享")
                    }
                }
            }

            // ===== 状态提示 =====
            item {
                when (val state = exportState) {
                    is DataBackUp_VM.ExportState.Idle -> Unit
                    is DataBackUp_VM.ExportState.Exporting -> {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Text("正在生成备份，请稍候…", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                    is DataBackUp_VM.ExportState.Done -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("导出成功", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2E7D32))
                                Spacer(Modifier.height(4.dp))
                                Text(state.message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1B5E20))
                            }
                        }
                    }
                    is DataBackUp_VM.ExportState.Failed -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("导出失败", style = MaterialTheme.typography.titleSmall, color = Color(0xFFC62828))
                                Spacer(Modifier.height(4.dp))
                                Text(state.message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB71C1C))
                            }
                        }
                    }
                }
            }

            // ===== 数据导入 =====
            item {
                Text(
                    text = "数据导入",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "从备份恢复账单",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider()
                        Text(
                            text = "选择本软件导出的 JSON 备份文件（.json），恢复全部数据：账单、钱包、负债、愿望、记账天数",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // 导入模式选择
                        DataBackUp_VM.ImportMode.entries.forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedImportModeName = mode.name },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = importMode == mode,
                                    onClick = { selectedImportModeName = mode.name }
                                )
                                Column(Modifier.padding(vertical = 8.dp)) {
                                    Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        HorizontalDivider()
                        Button(
                            onClick = { onImportClicked() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            enabled = importState !is DataBackUp_VM.ImportState.Importing
                        ) {
                            Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("选择 JSON 文件导入")
                        }
                    }
                }
            }

            // ===== 导入状态提示 =====
            item {
                when (val state = importState) {
                    is DataBackUp_VM.ImportState.Idle -> Unit
                    is DataBackUp_VM.ImportState.Importing -> {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Text("正在解析并写入账单，请稍候…", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                    is DataBackUp_VM.ImportState.Done -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("导入成功", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2E7D32))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "已导入全部数据（${state.modeLabel}），其中账单 ${state.count} 条" +
                                        if (state.myDateRestored) "，并恢复“坚持记账”天数" else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                    is DataBackUp_VM.ImportState.Failed -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("导入失败", style = MaterialTheme.typography.titleSmall, color = Color(0xFFC62828))
                                Spacer(Modifier.height(4.dp))
                                Text(state.message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB71C1C))
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // ===== 日期范围选择弹窗 =====
    if (showRangePicker) {
        CalendarDialog(
            state = rememberUseCaseState(visible = true),
            selection = CalendarSelection.Period(
                // 预选高亮可选；LocalDate 非自比较类型，无法构造 Range<LocalDate>，传 null 让用户直接选择
                selectedRange = null,
                onSelectRange = { start, end ->
                    rangeStart = start
                    rangeEnd = end
                    showRangePicker = false
                }
            ),
            config = CalendarConfig(
                monthSelection = true,
                yearSelection = true,
                style = CalendarStyle.MONTH
            )
        )
    }

    // ===== 清空后导入的二次确认弹窗 =====
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            icon = { Icon(Icons.Outlined.Upload, contentDescription = null) },
            title = { Text("清空现有账单？") },
            text = {
                Text("导入前将删除当前全部数据（$billCount 条账单记录，以及钱包、负债、愿望），此操作不可恢复。\n确认继续导入备份中的内容？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        openDocLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Text("清空并选择文件", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}
