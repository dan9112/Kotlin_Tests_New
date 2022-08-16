@file:OptIn(ExperimentalMaterial3Api::class)

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
import com.livefront.sealedenum.GenSealedEnum
import kotlinx.coroutines.launch
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsTheme
import ru.kamaz.compose_catalog.views.DrawerContentComponent
import ru.kamaz.compose_catalog.views.screens.*
import java.io.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinTestsTheme {
                DrawerAppComponent()
            }
        }
    }

    @Composable
    private fun DrawerAppComponent() {
        // Состояние выдвигаемого ящика - перенести во viewModel
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        // Текущий активный экран - перенести во viewModel
        val currentScreen =
            rememberSaveable { mutableStateOf<DrawerAppScreen>(DrawerAppScreen.StartScreen) }
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
                BodyContentComponent(
                    currentScreen = currentScreen.value,
                    openDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            })
    }

    sealed interface DrawerAppScreen : Serializable {
        object StartScreen : DrawerAppScreen {
            override fun toString() = "Приветственный экран"
        }

        sealed interface Product : DrawerAppScreen {
            object KamAZ4310Screen : Product {
                override fun toString() = "КамАЗ-4310"
            }

            object KamAZ5511Screen : Product {
                override fun toString() = "КамАЗ-5511"
            }

            object KamAZ6282Screen : Product {
                override fun toString() = "КамАЗ-6282"
            }

            object KamAZ6350Screen : Product {
                override fun toString() = "КамАЗ-6350"
            }

            @GenSealedEnum
            companion object
        }
    }

    /**
     * Передаёт соответствующий экран, компонуемый на основе текущего активного экрана.
     */
    @Composable
    fun BodyContentComponent(
        currentScreen: DrawerAppScreen,
        openDrawer: () -> Unit
    ) = when (currentScreen) {
        DrawerAppScreen.StartScreen -> StartScreen(openDrawer)
        DrawerAppScreen.Product.KamAZ4310Screen -> KamAZ4310Screen(openDrawer)
        DrawerAppScreen.Product.KamAZ5511Screen -> KamAZ5511Screen(openDrawer)
        DrawerAppScreen.Product.KamAZ6282Screen -> KamAZ6282Screen(openDrawer)
        DrawerAppScreen.Product.KamAZ6350Screen -> KamAZ6350Screen(openDrawer)
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        KotlinTestsTheme {
            DrawerAppComponent()
        }
    }
}
