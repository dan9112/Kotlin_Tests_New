package ru.kamaz.service_bind_and_start.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ru.kamaz.service_bind_and_start.R
import ru.kamaz.service_bind_and_start.databinding.FragmentBlankBinding

/*private const val EXAMPLE_PARAM = "param"*/

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlankFragment : Fragment() {
    /*private var exampleParam: String? = null*/
    private var _binding: FragmentBlankBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            /*exampleParam = it.getString(EXAMPLE_PARAM)*/
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<FragmentBlankBinding>(
        inflater, R.layout.fragment_blank, container, false
    ).run {
        _binding = this
        root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BlankFragment.
         */
        @JvmStatic
        fun newInstance(/*exampleParam: String*/) =
            BlankFragment().apply {
                arguments = Bundle().apply {
                    /*putString(EXAMPLE_PARAM, exampleParam)*/
                }
            }
    }
}