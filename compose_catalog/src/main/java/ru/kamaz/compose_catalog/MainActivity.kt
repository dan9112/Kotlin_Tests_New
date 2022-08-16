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
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsTheme
import ru.kamaz.compose_catalog.views.DrawerContentComponent

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()

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
        // Состояние выдвигаемого ящика - перенести во viewModel?
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        // Текущий активный экран
        val currentScreen = viewModel.currentScreen.collectAsState()
        // Область действия составного объекта
        val coroutineScope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                DrawerContentComponent(
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
