package ru.kamaz.compose_catalog.views.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.livefront.sealedenum.GenSealedEnum

sealed class DrawerAppScreen {

    // Мы представляем функцию composable, аннотируя ее аннотацией @Composable. Составные функции можно вызывать
    // только из области действия других composable функций. Мы должны думать о composable функциях как о
    // блоках лего — каждая composable функция, в свою очередь, состоит из более мелких composable функций.
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GetView(openDrawer: () -> Unit) {
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = {
                // TopAppBar — это предопределенный composable элемент, который размещается в верхней части экрана.
                // В нем есть слоты для заголовка, значка навигации и действий. Также известна как панель действий.
                SmallTopAppBar(
                    // Составляемый объект Text предопределен библиотекой Compose UI; вы можете использовать
                    // это составное для отображения текста на экране
                    title = {
                        Text(
                            text = when (this@DrawerAppScreen) {
                                is CarScreen -> this@DrawerAppScreen.toString()
                                else -> "Приветственная страница"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = openDrawer) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }) {
            // Surface — это компонуемый объект, предназначенный для удовлетворения потребностей метафоры
            // «Surface» из спецификации Material Design. Обычно он используется для изменения цвета фона,
            // добавления высоты, обрезки или добавления формы фона к его дочерним составным элементам. Так
            // как мы хотим, чтобы поверхность занимала всю доступную ширину после того, как TopAppBar был
            // размещен вверху, мы используем модификатор Modifier.weight и передаем вес как 1 (который
            // представляет весь доступный вес).

            // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
            // для изменения составного объекта, к которому он применяется.
            Surface(
                color = MaterialTheme.colorScheme.tertiary, modifier = Modifier
                    .padding(top = it.calculateTopPadding())
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
            ) {
                // Столбец — это компонуемый объект, который размещает свои дочерние элементы в вертикальной
                // последовательности. Вы можете думать об этом как о LinearLayout с вертикальной ориентацией.
                // Кроме того, мы также передаем ему несколько модификаторов.

                // Вы можете думать о модификаторах как о реализации шаблона декораторов, которые используются
                // для изменения составного объекта, к которому он применяется. В приведенном ниже примере
                // мы настраиваем столбец так, чтобы он занимал всю доступную высоту и ширину, используя
                // Modifier.fillMaxSize().
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxSize(),
                    verticalArrangement = when (this@DrawerAppScreen) {
                        is CarScreen -> Arrangement.Top
                        else -> Arrangement.Center
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val screen = this@DrawerAppScreen) {
                        is CarScreen -> {
                            Image(
                                painter = painterResource(id = screen.imageResource),
                                contentDescription = "Image",
                                modifier = Modifier.padding(all = 20.dp)
                            )
                            Text(
                                text = stringResource(id = screen.contentResource),
                                modifier = Modifier.testTag(tag = "Content text"),
                                textAlign = TextAlign.Justify
                            )
                        }
                        else -> {
                            Text(
                                modifier = Modifier.testTag(tag = "Main text"),
                                text = "Добро пожаловать в альфа-версию приложения для демонстрации " +
                                        "возможностей Material Design 3 в связке с:",
                                textAlign = TextAlign.Center
                            )
                            Text(
                                modifier = Modifier.testTag(tag = "Numbered list"),
                                text = "1. Jetpack Compose;\n2. Kotlin Coroutines;\n3. Kotlin Flow;\n4. " +
                                        "View Model Compose;\n5*. Koin.",
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }

    sealed class CarScreen(
        @VisibleForTesting
        @DrawableRes
        val imageResource: Int,
        @VisibleForTesting
        @StringRes
        val contentResource: Int
    ) : DrawerAppScreen() {

        @GenSealedEnum
        companion object
    }
}
