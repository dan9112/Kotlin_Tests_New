package ru.kamaz.compose_catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsTheme
import ru.kamaz.compose_catalog.views.DrawerContent
import ru.kamaz.compose_catalog.views.screens.StartScreen
import androidx.lifecycle.viewmodel.compose.viewModel

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
    private fun DrawerAppComponent(viewModel: MainViewModel = viewModel()) {
        // Состояние выдвигаемого ящика - перенести во viewModel?
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        // Область действия составного объекта
        val coroutineScope = rememberCoroutineScope()
        // Текущий активный экран
        val currentScreen = viewModel.currentScreen.collectAsState(initial = StartScreen)

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                DrawerContent(
                    setScreen = { viewModel.setScreen(it) },
                    getScreen = { currentScreen.value }
                ) { coroutineScope.launch { drawerState.close() } }
            }
        ) { currentScreen.value.GetView { coroutineScope.launch { drawerState.open() } } }
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        KotlinTestsPreviewTheme {
            DrawerAppComponent()
        }
    }
}
