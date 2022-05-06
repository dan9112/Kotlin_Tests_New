package ru.kamaz.service_bind_and_start.presentation.ui.fragments.blank

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import ru.kamaz.service_bind_and_start.R
import ru.kamaz.service_bind_and_start.databinding.FragmentBlankBinding
import ru.kamaz.service_bind_and_start.presentation.services.my_service.MyService
import ru.kamaz.service_bind_and_start.presentation.ui.AndroidViewModelsFactory
import timber.log.Timber

class BlankFragment : Fragment() {
    private lateinit var serviceConnection: ServiceConnection
    private var myService: MyService? = null
    private var _bindingAdapter: BlankFragmentBindingAdapter? = null
    private val bindingAdapter: BlankFragmentBindingAdapter
        get() = _bindingAdapter!!

    private val viewModel by viewModels<BlankFragmentAndroidViewModel> {
        AndroidViewModelsFactory(requireActivity().application)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                myService = (binder as MyService.MyBinder).service.apply {
                    _bindingAdapter?.let { it.service = this }
                    result.observe(viewLifecycleOwner, serviceResultObserver)
                    isInProcess.observe(viewLifecycleOwner) { disable ->
                        _bindingAdapter?.isCreateNewButtonEnable?.set(!disable)
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                myService = null

                Timber.e(message = "Service connection lost")
            }
        }
    }

    private val serviceResultObserver = Observer { result: Boolean? ->
        if (result != null) {
            myService!!.stopSelf()
            viewModel.saveResult(result = result)
            Timber.d(message = "Result has been received: $result")
            _bindingAdapter?.updateTvAndDrButton?.invoke(result)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<FragmentBlankBinding>(
        inflater, R.layout.fragment_blank, container, false
    ).run {
        bindingAdapter =
            BlankFragmentBindingAdapter(context = requireContext(), viewModel = viewModel).apply {
                _bindingAdapter = this
                updateTvAndDrButton.invoke(viewModel.getResult())
            }
        root
    }

    override fun onStart() {
        super.onStart()
        with(receiver = requireContext()) {
            Intent(this, MyService::class.java).also { intent ->
                bindService(intent, serviceConnection, BIND_AUTO_CREATE)
                Timber.d(message = "Tries to bind service")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        with(receiver = bindingAdapter) {
            isCreateNewButtonEnable.set(false)
            service = null
        }
        myService = null
        requireContext().unbindService(serviceConnection)
        Timber.d(message = "Tries to unbind service")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingAdapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (requireActivity().isFinishing) with(receiver = requireContext()) {
            stopService(Intent(this, MyService::class.java))// ???
        }
    }

    companion object {

        // @JvmStatic
        // fun newInstance() = BlankFragment()
    }
}
