package lord.kotlin.file_scanner

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import lord.kotlin.file_scanner.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TreeListAdapter
    internal lateinit var progressBar: ProgressBar
    internal lateinit var scanButton: Button

    private val list = ArrayList<TreeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TreeListAdapter(this, list)
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        ).apply {
            idListview.apply {
                this.adapter = this@MainActivity.adapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
            this@MainActivity.progressBar = progressBar
            scanButton = button
        }
    }

    private fun scanFiles(dir: File): ArrayList<TreeItem> {
        val files = ArrayList<TreeItem>()
        for (file in dir.listFiles()!!) {
            val item = TreeItem(file.name)
            Timber.d("item: " + item.string)
            if (file.isDirectory) {
                // Костыли. Нужны из-за того, что в последних версиях нет доступа к содержимому
                // некоторых директорий, что вызывает вылет без ошибок при попытке доступа:
                // isDirectory и isFile работают корректно, но приложение не может получить
                // содержимое
                if (file.name == "MUSIC")
                    Timber.i("Music Directory")
                if (file.childrenAreAvailable) scanFiles(file).forEach { item.add(it) }
                else item.add(TreeItem(null))
            }
            files.add(item)
        }
        return files
    }

    /** Возвращает true, если у приложения есть доступ к содержимому директории */
    private val File.childrenAreAvailable: Boolean
        get() {
            val sons = this.listFiles()
            return (sons != null && sons.isNotEmpty())
        }

    fun onClick(view: View) {
        scanButton.isEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) scan()
            else startActivity(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else scan()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun scan() {
        Thread {
            runOnUiThread { progressBar.visibility = VISIBLE }
            list.clear()
            list.addAll(scanFiles(File("/storage/emulated/0")))
            Timber.d("Весь список получен")
            runOnUiThread {
                adapter.notifyDataSetChanged()
                progressBar.visibility = GONE
                scanButton.isEnabled = true
            }
        }.start()
    }
}