package ru.kamaz.foreground_service

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import ru.kamaz.foreground_service.databinding.Fragment1Binding

class Fragment1 : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<Fragment1Binding>(
            inflater, R.layout.fragment_1, container, false
        )
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = Fragment1().apply {
            arguments = Bundle().apply {

            }
        }
    }
}
