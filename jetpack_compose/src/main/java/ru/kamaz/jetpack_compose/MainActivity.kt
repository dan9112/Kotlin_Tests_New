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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import timber.log.Timber

/**
 * Фоновый рисунок, заданный как радиальный градиент
 */
private val backgroundRadialGradient = Brush.radialGradient(
    colorStops = arrayOf(
        0.57f to Black,
        0.72f to Color(color = 0xFF971212),
        1f to Black,
    ),
    tileMode = TileMode.Decal
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LazyColumn(
                modifier = Modifier.background(brush = backgroundRadialGradient)
            ) {
                items(count = 25) {
                    ListItem(
                        name = "Android",
                        description = "Operating System",
                        num = it + 1
                    )
                }
            }
        }
    }
}

/** Член списка с заданными данными */
@Composable
fun ListItem(
    /** Наименование члена списка */
    name: String,
    /** Описание к члену списка */
    description: String,
    /** Порядковый номер элемента списка */
    num: Int
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
        Box(modifier = Modifier.padding(all = 5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            val modifier = Modifier
                                .padding(all = 5.dp)
                                .size(size = 64.dp)
                            Box(modifier = modifier.background(brush = backgroundRadialGradient))
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "icon",
                                contentScale = ContentScale.Crop,
                                modifier = modifier
                                    .clip(shape = CircleShape)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(text = name)
                            Text(text = description)
                        }
                    }
                }
                Column(modifier = Modifier.padding(all = 5.dp)) {
                    Text(text = num.toString())
                }
            }
        }
    }
}
