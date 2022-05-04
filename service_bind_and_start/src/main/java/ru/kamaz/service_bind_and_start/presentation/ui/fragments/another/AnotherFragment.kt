package ru.kamaz.service_bind_and_start.presentation.ui.fragments.another

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kamaz.service_bind_and_start.R
import ru.kamaz.service_bind_and_start.databinding.FragmentAnotherBinding

class AnotherFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<FragmentAnotherBinding>(
        inflater,
        R.layout.fragment_another,
        container,
        false
    ).run {
        buttonToBlank.setOnClickListener {
            findNavController().navigate(AnotherFragmentDirections.actionAnotherFragmentToBlankFragment())
        }
        root
    }

    companion object {
        // @JvmStatic
        // fun newInstance() = AnotherFragment()
    }
}
