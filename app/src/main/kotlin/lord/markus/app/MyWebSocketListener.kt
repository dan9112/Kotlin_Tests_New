package lord.markus.app

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

internal class MyWebSocketListener(
    val onOpen: () -> Unit = {},
    val onMessage: (String) -> Unit,
    val onClosing: (code: Int, reason: String) -> Unit = { _, _ -> },
    val onClosed: (code: Int, reason: String) -> Unit = { _, _ -> },
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

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.e(TAG, "Closed : $code / $reason")
        super.onClosed(webSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        t.printStackTrace()
        Log.e(TAG, "Error : ${t.message}\t${response} - $t")
        onFailure(t)
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000

        private const val TAG = "SocketListener"
    }
}
