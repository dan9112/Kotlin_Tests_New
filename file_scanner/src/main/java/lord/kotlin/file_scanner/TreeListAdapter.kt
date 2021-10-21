package lord.kotlin.file_scanner

import android.content.Intent
import android.content.Intent.*
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import lord.kotlin.file_scanner.databinding.ListItemBinding
import lord.kotlin.file_scanner.main.MainActivity
import java.io.File

class TreeListAdapter(
    /** Контекст RecyclerView */
    private val contextClass: MainActivity,
    /** Данные входящего списка */
    private val treeItems: ArrayList<TreeItem>
) : RecyclerView.Adapter<TreeListAdapter.TreeViewHolder>() {

    /** Отображаемые данные списка */
    private val itemList: MutableList<TreeItem>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        return TreeViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(contextClass),
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
                itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        contextClass,
                        if (position % 2 == 1) {
                            if (contextClass.isDarkModeOn) R.color.black_light
                            else R.color.white_dark
                        } else {
                            if (contextClass.isDarkModeOn) android.R.color.black
                            else R.color.white
                        }
                    )
                )
                textView.setTextColor(ContextCompat.getColor(contextClass, android.R.color.black))
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
                                contextClass,
                                android.R.color.holo_red_dark
                            )
                        )
                        textView.setTextColor(
                            ContextCompat.getColor(
                                contextClass,
                                android.R.color.holo_red_dark
                            )
                        )
                    } else {
                        iconView.setColorFilter(
                            ContextCompat.getColor(
                                contextClass,
                                android.R.color.black
                            )
                        )
                        // Добавляем событие щелчка к изображению списка с дочерними элементами, изменяем, расширять ли
                        itemView.setOnClickListener {
                            contextClass.apply {
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
                            contextClass.apply {
                                progressBar.visibility = GONE
                                scanButton.isEnabled = true
                            }
                        }
                    }
                } else {
                    // Устанавливаем флаг для корневого узла
                    iconView.apply {
                        setImageResource(getIconResource(string!!.getExtension))
                        clearColorFilter()
                    }
                    itemView.setOnClickListener {
                        val iconRes = getIconResource(string!!.getExtension)
                        if (iconRes != R.drawable.ic_unknown_file_64 && iconRes != R.drawable.ic_apk_64) {
                            openFile(File("${contextClass.path}/$path"))
                        }
                    }
                }
                //Добавить текст
                textView.text = string
            }
        }
    }

    fun openFile(file: File) {
        // Open file with user selected app
        val intent = Intent(
            ACTION_VIEW,
            // Get URI of file
            FileProvider.getUriForFile(
                contextClass,
                "${contextClass.application.packageName}.provider",
                file
            )
        ).apply {
            addFlags(FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(FLAG_ACTIVITY_NO_HISTORY)
        }
        contextClass.startActivity(createChooser(intent, "Open file"))
    }

    private val TreeItem.path: String
        get() = if (parent != null) "${parent!!.path}/${string}"
        else string!!

    private val String.getExtension: String
        get() = substringAfterLast('.', "")

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
