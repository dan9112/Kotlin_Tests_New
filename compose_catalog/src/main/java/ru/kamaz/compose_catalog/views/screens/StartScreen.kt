package ru.kamaz.compose_catalog.views.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme

object StartScreen : DrawerAppScreen() {
    override fun toString() = "КамАЗ"
}

@Preview
@Composable
private fun DefaultPreview() {
    KotlinTestsPreviewTheme {
        StartScreen.GetView(openDrawer = {})
    }
}
