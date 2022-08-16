package ru.kamaz.compose_catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsTheme
import ru.kamaz.compose_catalog.views.DrawerContentComponent
import ru.kamaz.compose_catalog.views.screens.DrawerAppScreen
import ru.kamaz.compose_catalog.views.screens.StartScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinTestsTheme {
                DrawerAppComponent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DrawerAppComponent() {
        // Состояние выдвигаемого ящика - перенести во viewModel
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        // Текущий активный экран - перенести во viewModel
        val currentScreen =
            rememberSaveable { mutableStateOf<DrawerAppScreen>(StartScreen) }
        // Область действия составного объекта
        val coroutineScope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                DrawerContentComponent(
                    currentScreen = currentScreen,
                    closeDrawer = { coroutineScope.launch { drawerState.close() } })
            },
            content = {
                currentScreen.value.GetView(openDrawer = {
                    coroutineScope.launch { drawerState.open() }
                })
            })
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        KotlinTestsPreviewTheme {
            DrawerAppComponent()
        }
    }
}
