package ru.kamaz.bluetooth_connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import ru.kamaz.bluetooth_connection.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()

    private var bluetoothAdapter: BluetoothAdapter? = null

    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private val connectTriggerObserver = Observer<Boolean> {
        if (it) {
            viewModel.connectTriggerCaught()
            try {
                run search@{
                    bluetoothAdapter!!.bondedDevices.forEach { bluetoothDevice ->
                        if (bluetoothDevice.name.startsWith(prefix = "E-200")) {
                            connect(bluetoothDevice)
                            return@search
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "No Permission exception", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).also {
            it.viewModel = viewModel
            it.lifecycleOwner = this
        }

        bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

        with(receiver = viewModel) {
            connectTrigger.observe(this@MainActivity, connectTriggerObserver)
            buttonIsEnable.observe(this@MainActivity) { isEnable ->
                if (isEnable) {
                    connectedThread?.let {
                        it.cancel()
                        connectedThread = null
                    }
                    connectThread?.let {
                        it.cancel()
                        connectThread = null
                    }
                }
            }
        }
    }

    @Synchronized
    private fun connect(device: BluetoothDevice) {
        viewModel.text = "Подключается"
        connectedThread?.let {
            it.cancel()
            connectedThread = null
        }
        connectThread?.let {
            it.cancel()
            connectThread = null
        }
        connectThread = ConnectThread(device)
        connectThread!!.start()
    }

    @Synchronized
    private fun connected(socket: BluetoothSocket) {
        viewModel.text = "Подключено"
        connectedThread?.let {
            it.cancel()
            connectedThread = null
        }
        connectThread?.let {
            it.cancel()
            connectThread = null
        }
        connectedThread = ConnectedThread(socket)
        connectedThread!!.start()
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID_INSECURE)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            try {
                mmSocket?.let { socket ->
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    socket.connect()
                    connectThread = null

                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    connected(socket)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Connect Error", e)
                with(receiver = viewModel) {
                    text = "Ошибка: не удалось подключиться"
                    connectionTryFinished()
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "No Permission exception", e)
                with(receiver = viewModel) {
                    text = "Ошибка: отсутствуют разрешения"
                    connectionTryFinished()
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream = mmSocket.inputStream
        private val mmBuffer = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    with(receiver = viewModel) {
                        connectionTryFinished()
                        text = "Сигнал потерян"
                    }
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                val data = String(mmBuffer, 0, numBytes)
                Log.d(TAG, data)

                viewModel.text = data
                if (data.startsWith(prefix = "\$R:")) {
                    connectedThread!!.cancel()
                    connectedThread = null
                    viewModel.connectionTryFinished()
                    break
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    companion object {
        private val UUID_INSECURE: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        // private const val NAME_INSECURE = "SMAC1"
    }
}
