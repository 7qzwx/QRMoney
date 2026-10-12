package qzwx.app.feature_home.widget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import qzwx.app.feature_home.autobill.BillTextParser
import qzwx.app.feature_home.components.AutoBillVoiceBottomSheet
import qzwx.app.feature_home.viewmodel.HomePage_VM
import qzwx.app.module_ui.theme.QAppTheme

@AndroidEntryPoint
class WidgetVoiceInputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QAppTheme {
                WidgetVoiceInputScreen()
            }
        }
    }
}

@Composable
fun WidgetVoiceInputScreen(viewModel: HomePage_VM = hiltViewModel()) {
    var showVoiceSheet by remember { mutableStateOf(true) }
    var showVoiceConfirmDialog by remember { mutableStateOf(false) }
    var pendingVoiceTranscript by remember { mutableStateOf<String?>(null) }
    var isParsingVoiceTranscript by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val recordAudioPermission = Manifest.permission.RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("需要麦克风权限")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, recordAudioPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(recordAudioPermission)
        }
    }

    if (showVoiceConfirmDialog && !pendingVoiceTranscript.isNullOrBlank()) {
        var editableConfirmText by remember(pendingVoiceTranscript) {
            mutableStateOf(pendingVoiceTranscript.orEmpty())
        }

        AlertDialog(
            onDismissRequest = {
                if (!isParsingVoiceTranscript) {
                    showVoiceConfirmDialog = false
                    pendingVoiceTranscript = null
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("确认识别内容", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "例子：昨天 9：26 支付宝 花了7块钱 买包子",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        val accentBrush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                            )
                        )
                        OutlinedTextField(
                            value = editableConfirmText,
                            onValueChange = { editableConfirmText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(accentBrush)
                                .padding(8.dp),
                            placeholder = { Text("可在此修改识别文本") },
                            singleLine = false,
                            maxLines = 8
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isParsingVoiceTranscript,
                    onClick = {
                        val transcript = editableConfirmText.trim()
                        if (transcript.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请输入识别内容")
                            }
                            return@Button
                        }
                        isParsingVoiceTranscript = true
                        scope.launch {
                            runCatching { BillTextParser.parse(transcript) }
                                .onSuccess { parsed ->
                                    val entity = parsed.toEntity()
                                    viewModel.insertBill(entity)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("已保存：${parsed.amount} ${parsed.category}")
                                    }
                                    showVoiceConfirmDialog = false
                                    pendingVoiceTranscript = null
                                    kotlinx.coroutines.delay(500)
                                    (context as? android.app.Activity)?.finish()
                                }
                                .onFailure { throwable ->
                                    snackbarHostState.showSnackbar(
                                        "语音解析失败：${throwable.message ?: "请重试"}"
                                    )
                                    isParsingVoiceTranscript = false
                                }
                        }
                    }
                ) {
                    Text(if (isParsingVoiceTranscript) "解析中..." else "确认")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isParsingVoiceTranscript,
                    onClick = {
                        showVoiceConfirmDialog = false
                        pendingVoiceTranscript = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    if (showVoiceSheet) {
        AutoBillVoiceBottomSheet(
            onDismissRequest = {
                showVoiceSheet = false
                (context as? android.app.Activity)?.finish()
            },
            onTranscriptReady = { transcript ->
                showVoiceSheet = false
                pendingVoiceTranscript = transcript
                showVoiceConfirmDialog = true
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) {
        // Empty content, UI is in voice sheet and dialog
    }
}

