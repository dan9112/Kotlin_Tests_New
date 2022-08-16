@file:OptIn(ExperimentalMaterial3Api::class)

package ru.kamaz.compose_catalog.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kamaz.compose_catalog.MainActivity
import ru.kamaz.compose_catalog.ui.theme.KotlinTestsTheme
import ru.kamaz.compose_catalog.values

@Composable
fun DrawerContentComponent(
    currentScreen: MutableState<MainActivity.DrawerAppScreen>,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet {
        NavigationDrawerItem(
            label = { Text(text = "КамАЗ") },
            selected = false,
            onClick = {
                currentScreen.value = MainActivity.DrawerAppScreen.StartScreen
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
        for (item in MainActivity.DrawerAppScreen.Product.values) {
            NavigationDrawerItem(
                label = { Text(text = item.toString()) },
                selected = item == currentScreen,
                onClick = {
                    currentScreen.value = item
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
    KotlinTestsTheme {
        DrawerContentComponent(
            currentScreen = mutableStateOf(MainActivity.DrawerAppScreen.StartScreen),
            closeDrawer = {}
        )
    }
}
