package com.example.alarm_manager

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.alarm_manager.main.MainActivity

class PermissionDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage("Предоставить приложению разрешение использовать точные интерфейсы сигнализации?")
            .setCancelable(true)
            .setPositiveButton("Да") { _, _ ->
                (activity as MainActivity).dialogPositiveChoice
            }
            .setNegativeButton("Нет") { _, _ ->
                Toast.makeText(
                    activity,
                    "Сигнализация будет срабатывать с неточным временем запуска",
                    Toast.LENGTH_LONG
                ).show()
                (activity as MainActivity).dialogNegativeChoice
            }
            .create()
}