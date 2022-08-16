package ru.kamaz.compose_catalog.views.screens

import androidx.compose.runtime.Composable
import com.livefront.sealedenum.GenSealedEnum

sealed interface DrawerAppScreen {
    @Composable
    fun GetView(openDrawer: () -> Unit)

    sealed interface Product : DrawerAppScreen {
        @GenSealedEnum
        companion object
    }
}
