package lord.kotlin.file_scanner

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import lord.kotlin.file_scanner.UserFeedbackDialogFragment.DialogModes.*
import java.io.Serializable

class UserFeedbackDialogFragment : DialogFragment() {
    private lateinit var agreePermission: OnAgreePermission

    internal interface OnAgreePermission {
        /** Функция положительной реакции на выбор пользователя в диалоговом окне */
        fun onAgreePermission(mode: DialogModes?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAgreePermission) agreePermission = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity()).apply {
            val dialogMode =
                requireArguments().getSerializable(getString(R.string.dialog_fragment_mode)) as DialogModes
            setTitle(getString(R.string.dialog_fragment_title))
            setMessage(
                when (dialogMode) {
                    Read -> getString(R.string.dialog_fragment_message_read)
                    ReadToSettings -> getString(R.string.dialog_fragment_message_read_to_settings)
                    Manage -> getString(R.string.dialog_fragment_message_manage)
                }
            )
            setPositiveButton(getString(R.string.dialog_fragment_positive_button_text)) { dialog: DialogInterface, _: Int ->
                agreePermission.onAgreePermission(dialogMode)
                dialog.dismiss()
            }
            setNegativeButton(getString(R.string.dialog_fragment_negative_button_text)) { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            }
        }.create().apply {
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        agreePermission.onAgreePermission(null)
    }

    enum class DialogModes : Serializable {
        Read,
        ReadToSettings,
        Manage
    }
}
