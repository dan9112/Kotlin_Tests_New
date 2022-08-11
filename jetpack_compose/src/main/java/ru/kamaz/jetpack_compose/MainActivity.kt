package ru.kamaz.jetpack_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.kamaz.jetpack_compose.ui.theme.CarmineRed
import timber.log.Timber


class MainActivity : ComponentActivity() {

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
                items(count = 25) {
                    ListItem(
                        title = "Android",
                        content = "Operating System",
                        num = it + 1,
                        backgroundBrush = backgroundRadialGradient
                    )
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
    val counter = rememberSaveable { mutableStateOf(value = 0) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
            .clickable {
                counter.value++
                Timber.i(message = "Number[$num] click - ${counter.value}")
            },
        shape = RoundedCornerShape(size = 15.dp),
        elevation = 5.dp
    ) {
        val modifier = Modifier.padding(all = 5.dp)
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "icon",
                            contentScale = ContentScale.Crop,
                            modifier = modifier
                                .size(size = 64.dp)
                                .clip(shape = CircleShape)
                                .background(brush = backgroundBrush)
                        )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(text = title)
                        Text(text = content)
                    }
                }
            }
            Column(modifier = modifier) {
                Text(text = num.toString())
            }
        }
    }
}
