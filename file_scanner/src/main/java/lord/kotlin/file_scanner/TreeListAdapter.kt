package lord.kotlin.file_scanner

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import lord.kotlin.file_scanner.databinding.ListItemBinding
import kotlin.collections.ArrayList

@SuppressLint("NotifyDataSetChanged")
class TreeListAdapter(
    /** Контекст RecyclerView */
    private val context: Context,
    /** Данные входящего списка */
    private val treeItems: ArrayList<TreeItem>
) : RecyclerView.Adapter<TreeListAdapter.TreeViewHolder>() {
    /** Флаг первой загрузки, чтобы было удобно добавлять данные в список отображения */
    private var begin: Boolean

    /** Отображаемые данные списка */
    private val itemList: MutableList<TreeItem>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        return TreeViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
        holder.apply {
            // В методе getList все данные, которые необходимо отобразить, добавляются в itemList, поэтому вы можете отображать их по порядку в соответствии с порядком
            itemList[position].apply {
                // Устанавливаем отступ
                itemView.setPadding(30 * level, 0, 0, 0)

                // Если есть подклассы
                if (sons != null) {
                    // Если дочерний элемент расширен
                    iconView.setImageResource(
                        if (isOpen) R.drawable.tree_open else R.drawable.tree_close
                    )
                    // Добавляем событие щелчка к изображению списка с дочерними элементами, изменяем, расширять ли
                    itemView.setOnClickListener {
                        isOpen = !isOpen
                        // Обновляем список и снова добавляем данные
                        begin = true
                        notifyDataSetChanged()
                    }
                } else {
                    // Устанавливаем флаг для корневого узла
                    iconView.setImageResource(R.drawable.ic_launcher_background)
                }
                //Добавить текст
                textView.text = string
            }
        }
    }

    // Поскольку этот метод вызывается первым каждый раз при обновлении списка, заполните список itemList в этом методе
    override fun getItemCount(): Int {
        if (begin) {
            itemList.clear()
            getList(treeItems)
            // Добавляем только один раз за обновление
            begin = false
        }
        return itemList.size
    }

    /** Передаем последовательность TreeItem */
    private fun getList(treeItems: List<TreeItem>) {
        // Добавляем каждый элемент в последовательности по очереди
        treeItems.forEach {
            // Сначала добавляем в список то, что нужно отобразить
            itemList.add(it)
            // Если этот элемент имеет подпункты и раскрывается
            // передаём список подпунктов этого элемента
            if (it.sons != null && it.isOpen) getList(it.sons!!)
        }
    }

    inner class TreeViewHolder(binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val iconView: ImageView = binding.idItemIcon
        val textView: TextView = binding.idItemText
    }

    init {
        itemList = java.util.ArrayList()
        begin = true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(newList: ArrayList<TreeItem>) {
        treeItems.clear()
        treeItems.addAll(newList)
        begin = true
        notifyDataSetChanged()
    }
}
