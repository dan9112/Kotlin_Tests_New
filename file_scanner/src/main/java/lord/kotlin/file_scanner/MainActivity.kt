package lord.kotlin.file_scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import lord.kotlin.file_scanner.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TreeListAdapter
    internal lateinit var progressBar: ProgressBar
    internal lateinit var scanButton: Button

    private val list = ArrayList<TreeItem>()

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) scan()
            scanButton.isEnabled =true
        }

    private val permissionAskListener = object : PermissionUtils.PermissionAskListener {
        override fun onPermissionGranted() {
            scan()
        }

        override fun onPermissionRequest() {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionPreviouslyDenied() {
            Toast.makeText(this@MainActivity, "Так нужно, чувак!", Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionDisabled() {
            Toast.makeText(this@MainActivity, "Включи разрешение сам, чувак!", Toast.LENGTH_SHORT)
                .show()
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // (Опционально!) Открывает активность с настройками приложения как новое действие
                data = Uri.fromParts("package", packageName, null)
            })
            scanButton.isEnabled = true
        }
    }

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
                if (file.childrenAreAvailable) scanFiles(file).forEach { item.add(it) }
                else item.add(TreeItem(null))
            }
            files.add(item)
        }
        // Сортировка в алфавитном порядке
        files.sortWith { lhs, rhs ->
            if (lhs.string != null && rhs.string != null) {
                if (lhs.string!! > rhs.string!!) 1 else if (lhs.string!! < rhs.string!!) -1 else 0
            } else 0
        }
        // Сортировка директории / файлы
        files.sortWith { lhs, rhs ->
            if (!lhs.sons.isNullOrEmpty() && rhs.sons.isNullOrEmpty()) -1 else if (lhs.sons.isNullOrEmpty() && !rhs.sons.isNullOrEmpty()) 1 else 0
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
        } else PermissionUtils.checkPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            permissionAskListener,
            "permissionFlag"
        )
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
