package ru.kamaz.foreground_service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kamaz.foreground_service.databinding.Fragment1Binding

class Fragment1 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<Fragment1Binding>(
        inflater, R.layout.fragment_1, container, false
    ).run {
        button12.setOnClickListener {
            findNavController().navigate(Fragment1Directions.actionFragment1ToFragment2())
        }
        button13.setOnClickListener {
            findNavController().navigate(
                Fragment1Directions.actionFragment1ToFragment3(
                    argumentFloat = 13f,
                    argumentString = "anyString"
                )
            )
        }
        root
    }
}
