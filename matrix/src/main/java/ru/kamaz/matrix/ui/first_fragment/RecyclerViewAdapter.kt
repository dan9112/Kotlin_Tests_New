package ru.kamaz.matrix.ui.first_fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.kamaz.matrix.databinding.ListItemBinding

class RecyclerViewAdapter(private val list: Array<String>) :
    RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {
    private var minRootWidth: Int? = null

    inner class RecyclerViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            with(receiver = binding.contentView) {
                if (minRootWidth != null) {
                    minWidth = minRootWidth!!
                    minimumWidth = minRootWidth!!
                }
                text = "${position + 1}. ${list[position]}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecyclerViewHolder(
        ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun setMinWidth(width: Int) {
        minRootWidth = width
        notifyDataSetChanged()
    }
}
