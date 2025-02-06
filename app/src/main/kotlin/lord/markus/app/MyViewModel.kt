package lord.markus.app

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

internal class MyViewModel : ViewModel() {
    // todo: add navigation or DI to clear view model
    private val client = OkHttpClient()
    private val request = Request.Builder()
        .url(URL)
        .build()
    private val listener = MyWebSocketListener(
        onMessage = {
            val message = Json.decodeFromString<Message>(it)
            _messages.add(message)
            messagesFlow.value = _messages.toList()
        }
    )
    private var socket: WebSocket? = null

    private val _messages = mutableListOf<Message>()
    private val messagesFlow = MutableStateFlow(value = _messages.toList())
    val messages = messagesFlow.asStateFlow()

    fun openWebSocket() {
        socket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: Message) {
        Log.d("Test", message.toString())
        socket?.send(message.let(Json::encodeToString))
    }

    fun closeWebSocket(reason: String) = socket
        ?.close(code = 1001, reason = reason)
        ?: false

    override fun onCleared() {
        super.onCleared()
        Log.e("VM", "Cleared!")
    }


    private companion object {
        const val URL = "ws://62.109.14.35:12000/ws/"
    }
}
