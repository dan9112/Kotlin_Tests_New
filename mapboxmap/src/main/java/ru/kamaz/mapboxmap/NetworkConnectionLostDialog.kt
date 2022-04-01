package ru.kamaz.mapboxmap

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.io.Serializable

private const val ARG_PARAM1 = "param1"

class NetworkConnectionLostDialog : DialogFragment() {
    private lateinit var onDialogCloseCallbackCallback: OnDialogCloseCallback

    interface OnDialogCloseCallback : Serializable {
        val closed: (Boolean) -> Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            onDialogCloseCallbackCallback = it.getSerializable(ARG_PARAM1) as OnDialogCloseCallback
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        AlertDialog.Builder(requireContext()).run {
            setMessage("Соединение с сетью интернет утеряно. Карта не может работать без доступа к сети интернет. Подключитесь к интернету для работы с картой")
            setPositiveButton("Подключено") { _, _ ->
                onDialogCloseCallbackCallback.closed(true)
                dismiss()
            }
            setNegativeButton("Закрыть карту") { dialog, _ -> dialog.cancel() }
            create()
        }

    override fun onCancel(dialog: DialogInterface) {
        onDialogCloseCallbackCallback.closed(false)
        super.onCancel(dialog)
    }

    companion object {
        @JvmStatic
        fun newInstance(onDialogCloseCallbackCallback: (connected: Boolean) -> Unit) =
            NetworkConnectionLostDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, object : OnDialogCloseCallback {
                        override val closed = onDialogCloseCallbackCallback
                    })
                }
            }
    }
}
