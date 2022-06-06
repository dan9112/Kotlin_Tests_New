package ru.kamaz.foreground_service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kamaz.foreground_service.databinding.Fragment4Binding

class Fragment4 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<Fragment4Binding>(
        inflater, R.layout.fragment_4, container, false
    ).run {
        button42.setOnClickListener {
            findNavController().navigate(Fragment4Directions.actionFragment4ToFragment2())
        }
        button45.setOnClickListener {
            findNavController().navigate(Fragment4Directions.actionFragment4ToFragment5())
        }
        root
    }
}
