package qzwx.app.feature_home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import qzwx.app.feature_home.viewmodel.HomePage_VM
import qzwx.app.module_data.entity.BillType
import qzwx.app.module_data.entity.QBillEntity


@Composable
fun HomePage(
    viewModel: HomePage_VM = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var amount by rememberSaveable { mutableStateOf("") }
        var category by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            value = amount, onValueChange = { amount = it },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "金额") }
        )
        OutlinedTextField(
            value = category, onValueChange = { category = it },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            placeholder = { Text(text = "种类") }
        )
        Button(
            onClick = {
                viewModel.insertBill(
                    QBillEntity(
                        amount = amount.toLongOrNull() ?: 0L,
                        type = BillType.EXPENSE,
                        category = category,
                    )
                )
            }
        ) {
            Text(text = "添加")
        }
    }
}
