package ru.kamaz.foreground_service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kamaz.foreground_service.databinding.Fragment5Binding

class Fragment5 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<Fragment5Binding>(
        inflater, R.layout.fragment_5, container, false
    ).run {
        button52.setOnClickListener {
            findNavController().navigate(Fragment5Directions.actionFragment5ToFragment2())
        }
        root
    }
}
