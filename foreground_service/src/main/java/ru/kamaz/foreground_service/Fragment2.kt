package ru.kamaz.foreground_service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kamaz.foreground_service.MyService.Companion.command
import ru.kamaz.foreground_service.databinding.Fragment2Binding

class Fragment2 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<Fragment2Binding>(
        inflater, R.layout.fragment_2, container, false
    ).run {
        button25.setOnClickListener {
            requireContext().stopService()
            findNavController().navigate(Fragment2Directions.actionFragment2ToFragment5())
        }
        startService.setOnClickListener {
            with(receiver = requireContext()) {
                val destinationsList = ArrayList<DestinationInfo>()
                findNavController().backQueue.forEach {
                    destinationsList.add(
                        DestinationInfo(
                            destId = it.destination.id, destArgs = it.arguments
                        )
                    )
                }
                startForegroundService(Intent(this, MyService::class.java).apply {
                    putExtra(command, Command.Start(destinationsList))
                })
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        root
    }

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            with(receiver = requireActivity()) {
                stopService()
                with(receiver = findNavController()) {
                    if (backQueue[backQueue.size - 2].destination.id == graph.startDestinationId) finish()// проверка на первый фрагмент, не считая начального, в стеке
                    else navigateUp()
                }
            }
        }
    }

    private fun Context.stopService() {
        startService(Intent(this, MyService::class.java).apply {
            putExtra(command, Command.Stop())
        })
    }
}
