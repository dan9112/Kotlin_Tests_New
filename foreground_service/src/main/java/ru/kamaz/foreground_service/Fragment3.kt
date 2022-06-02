package ru.kamaz.foreground_service

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import ru.kamaz.foreground_service.databinding.Fragment3Binding

class Fragment3 : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<Fragment3Binding>(
            inflater, R.layout.fragment_3, container, false
        )
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = Fragment3().apply {
            arguments = Bundle().apply {

            }
        }
    }
}
