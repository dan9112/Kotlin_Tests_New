package ru.kamaz.compose_catalog.views.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.kamaz.compose_catalog.R
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme
import ru.kamaz.compose_catalog.views.screens.DrawerAppScreen.CarScreen

object KamAZ6350Screen : CarScreen(
    imageResource = R.drawable.kamaz6350,
    contentResource = R.string.KamAZ6350_content
) {
    override fun toString() = "КамАЗ-6350"
}

@Preview
@Composable
private fun DefaultPreview() {
    KotlinTestsPreviewTheme {
        KamAZ6350Screen.GetView(openDrawer = {})
    }
}
