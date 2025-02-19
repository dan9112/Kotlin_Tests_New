package lord.markus.app

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

internal class WebSocketService : Service() {
    private val binder = WebSocketServiceBinder()

    private val client = OkHttpClient()

    private val request = Request.Builder()
        .url(URL)
        .build()

    private val listener = MyWebSocketListener(
        onOpen = {
            _errorMessage.value?.run {
                _errorMessage.value = null
            }
        },
        onMessage = {
            val message = Json.decodeFromString<Message>(it)
            _messages.add(message)
            messagesFlow.value = _messages.toList()
        },
        onFailure = {
            _errorMessage.value = it.message
        }
    )
    private var socket: WebSocket? = null

    private val _messages = mutableListOf<Message>()
    private val messagesFlow = MutableStateFlow(value = _messages.toList())
    val messages = messagesFlow.asStateFlow()


    private val _errorMessage = MutableStateFlow<String?>(value = null)
    val errorMessage: StateFlow<String?>
        get() = _errorMessage

    private fun openWebSocket() {
        socket = client.newWebSocket(request, listener)
    }

    fun tryReopenWebSocket() {
        _errorMessage.value = "Connecting..."
        socket?.cancel()
        openWebSocket()
    }

    fun sendMessage(message: Message) {
        Log.d("Test", message.toString())
        socket?.send(message.message.let(Json::encodeToString))
    }

    override fun onBind(intent: Intent) = binder

    override fun onCreate() {
        super.onCreate()

        openWebSocket()
    }

    override fun onDestroy() {
        super.onDestroy()

        socket?.close(code = 1000, reason = null)
    }

    inner class WebSocketServiceBinder : Binder() {
        val service
            get() = this@WebSocketService
    }

    private companion object {
        const val URL = "ws://62.109.14.35:12000/ws/"
    }
}
