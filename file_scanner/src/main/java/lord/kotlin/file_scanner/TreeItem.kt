package lord.kotlin.file_scanner

import java.util.*

/** Класс элементов дерева.
 *
 * Уровень по умолчанию для каждого элемента равен 1, и уровень изменяется в соответствии с
 * родительским классом, когда он добавляется в родительский класс
 * @param string данные элемента */
class TreeItem(
    /** Сохраненные данные */
    var string: String?
) {

    /** Уровень пункта в дереве */
    var level = 1

    /** Флаг развёрнутости пункта.
     *
     * True - развёрнут, false - свёрнут*/
    var isOpen = false

    /** Подпункты */
    private var _sons: MutableList<TreeItem>? = null

    /** Родитель; пункт, для которого текущий является подпунктом */
    var parent: TreeItem? = null

    /** Коллекция подпунктов, если нет, то null */
    val sons: List<TreeItem>?
        get() = if (_sons == null || _sons!!.size == 0) null else _sons

    /** Функция добавления подпункта */
    fun add(sons: TreeItem): TreeItem {
        // Инициализируем при первом добавлении
        if (_sons == null) _sons = ArrayList()
        // Установите уровень подпункта в соответствии с вашим уровнем
        sons.setSonLevel(sons, level)
        sons.parent = this
        // Добавляем дочерний элемент в ваш список
        _sons!!.add(sons)
        return this
    }

    /** Функция установки уровня подпунктов текущего пункта в дереве */
    private fun setSonLevel(treeItem: TreeItem, level: Int) {
        // Уровень дочернего элемента на 1 выше, чем родительская категория
        treeItem.level = level + 1
        // Если есть подэлементы, вызвать рекурсивно и установить их уровень
        if (treeItem.sons != null) {
            for (item in treeItem.sons!!) {
                setSonLevel(item, item.level)
            }
        }
    }
}
