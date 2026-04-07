package qzwx.app.feature_my.screen.qwish

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import qzwx.app.feature_my.viewmodel.QWishPage_VM
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewWishPage(
    backStack: NavBackStack<NavKey>? = null,
    onBackClick: () -> Unit = { backStack?.removeLastOrNull() },
    wishId: Long? = null // 新增参数，编辑模式
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: QWishPage_VM = hiltViewModel(activity)

    var isEditMode = wishId != null
    var loaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var savedAmount by remember { mutableStateOf("0") }
    var remark by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf(calendar.timeInMillis) }
    var expectedDate by remember { mutableStateOf<Long?>(null) }

    // 编辑模式下加载数据
    LaunchedEffect(wishId) {
        if (isEditMode && !loaded) {
            viewModel.getWishById(wishId!!) {
                it?.let { wish ->
                    name = wish.name
                    targetAmount = wish.targetAmount.toString()
                    savedAmount = wish.savedAmount.toString()
                    remark = wish.remark ?: ""
                    startDate = wish.startDate
                    expectedDate = wish.expectedDate
                }
                loaded = true
            }
        }
    }

    val onConfirm =
        { name: String, targetAmount: Double, savedAmount: Double, remark: String?, startDate: Long, expectedDate: Long? ->
            if (isEditMode && wishId != null) {
                viewModel.updateWish(
                    id = wishId,
                    name = name,
                    targetAmount = targetAmount,
                    savedAmount = savedAmount,
                    remark = remark,
                    startDate = startDate,
                    expectedDate = expectedDate
                )
            } else {
                viewModel.insertWish(
                    name,
                    targetAmount,
                    savedAmount,
                    remark,
                    startDate,
                    expectedDate
                )
            }
            backStack?.removeLastOrNull()
            Unit
        }

    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val amountPattern = remember { Regex("^\\d*(\\.\\d{0,2})?$") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showExpectedDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showStartDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showExpectedDatePicker) {
        val startMidnightUtc = remember(startDate) {
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expectedDate ?: startDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= startMidnightUtc
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showExpectedDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { expectedDate = it }
                    showExpectedDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showExpectedDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val targetVal = targetAmount.toDoubleOrNull() ?: 0.0
    val savedVal = savedAmount.toDoubleOrNull() ?: 0.0
    val isSavedValid = savedAmount.isEmpty() || savedVal <= targetVal
    val canSave = name.isNotBlank() && targetAmount.isNotBlank() && isSavedValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "修改愿望清单" else "新增愿望清单") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 基本信息卡片
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "基本信息",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("愿望名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = targetAmount,
                            onValueChange = {
                                if (it.matches(amountPattern)) targetAmount = it
                            },
                            label = { Text("目标金额") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        OutlinedTextField(
                            value = savedAmount,
                            onValueChange = {
                                if (it.matches(amountPattern)) savedAmount = it
                            },
                            label = { Text("已攒金额") },
                            isError = !isSavedValid,
                            supportingText = if (!isSavedValid) {
                                { Text("不能大于目标") }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            // 日期信息卡片
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "日期设置",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = dateFormat.format(Date(startDate)),
                        onValueChange = { },
                        label = { Text("开始日期") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showStartDatePicker = true
                                    }
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    OutlinedTextField(
                        value = expectedDate?.let { dateFormat.format(Date(it)) } ?: "",
                        placeholder = {
                            Text(
                                "选择一个日期作为DeadLine吧！（可选）",
                                fontSize = MaterialTheme.typography.titleSmall.fontSize
                            )
                        },
                        onValueChange = { },
                        label = { Text("期望日期（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            if (expectedDate != null) {
                                IconButton(onClick = { expectedDate = null }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "清除")
                                }
                            }
                        },
                        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showExpectedDatePicker = true
                                    }
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // 备注卡片
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "其他信息",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("备注 (可选)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (canSave) {
                                    onConfirm(
                                        name,
                                        targetVal,
                                        savedVal,
                                        remark.takeIf { it.isNotBlank() },
                                        startDate,
                                        expectedDate
                                    )
                                }
                            },
                            enabled = canSave
                        ) {
                            Text(if (isEditMode) "更新" else "保存")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}