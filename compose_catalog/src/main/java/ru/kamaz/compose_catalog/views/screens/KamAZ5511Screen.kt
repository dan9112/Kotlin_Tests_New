package ru.kamaz.compose_catalog.views.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.kamaz.compose_catalog.R
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme

object KamAZ5511Screen : CarScreen(
    imageResource = R.drawable.kamaz5511,
    contentResource = R.string.KamAZ5511_content
) {
    override fun toString() = "КамАЗ-5511"
}

@Preview
@Composable
private fun DefaultPreview() {
    KotlinTestsPreviewTheme {
        KamAZ5511Screen.GetView(openDrawer = {})
    }
}
