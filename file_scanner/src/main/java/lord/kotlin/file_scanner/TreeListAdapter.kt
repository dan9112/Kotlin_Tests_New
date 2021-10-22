package lord.kotlin.file_scanner

import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import lord.kotlin.file_scanner.databinding.ListItemBinding
import lord.kotlin.file_scanner.main.MainActivity
import java.io.File

/** Класс-адаптер для взаимодействия с TreeRecyclerView - [RecyclerView] для отображения многоуровневых списков */
class TreeListAdapter(
    /** Контекст RecyclerView */
    private val contextClass: MainActivity,
    /** Данные входящего списка */
    private val treeItems: ArrayList<TreeItem>
) : RecyclerView.Adapter<TreeListAdapter.TreeViewHolder>() {

    /** Отображаемые данные списка */
    private val itemList: MutableList<TreeItem> = ArrayList()

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
        itemList.apply {
            replace(treeItems.getVisibleItemList)
            return size
        }
    }

    /** Функция замены коллекции
     * @param newList новая коллекция */
    private fun MutableList<TreeItem>.replace(newList: List<TreeItem>) {
        clear()
        addAll(newList)
    }

    /** Список видимых элементов коллекции */
    private val List<TreeItem>.getVisibleItemList: List<TreeItem>
        get() {
            val list = ArrayList<TreeItem>()
            // Добавляем каждый элемент в последовательности по очереди
            forEach {
                // Сначала добавляем в список то, что нужно отобразить
                list.add(it)
                // Если этот элемент имеет подпункты и раскрывается
                // передаём список подпунктов этого элемента
                if (it.sons != null && it.isOpen) list.addAll(it.sons!!.getVisibleItemList)
            }
            return list
        }

    /** Количество добвленных/удалённых элементов [списка][itemList] */
    private val List<TreeItem>.getCount: Int
        get() {
            var count = 0
            forEach {
                count++
                if (it.sons != null && it.isOpen) count += it.sons!!.getCount
            }
            return count
        }

    inner class TreeViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /** Функция заполнения данными элемента [списка][itemList]
         * @param position позиция элемента */
        fun setData(position: Int) {
            binding.apply {
                contextClass.apply {
                    itemList[position].apply {
                        // Устанавливаем отступ
                        itemView.apply {
                            setPadding(30 * level, 0, 0, 0)
                            setBackgroundColor(
                                ContextCompat.getColor(
                                    contextClass,
                                    if (position % 2 == 1) {
                                        if (isDarkModeOn) R.color.black_light
                                        else R.color.white_dark
                                    } else {
                                        if (isDarkModeOn) android.R.color.black
                                        else R.color.white
                                    }
                                )
                            )
                        }
                        idItemText.setTextColor(
                            ContextCompat.getColor(
                                contextClass,
                                android.R.color.black
                            )
                        )
                        // Если есть подклассы
                        if (sons != null) {
                            // Если дочерний элемент расширен
                            idItemIcon.setImageResource(
                                if (isOpen) R.drawable.tree_open else R.drawable.tree_close
                            )
                            if (sons!!.size == 1 && sons!![0].string == null) {// Директория, содержимое которой недоступно, либо она пуста
                                itemView.setOnClickListener(null)
                                idItemIcon.setColorFilter(
                                    ContextCompat.getColor(
                                        contextClass,
                                        android.R.color.holo_red_dark
                                    )
                                )
                                idItemText.setTextColor(
                                    ContextCompat.getColor(
                                        contextClass,
                                        android.R.color.holo_red_dark
                                    )
                                )
                            } else {
                                idItemIcon.setColorFilter(
                                    ContextCompat.getColor(
                                        contextClass,
                                        android.R.color.black
                                    )
                                )
                                // Добавляем событие щелчка к изображению списка с дочерними элементами, изменяем, расширять ли
                                itemView.setOnClickListener {
                                    viewModel.apply {
                                        processStarted()
                                        isOpen = !isOpen
                                        // Обновляем список и снова добавляем данные
                                        notifyItemChanged(position)
                                        val count = sons!!.getCount
                                        if (isOpen) notifyItemRangeInserted(position + 1, count)
                                        else notifyItemRangeRemoved(position + 1, count)
                                        notifyItemRangeChanged(
                                            position + 1,
                                            itemList.size - position
                                        )
                                        processStopped()
                                    }
                                }
                            }
                        } else {
                            val uri = FileProvider.getUriForFile(
                                contextClass,
                                "${application.packageName}.provider",
                                File("${rootDirectoryPath}/$path")
                            )
                            // Устанавливаем флаг для корневого узла
                            idItemIcon.apply {
                                setImageResource(uri.getIconResource)
                                clearColorFilter()
                            }
                            itemView.setOnClickListener {
                                openFile(uri)
                            }
                        }
                        //Добавить текст
                        idItemText.text = string
                    }
                }
            }
        }
    }

    /** Функция открытия файла
     * @param fileUri Uri файла */
    fun openFile(fileUri: Uri) {
        // Open file with user selected app
        val intent = Intent(
            ACTION_VIEW,
            fileUri
        ).apply {
            addFlags(FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(FLAG_ACTIVITY_NO_HISTORY)
        }
        contextClass.startActivity(createChooser(intent, "Open file"))
    }

    /** Путь к файлу (директории) хранящемуся(-ейся) в [элементе многомерного списка][TreeItem] */
    private val TreeItem.path: String
        get() = if (parent != null) "${parent!!.path}/${string}"
        else string!!

    /** Уникальный идентификатор шконки в ресурсах, соответствующий типу MIME Uri файла */
    private val Uri.getIconResource: Int
        get() {
            contextClass.contentResolver.getType(this)!!.apply {
                return when (substringBefore("/", "")) {
                    "audio" -> R.drawable.ic_music_64_without_background// заменить на иконки для каждого отдельного формата!
                    "image" -> R.drawable.ic_image_64_without_background// заменить на иконки для каждого отдельного формата!
                    "video" -> R.drawable.ic_video_64_without_background// заменить на иконки для каждого отдельного формата!
                    "text" -> R.drawable.ic_txt_64_without_background
                    else -> when (substringAfter("application/", "")) {
                        "pdf" -> R.drawable.ic_pdf_64_without_background
                        "zip" -> R.drawable.ic_zip_64_without_background
                        "xml" -> R.drawable.ic_xml_64_without_background
                        "msword", "vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "vnd.openxmlformats-officedocument.wordprocessingml.template",
                        "vnd.ms-word.document.macroEnabled.12", "vnd.ms-word.template.macroEnabled.12" ->
                            R.drawable.ic_doc_64_without_background
                        "x-rar-compressed" -> R.drawable.ic_rar_64_without_background
                        else -> R.drawable.ic_unknown_file_64
                    }
                }
            }
        }
}
