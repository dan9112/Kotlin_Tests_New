package ru.kamaz.foreground_service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kamaz.foreground_service.databinding.Fragment3Binding

class Fragment3 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<Fragment3Binding>(
        inflater, R.layout.fragment_3, container, false
    ).run {
        button34.setOnClickListener {
            findNavController().navigate(Fragment3Directions.actionFragment3ToFragment4())
        }
        button35.setOnClickListener {
            findNavController().navigate(Fragment3Directions.actionFragment3ToFragment5())
        }
        root
    }
}
