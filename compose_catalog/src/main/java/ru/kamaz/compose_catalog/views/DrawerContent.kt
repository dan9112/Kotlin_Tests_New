package ru.kamaz.compose_catalog.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme
import ru.kamaz.compose_catalog.views.screens.DrawerAppScreen
import ru.kamaz.compose_catalog.views.screens.DrawerAppScreen.CarScreen
import ru.kamaz.compose_catalog.views.screens.StartScreen
import ru.kamaz.compose_catalog.views.screens.values

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    setScreen: (DrawerAppScreen) -> Unit,
    getScreen: () -> DrawerAppScreen,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet(modifier = Modifier.verticalScroll(rememberScrollState())) {
        with(receiver = StartScreen) {
            NavigationDrawerItem(
                label = { Text(text = toString()) },
                selected = false,
                onClick = {
                    setScreen(this)
                    closeDrawer()
                },
                modifier = Modifier
                    .height(height = 120.dp)
                    .padding(all = 10.dp)
                    .testTag(tag = toString()),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = MaterialTheme.colorScheme.tertiary,
                    unselectedTextColor = MaterialTheme.colorScheme.onTertiary
                )
            )
        }
        Spacer(modifier = Modifier.height(height = 12.dp))
        CarScreen.values.forEach {
            NavigationDrawerItem(
                label = { Text(text = it.toString()) },
                selected = it == getScreen,
                onClick = {
                    setScreen(it)
                    closeDrawer()
                },
                modifier = Modifier
                    .padding(all = 10.dp)
                    .testTag(tag = it.toString())
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun DefaultPreview() {
    KotlinTestsPreviewTheme {
        DrawerContent(
            setScreen = {},
            getScreen = { StartScreen },
            closeDrawer = {}
        )
    }
}
