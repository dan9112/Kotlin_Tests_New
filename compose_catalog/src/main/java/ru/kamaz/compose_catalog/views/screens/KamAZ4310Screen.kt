package ru.kamaz.compose_catalog.views.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.kamaz.compose_catalog.R
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme
import ru.kamaz.compose_catalog.views.screens.DrawerAppScreen.CarScreen

object KamAZ4310Screen : CarScreen(
    imageResource = R.drawable.kamaz4310,
    contentResource = R.string.KamAZ4310_content
) {
    override fun toString() = "КамАЗ-4310"
}

@Preview
@Composable
private fun DefaultPreview() {
    KotlinTestsPreviewTheme {
        KamAZ4310Screen.GetView(openDrawer = {})
    }
}
