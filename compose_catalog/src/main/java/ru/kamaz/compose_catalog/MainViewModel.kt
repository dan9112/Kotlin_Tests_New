package ru.kamaz.compose_catalog

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.compose_catalog.views.screens.DrawerAppScreen
import ru.kamaz.compose_catalog.views.screens.StartScreen

class MainViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow<DrawerAppScreen>(StartScreen)
    val currentScreen: StateFlow<DrawerAppScreen>
        get() = _currentScreen

    fun setScreen(screen: DrawerAppScreen) {
        _currentScreen.value = screen
    }
}
