package ru.kamaz.compose_catalog.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsPreviewTheme
import ru.kamaz.compose_catalog.views.screens.DrawerAppScreen
import ru.kamaz.compose_catalog.views.screens.CarScreen
import ru.kamaz.compose_catalog.views.screens.StartScreen
import ru.kamaz.compose_catalog.views.screens.values

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContentComponent(
    setScreen: (DrawerAppScreen) -> Unit,
    getScreen: () -> DrawerAppScreen,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet {
        NavigationDrawerItem(
            label = { Text(text = "КамАЗ") },
            selected = false,
            onClick = {
                setScreen(StartScreen)
                closeDrawer()
            },
            modifier = Modifier
                .height(height = 120.dp)
                .padding(all = 10.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.tertiary,
                unselectedTextColor = MaterialTheme.colorScheme.onTertiary
            )
        )
        Spacer(modifier = Modifier.height(height = 12.dp))
        for (item in CarScreen.values) {
            NavigationDrawerItem(
                label = { Text(text = item.toString()) },
                selected = item == getScreen,
                onClick = {
                    setScreen(item)
                    closeDrawer()
                },
                modifier = Modifier.padding(all = 10.dp)
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun DefaultPreview() {
    KotlinTestsPreviewTheme {
        DrawerContentComponent(
            setScreen = {},
            getScreen = { StartScreen },
            closeDrawer = {}
        )
    }
}
