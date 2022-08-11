package ru.kamaz.jetpack_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.kamaz.jetpack_compose.ui.theme.CarmineRed
import timber.log.Timber


class MainActivity : ComponentActivity() {

    private data class ListItemData(
        val title: String,
        val content: String
    )

    private val list = listOf(
        ListItemData(title = "Заголовок", content = "Бла-бла-бла..."),
        ListItemData(
            title = "Заголовок",
            content = "Очень длинный текст. Очень длинный текст. Очень длинный текст. Очень длинный " +
                    "текст. Очень длинный текст. Очень длинный текст. Очень длинный текст. Очень " +
                    "длинный текст. Очень длинный текст. Очень длинный текст. Очень длинный текст. " +
                    "Очень длинный текст. Очень длинный текст. Очень длинный текст. Очень длинный " +
                    "текст. Очень длинный текст."
        ),
        ListItemData(title = "Заголовок", content = "Па-рам-пам"),
        ListItemData(title = "Заголовок", content = "Б*!*т!!"),
        ListItemData(title = "Заголовок", content = "Бла-бла-бла..."),
        ListItemData(
            title = "Заголовок",
            content = "Не очень длинный текст. Не очень длинный текст. Не очень длинный текст. Не " +
                    "очень длинный текст. Не очень длинный текст. Не очень длинный текст. Не очень " +
                    "длинный текст. Не очень длинный текст."
        ),
        ListItemData(title = "Заголовок", content = "Бла-бла-бла..."),
        ListItemData(title = "Заголовок", content = "Бла-бла-бла..."),
        ListItemData(title = "Заголовок", content = "Разное и тому подобное."),
        ListItemData(title = "Заголовок", content = "Бла-бла-бла..."),
        ListItemData(title = "Заголовок", content = "Бла-бла-бла..."),
        ListItemData(title = "Заголовок", content = "Конец")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val backgroundRadialGradient = Brush.radialGradient(
                colorStops = arrayOf(
                    0.57f to Black,
                    0.72f to CarmineRed,
                    1f to Black,
                ),
                tileMode = TileMode.Decal
            )
            LazyColumn(
                modifier = Modifier.background(brush = backgroundRadialGradient)
            ) {
                itemsIndexed(list) { index, item ->
                    with(receiver = index + 1) {
                        ListItem(
                            title = "${item.title} $this",
                            content = item.content,
                            num = this,
                            backgroundBrush = backgroundRadialGradient
                        )
                    }
                }
            }
        }
    }
}

/** Член списка с заданными данными */
@Composable
fun ListItem(
    /** Заголовок члена списка */
    title: String,
    /** Содержание члена списка */
    content: String,
    /** Порядковый номер элемента списка */
    num: Int,
    /** Фоновый рисунок */
    backgroundBrush: Brush
) {
    var isExpanded by rememberSaveable { mutableStateOf(value = false) }
    val counter = rememberSaveable { mutableStateOf(value = 0) }
    val modifier = Modifier.padding(all = 5.dp)

    val clicker = {
        counter.value++
        Timber.i(message = "Number[$num] click - ${counter.value}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 10.dp, end = 15.dp, bottom = 10.dp)
            .clickable(onClick = clicker),
        shape = RoundedCornerShape(size = 15.dp),
        elevation = 5.dp
    ) {
        ConstraintLayout(modifier = modifier.fillMaxSize()) {
            val (image, contentView, numView) = createRefs()
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "icon",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .constrainAs(image) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                    }
                    .size(size = 64.dp)
                    .clip(shape = CircleShape)
                    .background(brush = backgroundBrush)
            )
            Text(
                modifier = Modifier
                    .padding(end = 5.dp, bottom = 5.dp)
                    .constrainAs(numView) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }, text = num.toString()
            )
            Column(modifier = Modifier
                .constrainAs(contentView) {
                    linkTo(
                        start = image.end,
                        end = numView.start,
                        top = parent.top,
                        bottom = numView.top,
                        verticalBias = 0f
                    )
                    width = Dimension.fillToConstraints
                }
                .padding(start = 15.dp), verticalArrangement = Arrangement.Top) {
                Text(text = title)
                Text(text = content, modifier = Modifier.clickable {
                    clicker()
                    isExpanded = !isExpanded
                }, maxLines = if (isExpanded) Int.MAX_VALUE else 1)
            }
        }
    }
}
