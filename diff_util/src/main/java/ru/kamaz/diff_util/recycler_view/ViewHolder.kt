package ru.kamaz.diff_util.recycler_view

import androidx.recyclerview.widget.RecyclerView
import ru.kamaz.diff_util.Item
import ru.kamaz.diff_util.databinding.RecyclerViewItemBinding

class ViewHolder(private val binding: RecyclerViewItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun setData(item: Item) = with(receiver = binding) {
        with(receiver = item) {
            itemName.text = name
            itemValue.text = value
        }
    }
}
