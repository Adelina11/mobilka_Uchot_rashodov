package com.example.mobilka_uchot_rashodov

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilka_uchot_rashodov.ui.AddScreen
import com.example.mobilka_uchot_rashodov.ui.ExpenseViewModel
import com.example.mobilka_uchot_rashodov.ui.ListScreen
import com.example.mobilka_uchot_rashodov.ui.theme.Mobilka_Uchot_rashodovTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mobilka_Uchot_rashodovTheme {
                val vm: ExpenseViewModel = viewModel()
                var screen by remember { mutableStateOf("list") }

                when (screen) {
                    "list" -> ListScreen(vm, onAdd = { screen = "add" })
                    "add" -> AddScreen(vm, onDone = { screen = "list" }, onBack = { screen = "list" })
                }
            }
        }
    }
}