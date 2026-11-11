package com.example.mobilka_uchot_rashodov

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilka_uchot_rashodov.ui.AddScreen
import com.example.mobilka_uchot_rashodov.ui.ExpenseViewModel
import com.example.mobilka_uchot_rashodov.ui.ListScreen
import com.example.mobilka_uchot_rashodov.ui.theme.Mobilka_Uchot_rashodovTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ExpenseViewModel = viewModel()
            val themeMode by vm.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            val isDark = when (themeMode) {
                1 -> false // Светлая
                2 -> true  // Тёмная
                else -> systemDark // Авто (системная)
            }

            Mobilka_Uchot_rashodovTheme(darkTheme = isDark) {
                var screen by remember { mutableStateOf("list") }

                when (screen) {
                    "list" -> ListScreen(vm, isDark = isDark, onAdd = { screen = "add" })
                    "add" -> {
                        BackHandler { screen = "list" }
                        AddScreen(
                            vm = vm,
                            isDark = isDark,
                            onDone = { screen = "list" },
                            onBack = { screen = "list" }
                        )
                    }
                }
            }
        }
    }
}