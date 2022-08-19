package ru.kamaz.compose_catalog.views.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.kamaz.compose_catalog.R
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme

object KamAZ6282Screen : CarScreen(
    imageResource = R.drawable.kamaz6282,
    contentResource = R.string.KamAZ6282_content
) {
    override fun toString() = run {
        "КамАЗ-6282"
    }
}

@Preview
@Composable
private fun DefaultPreview() {
    KotlinTestsPreviewTheme {
        KamAZ6282Screen.GetView(openDrawer = {})
    }
}
