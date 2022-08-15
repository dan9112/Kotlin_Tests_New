@file:OptIn(ExperimentalMaterial3Api::class)

package ru.kamaz.compose_catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // KotlinTestsTheme {
            DrawerAppComponent()
            // }
        }
    }
}

@Composable
fun DrawerAppComponent() {
    // Состояние выдвигаемого ящика - перенести во viewModel
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // Текущий активный экран - перенести во viewModel
    val currentScreen =
        rememberSaveable { mutableStateOf<DrawerAppScreen>(DrawerAppScreen.StartScreen) }
    // Область действия составного объекта
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            DrawerContentComponent(
                currentScreen = currentScreen,
                closeDrawer = { coroutineScope.launch { drawerState.close() } })
        },
        content = {
            BodyContentComponent(
                currentScreen = currentScreen.value,
                openDrawer = { coroutineScope.launch { drawerState.open() } }
            )
        })
}

sealed interface DrawerAppScreen : Serializable {
    object StartScreen : DrawerAppScreen {
        override fun toString() = "Start Screen"
    }

    sealed interface Product : DrawerAppScreen {
        object KamAZ4310Screen : Product {
            override fun toString() = "KamAZ-4310"
        }

        object KamAZ5511Screen : Product {
            override fun toString() = "KamAZ-5511"
        }

        object KamAZ6282Screen : Product {
            override fun toString() = "KamAZ-6282"
        }

        object KamAZ6350Screen : Product {
            override fun toString() = "KamAZ-6350"
        }
    }
}

@Composable
fun DrawerContentComponent(
    currentScreen: MutableState<DrawerAppScreen>,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet {
        NavigationDrawerItem(
            label = { Text(text = "Home") },
            selected = false,
            onClick = {
                currentScreen.value = DrawerAppScreen.StartScreen
                closeDrawer()
            },
            modifier = Modifier
                .height(height = 120.dp)
                .padding(all = 10.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color(color = 0xFFffd7d7)
            )
        )
        Spacer(modifier = Modifier.height(height = 12.dp))
        for (item in DrawerAppScreen.Product::class.sealedSubclasses) {
            val screen = item.objectInstance!!
            NavigationDrawerItem(
                label = { Text(text = screen.toString()) },
                selected = item == currentScreen,
                onClick = {
                    currentScreen.value = screen
                    closeDrawer()
                },
                modifier = Modifier.padding(all = 10.dp)
            )
        }
    }
}

/**
 * Передаёт соответствующий экран, компонуемый на основе текущего активного экрана.
 */
@Composable
fun BodyContentComponent(
    currentScreen: DrawerAppScreen,
    openDrawer: () -> Unit
) = when (currentScreen) {
    DrawerAppScreen.StartScreen -> StartScreen(openDrawer)
    DrawerAppScreen.Product.KamAZ4310Screen -> KamAZ4310Screen(openDrawer)
    DrawerAppScreen.Product.KamAZ5511Screen -> KamAZ5511Screen(openDrawer)
    DrawerAppScreen.Product.KamAZ6282Screen -> KamAZ6282Screen(openDrawer)
    DrawerAppScreen.Product.KamAZ6350Screen -> KamAZ6350Screen(openDrawer)
}

// Мы представляем функцию composable, аннотируя ее аннотацией @Composable. Составные функции можно вызывать
// только из области действия других composable функций. Мы должны думать о composable функциях как о
// блоках лего — каждая composable функция, в свою очередь, состоит из более мелких composable функций.
@Composable
fun StartScreen(openDrawer: () -> Unit) {
// Столбец — это оставной объект, который размещает свои дочерние элементы в вертикальной последовательности.
    // Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar — это предопределенный composable элемент, который размещается в верхней части экрана.
        // В нем есть слоты для заголовка, значка навигации и действий. Также известна как панель действий.
        SmallTopAppBar(
            // Составляемый объект Text предопределен библиотекой Compose UI; вы можете использовать
            // это составное для отображения текста на экране
            title = { Text("${DrawerAppScreen.StartScreen} Title") },
            navigationIcon = {
                IconButton(onClick = openDrawer) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        )
        // Surface — это компонуемый объект, предназначенный для удовлетворения потребностей метафоры
        // «Surface» из спецификации Material Design. Обычно он используется для изменения цвета фона,
        // добавления высоты, обрезки или добавления формы фона к его дочерним составным элементам. Так
        // как мы хотим, чтобы поверхность занимала всю доступную ширину после того, как TopAppBar был
        // размещен вверху, мы используем модификатор Modifier.weight и передаем вес как 1 (который
        // представляет весь доступный вес).

        // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
        // для изменения составного объекта, к которому он применяется.
        Surface(color = Color(0xFFffd7d7.toInt()), modifier = Modifier.weight(1f)) {
            // Столбец — это компонуемый объект, который размещает свои дочерние элементы в вертикальной
            // последовательности. Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
            // Кроме того, мы также передаем ему несколько модификаторов.

            // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
            // для изменения составного объекта, к которому он применяется. В приведенном ниже примере
            // мы настраиваем столбец так, чтобы он занимал всю доступную высоту и ширину, используя
            // Modifier.fillMaxSize().
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = { Text(text = DrawerAppScreen.StartScreen.toString()) }
            )
        }
    }
}

// Мы представляем функцию composable, аннотируя ее аннотацией @Composable. Составные функции можно вызывать
// только из области действия других composable функций. Мы должны думать о composable функциях как о
// блоках лего — каждая composable функция, в свою очередь, состоит из более мелких composable функций.
@Composable
fun KamAZ4310Screen(openDrawer: () -> Unit) {
// Столбец — это оставной объект, который размещает свои дочерние элементы в вертикальной последовательности.
    // Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar — это предопределенный composable элемент, который размещается в верхней части экрана.
        // В нем есть слоты для заголовка, значка навигации и действий. Также известна как панель действий.
        SmallTopAppBar(
            // Составляемый объект Text предопределен библиотекой Compose UI; вы можете использовать
            // это составное для отображения текста на экране
            title = { Text("${DrawerAppScreen.Product.KamAZ4310Screen} Title") },
            navigationIcon = {
                IconButton(onClick = openDrawer) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        )
        // Surface — это компонуемый объект, предназначенный для удовлетворения потребностей метафоры
        // «Surface» из спецификации Material Design. Обычно он используется для изменения цвета фона,
        // добавления высоты, обрезки или добавления формы фона к его дочерним составным элементам. Так
        // как мы хотим, чтобы поверхность занимала всю доступную ширину после того, как TopAppBar был
        // размещен вверху, мы используем модификатор Modifier.weight и передаем вес как 1 (который
        // представляет весь доступный вес).

        // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
        // для изменения составного объекта, к которому он применяется.
        Surface(color = Color(0xFFffd7d7.toInt()), modifier = Modifier.weight(1f)) {
            // Столбец — это компонуемый объект, который размещает свои дочерние элементы в вертикальной
            // последовательности. Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
            // Кроме того, мы также передаем ему несколько модификаторов.

            // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
            // для изменения составного объекта, к которому он применяется. В приведенном ниже примере
            // мы настраиваем столбец так, чтобы он занимал всю доступную высоту и ширину, используя
            // Modifier.fillMaxSize().
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = { Text(text = DrawerAppScreen.Product.KamAZ4310Screen.toString()) }
            )
        }
    }

}

// Мы представляем функцию composable, аннотируя ее аннотацией @Composable. Составные функции можно вызывать
// только из области действия других composable функций. Мы должны думать о composable функциях как о
// блоках лего — каждая composable функция, в свою очередь, состоит из более мелких composable функций.
@Composable
fun KamAZ5511Screen(openDrawer: () -> Unit) {
// Столбец — это оставной объект, который размещает свои дочерние элементы в вертикальной последовательности.
    // Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar — это предопределенный composable элемент, который размещается в верхней части экрана.
        // В нем есть слоты для заголовка, значка навигации и действий. Также известна как панель действий.
        SmallTopAppBar(
            // Составляемый объект Text предопределен библиотекой Compose UI; вы можете использовать
            // это составное для отображения текста на экране
            title = { Text("${DrawerAppScreen.Product.KamAZ5511Screen} Title") },
            navigationIcon = {
                IconButton(onClick = openDrawer) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        )
        // Surface — это компонуемый объект, предназначенный для удовлетворения потребностей метафоры
        // «Surface» из спецификации Material Design. Обычно он используется для изменения цвета фона,
        // добавления высоты, обрезки или добавления формы фона к его дочерним составным элементам. Так
        // как мы хотим, чтобы поверхность занимала всю доступную ширину после того, как TopAppBar был
        // размещен вверху, мы используем модификатор Modifier.weight и передаем вес как 1 (который
        // представляет весь доступный вес).

        // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
        // для изменения составного объекта, к которому он применяется.
        Surface(color = Color(0xFFffd7d7.toInt()), modifier = Modifier.weight(1f)) {
            // Столбец — это компонуемый объект, который размещает свои дочерние элементы в вертикальной
            // последовательности. Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
            // Кроме того, мы также передаем ему несколько модификаторов.

            // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
            // для изменения составного объекта, к которому он применяется. В приведенном ниже примере
            // мы настраиваем столбец так, чтобы он занимал всю доступную высоту и ширину, используя
            // Modifier.fillMaxSize().
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = { Text(text = DrawerAppScreen.Product.KamAZ5511Screen.toString()) }
            )
        }
    }

}

// Мы представляем функцию composable, аннотируя ее аннотацией @Composable. Составные функции можно вызывать
// только из области действия других composable функций. Мы должны думать о composable функциях как о
// блоках лего — каждая composable функция, в свою очередь, состоит из более мелких composable функций.
@Composable
fun KamAZ6282Screen(openDrawer: () -> Unit) {
// Столбец — это оставной объект, который размещает свои дочерние элементы в вертикальной последовательности.
    // Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar — это предопределенный composable элемент, который размещается в верхней части экрана.
        // В нем есть слоты для заголовка, значка навигации и действий. Также известна как панель действий.
        SmallTopAppBar(
            // Составляемый объект Text предопределен библиотекой Compose UI; вы можете использовать
            // это составное для отображения текста на экране
            title = { Text("${DrawerAppScreen.Product.KamAZ6282Screen} Title") },
            navigationIcon = {
                IconButton(onClick = openDrawer) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        )
        // Surface — это компонуемый объект, предназначенный для удовлетворения потребностей метафоры
        // «Surface» из спецификации Material Design. Обычно он используется для изменения цвета фона,
        // добавления высоты, обрезки или добавления формы фона к его дочерним составным элементам. Так
        // как мы хотим, чтобы поверхность занимала всю доступную ширину после того, как TopAppBar был
        // размещен вверху, мы используем модификатор Modifier.weight и передаем вес как 1 (который
        // представляет весь доступный вес).

        // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
        // для изменения составного объекта, к которому он применяется.
        Surface(color = Color(0xFFffd7d7.toInt()), modifier = Modifier.weight(1f)) {
            // Столбец — это компонуемый объект, который размещает свои дочерние элементы в вертикальной
            // последовательности. Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
            // Кроме того, мы также передаем ему несколько модификаторов.

            // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
            // для изменения составного объекта, к которому он применяется. В приведенном ниже примере
            // мы настраиваем столбец так, чтобы он занимал всю доступную высоту и ширину, используя
            // Modifier.fillMaxSize().
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = { Text(text = DrawerAppScreen.Product.KamAZ6282Screen.toString()) }
            )
        }
    }

}

// Мы представляем функцию composable, аннотируя ее аннотацией @Composable. Составные функции можно вызывать
// только из области действия других composable функций. Мы должны думать о composable функциях как о
// блоках лего — каждая composable функция, в свою очередь, состоит из более мелких composable функций.
@Composable
fun KamAZ6350Screen(openDrawer: () -> Unit) {
// Столбец — это оставной объект, который размещает свои дочерние элементы в вертикальной последовательности.
    // Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar — это предопределенный composable элемент, который размещается в верхней части экрана.
        // В нем есть слоты для заголовка, значка навигации и действий. Также известна как панель действий.
        SmallTopAppBar(
            // Составляемый объект Text предопределен библиотекой Compose UI; вы можете использовать
            // это составное для отображения текста на экране
            title = { Text("${DrawerAppScreen.Product.KamAZ6350Screen} Title") },
            navigationIcon = {
                IconButton(onClick = openDrawer) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        )
        // Surface — это компонуемый объект, предназначенный для удовлетворения потребностей метафоры
        // «Surface» из спецификации Material Design. Обычно он используется для изменения цвета фона,
        // добавления высоты, обрезки или добавления формы фона к его дочерним составным элементам. Так
        // как мы хотим, чтобы поверхность занимала всю доступную ширину после того, как TopAppBar был
        // размещен вверху, мы используем модификатор Modifier.weight и передаем вес как 1 (который
        // представляет весь доступный вес).

        // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
        // для изменения составного объекта, к которому он применяется.
        Surface(color = Color(0xFFffd7d7.toInt()), modifier = Modifier.weight(1f)) {
            // Столбец — это компонуемый объект, который размещает свои дочерние элементы в вертикальной
            // последовательности. Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
            // Кроме того, мы также передаем ему несколько модификаторов.

            // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
            // для изменения составного объекта, к которому он применяется. В приведенном ниже примере
            // мы настраиваем столбец так, чтобы он занимал всю доступную высоту и ширину, используя
            // Modifier.fillMaxSize().
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = { Text(text = DrawerAppScreen.Product.KamAZ6350Screen.toString()) }
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // KotlinTestsTheme {
    DrawerAppComponent()
    // }
}