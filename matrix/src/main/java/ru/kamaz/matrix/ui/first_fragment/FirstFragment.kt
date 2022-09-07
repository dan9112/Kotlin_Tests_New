package ru.kamaz.matrix.ui.first_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.kamaz.matrix.R
import ru.kamaz.matrix.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        recyclerViewAdapter = RecyclerViewAdapter(resources.getStringArray(R.array.storage_method))
        return binding.run {
            with(receiver = contentView) {
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                adapter = recyclerViewAdapter
            }
            containerView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                private var set = false
                override fun onGlobalLayout() {
                    if (!set) {
                        recyclerViewAdapter.setMinWidth(containerView.width)
                        set = true
                    }
                }
            })
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(receiver = binding) {
            if (title.measuredWidth > contentView.measuredWidth) recyclerViewAdapter.setMinWidth(
                title.measuredWidth
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
