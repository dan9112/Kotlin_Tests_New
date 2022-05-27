package ru.kamaz.diff_util.recycler_view

import androidx.recyclerview.widget.DiffUtil
import ru.kamaz.diff_util.Item

class ItemDiffUtilCallback(private val oldCollection: Collection<Item>, private val newCollection: Collection<Item>) : DiffUtil.Callback() {

    override fun getOldListSize() = oldCollection.size

    override fun getNewListSize() = newCollection.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldCollection.elementAt(oldItemPosition).id == newCollection.elementAt(newItemPosition).id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldCollection.elementAt(oldItemPosition).name == newCollection.elementAt(newItemPosition).name &&
                oldCollection.elementAt(oldItemPosition).value == newCollection.elementAt(newItemPosition).value
}
