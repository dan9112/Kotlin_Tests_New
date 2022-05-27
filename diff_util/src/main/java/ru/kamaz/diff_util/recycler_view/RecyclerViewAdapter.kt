package ru.kamaz.diff_util.recycler_view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.kamaz.diff_util.Item
import ru.kamaz.diff_util.databinding.RecyclerViewItemBinding

class RecyclerViewAdapter(items: Collection<Item> = emptyList()) :
    RecyclerView.Adapter<ViewHolder>() {
    private val listItems: ArrayList<Item>

    fun setItems(items: Collection<Item>) {
        DiffUtil.calculateDiff(
            ItemDiffUtilCallback(
                oldCollection = listItems,
                newCollection = items
            )
        ).dispatchUpdatesTo(this)
        with(receiver = listItems) {
            clear()
            addAll(items)
        }
    }

    val items
        get() = listItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RecyclerViewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.run {
        Log.d("RecyclerViewAdapter", "bind, position = $position")
        setData(item = listItems[position])
    }

    override fun getItemCount() = listItems.size

    init {
        listItems = ArrayList(items)
    }
}
