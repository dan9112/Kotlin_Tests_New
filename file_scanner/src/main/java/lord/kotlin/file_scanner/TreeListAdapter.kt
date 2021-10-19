package lord.kotlin.file_scanner

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import lord.kotlin.file_scanner.databinding.ListItemBinding
import lord.kotlin.file_scanner.main.MainActivity

class TreeListAdapter(
    /** Контекст RecyclerView */
    private val context: Context,
    /** Данные входящего списка */
    private val treeItems: ArrayList<TreeItem>
) : RecyclerView.Adapter<TreeListAdapter.TreeViewHolder>() {

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
        // В методе getList все данные, которые необходимо отобразить, добавляются в itemList, поэтому вы можете отображать их по порядку в соответствии с порядком
        holder.setData(position)
    }

    // Поскольку этот метод вызывается первым каждый раз при обновлении списка, заполните список itemList в этом методе
    override fun getItemCount(): Int {
        itemList.clear()
        getList(treeItems)
        // Добавляем только один раз за обновление
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

    private fun getCount(treeItems: List<TreeItem>): Int {
        var count = 0
        treeItems.forEach {
            count++
            if (it.sons != null && it.isOpen) count += getCount(it.sons!!)
        }
        return count
    }

    inner class TreeViewHolder(binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val iconView: ImageView = binding.idItemIcon
        private val textView: TextView = binding.idItemText

        fun setData(position: Int) {
            itemList[position].apply {
                // Устанавливаем отступ
                itemView.setPadding(30 * level, 0, 0, 0)
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                // Если есть подклассы
                if (sons != null) {
                    // Если дочерний элемент расширен
                    iconView.setImageResource(
                        if (isOpen) R.drawable.tree_open else R.drawable.tree_close
                    )
                    if (sons!!.size == 1 && sons!![0].string == null) {// Директория, содержимое которой недоступно, либо она пуста
                        itemView.setOnClickListener(null)
                        iconView.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                android.R.color.holo_red_dark
                            )
                        )
                        textView.setTextColor(
                            ContextCompat.getColor(
                                context,
                                android.R.color.holo_red_dark
                            )
                        )
                    } else {
                        iconView.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                android.R.color.black
                            )
                        )
                        // Добавляем событие щелчка к изображению списка с дочерними элементами, изменяем, расширять ли
                        itemView.setOnClickListener {
                            val activity = (context as MainActivity)
                            activity.apply {
                                progressBar.visibility = VISIBLE
                                scanButton.isEnabled = false
                            }
                            isOpen = !isOpen
                            // Обновляем список и снова добавляем данные
                            notifyItemChanged(position)
                            val count = getCount(this.sons!!)
                            if (isOpen) notifyItemRangeInserted(position + 1, count)
                            else notifyItemRangeRemoved(position + 1, count)
                            notifyItemRangeChanged(position + 1, itemList.size - position)
                            activity.apply {
                                progressBar.visibility = GONE
                                scanButton.isEnabled = true
                            }
                        }
                    }
                } else {
                    // Устанавливаем флаг для корневого узла
                    iconView.apply {
                        setImageResource(getIconResource(string!!.substringAfterLast('.', "")))
                        clearColorFilter()
                    }
                    itemView.setOnClickListener(null)
                }
                //Добавить текст
                textView.text = string
            }
        }
    }

    init {
        itemList = java.util.ArrayList()
    }

    private fun getIconResource(extension: String): Int {
        return when (extension.uppercase()) {
            "MP3", "M4A", "3GA", "AAC", "OGG", "OGA", "WAV", "WMA", "AMR", "AWB", "FLAC", "MID", "MIDI", "XMF", "MXMF", "IMY", "RTTTL", "RTX", "OTA" -> R.drawable.ic_music_64_without_background
            "GIF", "JPG", "PNG" -> R.drawable.ic_image_64_without_background// не нашёл список всех поддерживаемых планшетом расширений
            "MP4", "M4V", "3GP", "3G2", "WMV", "ASF", "AVI", "FLV", "MKV", "WEBM" -> R.drawable.ic_video_64_without_background
            "APK" -> R.drawable.ic_apk_64
            else -> R.drawable.ic_unknown_file_64
        }
    }
}
