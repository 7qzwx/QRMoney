package qzwx.app.feature_home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import qzwx.app.feature_home.components.AddNewBill
import qzwx.app.feature_home.components.Home_SnakBar
import qzwx.app.feature_home.viewmodel.HomePage_VM
import qzwx.app.module_data.entity.QBillEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: HomePage_VM = hiltViewModel()
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    // Draft state for undo functionality
    var draftBill by remember { mutableStateOf<QBillEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        AddNewBill(
            initialBill = draftBill,
            onDismissRequest = {
                showBottomSheet = false
            },
            onSaveRequest = { bill ->
                scope.launch {
                    draftBill = bill
                    val insertedId = viewModel.insertAndReturnId(bill)

                    showBottomSheet = false

                    val result = snackbarHostState.showSnackbar(
                        message = "已新增账单",
                        actionLabel = "撤销",
                        duration = SnackbarDuration.Short
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.deleteBill(bill.copy(id = insertedId))
                        showBottomSheet = true
                    } else {
                        draftBill = null
                    }
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { Home_SnakBar(snackbarHostState) },  //SnakBar用于新增数据后重新编辑
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            item {
                Button(onClick = {
                    showBottomSheet = true
                }) {
                    Text(text = "新增账单")
                }
            }
        }
    }
}
