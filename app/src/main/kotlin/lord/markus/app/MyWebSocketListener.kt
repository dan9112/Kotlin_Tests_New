package lord.markus.app

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

internal class MyWebSocketListener(
    val onOpen: () -> Unit = {},
    val onMessage: (String) -> Unit,
    val onClosing: (Int, String) -> Unit = { _, _ -> },
    val onFailure: (Throwable) -> Unit = { }
) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        onOpen()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "Receiving : $text")
        onMessage(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG, "Receiving bytes : " + bytes.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        Log.d(TAG, "Closing : $code / $reason")
        onClosing(code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "Error : " + t.message)
        onFailure(t)
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000

        private const val TAG = "SocketListener"
    }
}
