package lord.kotlin.file_scanner.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import lord.kotlin.file_scanner.TreeItem

/** Визуальная модель [главной активности приложения][MainActivity] */
class MainViewModel : ViewModel() {

    private var _list = ArrayList<TreeItem>()

    /** Дерево файлов в файле (директории), в котором осуществлялось сканирование */
    val list: ArrayList<TreeItem>
        get() = _list

    /** Функция замены содержимого [дерева файлов][list] */
    fun replaceList(newList: Collection<TreeItem>) {
        _list.clear()
        _list.addAll(newList)
    }

    private var _isInProcess = MutableLiveData<Boolean>()

    /** Флаг активности процесса*/
    val isInProcess: LiveData<Boolean>
        get() = _isInProcess

    /** Функция изменения [флага][isInProcess] в состояние true */
    fun processStarted() {
        _isInProcess.value = true
    }

    /** Функция изменения [флага][isInProcess] в состояние false */
    fun processStopped() {
        _isInProcess.value = false
    }

    init {
        _isInProcess.value = false
    }
}
