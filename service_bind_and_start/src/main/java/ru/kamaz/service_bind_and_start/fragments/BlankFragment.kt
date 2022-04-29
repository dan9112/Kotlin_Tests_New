package ru.kamaz.service_bind_and_start.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ru.kamaz.service_bind_and_start.R
import ru.kamaz.service_bind_and_start.databinding.FragmentBlankBinding
import ru.kamaz.service_bind_and_start.services.MyService

/*private const val EXAMPLE_PARAM = "param"*/

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlankFragment : Fragment() {
    /*private var exampleParam: String? = null*/
    private var _binding: FragmentBlankBinding? = null
    private lateinit var serviceConnection: ServiceConnection
    private var myService: MyService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            /*exampleParam = it.getString(EXAMPLE_PARAM)*/
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                myService = (binder as MyService.MyBinder).service
            }

            override fun onServiceDisconnected(name: ComponentName) {
                myService = null
            }
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

    override fun onStart() {
        super.onStart()
        with(requireContext()) {
            Intent(this, MyService::class.java).also { intent ->
                bindService(intent, serviceConnection, BIND_AUTO_CREATE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        requireContext().unbindService(serviceConnection)
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