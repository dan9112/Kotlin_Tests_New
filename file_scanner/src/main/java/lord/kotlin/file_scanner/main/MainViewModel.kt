package lord.kotlin.file_scanner.main

import androidx.lifecycle.ViewModel
import lord.kotlin.file_scanner.TreeItem

class MainViewModel : ViewModel() {
    private var _list = ArrayList<TreeItem>()
    val list: ArrayList<TreeItem>
    get() = _list

    fun replaceList(newList: Collection<TreeItem>) {
        _list.clear()
        _list.addAll(newList)
    }
}