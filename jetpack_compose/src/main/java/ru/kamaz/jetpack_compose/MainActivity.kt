package ru.kamaz.jetpack_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
                for (i in 0..24) {
                    ListItem(name = "Android", description = "Operating System", num = i + 1)
                }
            }
        }
    }
}

@Composable
fun ListItem(name: String, description: String, num: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 10.dp),
        shape = RoundedCornerShape(size = 15.dp),
        elevation = 5.dp
    ) {
        Box {
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
                                .clip(shape = CircleShape)
                            val contentScale = ContentScale.Crop
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_background),
                                contentDescription = "background",
                                contentScale = contentScale,
                                modifier = modifier
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "icon",
                                contentScale = contentScale,
                                modifier = modifier
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
