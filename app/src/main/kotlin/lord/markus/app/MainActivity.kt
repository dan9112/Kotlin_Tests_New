package lord.markus.app

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.parcelize.Parcelize
import lord.markus.app.ui.theme.KotlinTestsTheme
import java.lang.System.currentTimeMillis

private const val DATE_TIME_FORMAT = "yyyy:MM:dd HH:mm"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            KotlinTestsTheme {
                // A surface container using the 'background' color from the theme
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Background image",
                        modifier = Modifier.fillMaxSize(),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.None
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        ChatInterface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Parcelize
private data class Message(
    val id: Long,
    val producer: Long,
    val time: Long,
    val message: String,
    val state: MessageState = MessageState.Default
) : Parcelable

sealed interface MessageState : Parcelable {
    @Parcelize
    data object Default : MessageState

    @Parcelize
    data object Sending : MessageState

    @Parcelize
    data object Sent : MessageState

    /*@Parcelize
    data object Received : MessageState

    @Parcelize
    data object Seen : MessageState*/

    sealed interface Error : MessageState {
        @Parcelize
        data object NoInternet : Error
    }
}

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun ChatInterface(
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val myId by rememberSaveable { mutableLongStateOf(value = -1L) }

    val messages = rememberSaveable(
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) { mutableStateListOf<Message>() }

    val actualId by remember {
        derivedStateOf {
            messages.findLast { it.producer == myId }?.id?.inc() ?: 0
        }
    }

    DisposableEffect(Unit) {


        onDispose {

        }
    }

    Column(modifier = modifier) {
        val state = rememberLazyListState()
        LaunchedEffect(messages.size) {
            coroutineScope.launch {
                if (messages.isNotEmpty()) state.animateScrollToItem(index = messages.lastIndex)
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = state,
            contentPadding = PaddingValues(all = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)

        ) {
            items(items = messages, key = { it.id to it.producer }) { item ->
                if (item.producer == myId) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Column(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(size = 8.dp)
                                )
                                .padding(all = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = item.message,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                Text(
                                    text = Instant
                                        .fromEpochMilliseconds(item.time)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .format(
                                            LocalDateTime.Format {
                                                byUnicodePattern(DATE_TIME_FORMAT)
                                            }
                                        ),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(size = 8.dp)
                                )
                                .padding(all = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.message,
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(
                                    8.dp,
                                    Alignment.Start
                                )
                            ) {
                                Text(
                                    text = Instant
                                        .fromEpochMilliseconds(item.time)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .format(
                                            LocalDateTime.Format {
                                                byUnicodePattern(DATE_TIME_FORMAT)
                                            }
                                        ),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        var currentMessage by rememberSaveable { mutableStateOf(value = "") }

        val onSend by rememberUpdatedState {
            messages.add(
                Message(
                    id = actualId,
                    producer = myId,
                    time = currentTimeMillis(),
                    message = currentMessage
                )
            )
            currentMessage = ""
        }

        TextField(
            value = currentMessage,
            onValueChange = { currentMessage = it },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = Color.Magenta),
            textStyle = MaterialTheme.typography.titleMedium,
            placeholder = {
                Text(
                    text = "Message",
                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            },
            trailingIcon = {
                IconButton(onClick = onSend) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send button icon"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = true,
                imeAction = ImeAction.Send,
                showKeyboardOnFocus = true
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            singleLine = true,
            colors = MaterialTheme.colorScheme.run {
                TextFieldDefaults.colors(
                    focusedTextColor = onPrimaryContainer,
                    unfocusedTextColor = onPrimaryContainer,
                    disabledTextColor = onPrimaryContainer.copy(alpha = 0.75f),
                    focusedContainerColor = primaryContainer,
                    unfocusedContainerColor = primaryContainer,
                    disabledContainerColor = primaryContainer.copy(alpha = 0.75f),
                    focusedIndicatorColor = onPrimary,
                    unfocusedIndicatorColor = onPrimary,
                    disabledIndicatorColor = onPrimary.copy(alpha = 0.75f),
                    focusedTrailingIconColor = primary,
                    unfocusedTrailingIconColor = primary,
                    disabledTrailingIconColor = primary.copy(alpha = 0.75f),
                    cursorColor = primary
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KotlinTestsTheme {
        Greeting("Android")
    }
}
