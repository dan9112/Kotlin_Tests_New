package ru.kamaz.compose_catalog.views.screens

import androidx.compose.runtime.Composable

sealed interface DrawerAppScreen {
    @Composable
    fun GetView(openDrawer: () -> Unit)
}
