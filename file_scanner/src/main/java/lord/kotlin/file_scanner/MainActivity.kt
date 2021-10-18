package lord.kotlin.file_scanner

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import lord.kotlin.file_scanner.databinding.ActivityMainBinding
import java.io.File
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import lord.kotlin.file_scanner.check_permissions.PermissionUtils
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TreeListAdapter

    val requestPermissionLauncher =
        registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) scan()
        }

    private val permissionAskListener = object : PermissionUtils.PermissionAskListener {
        override fun onPermissionGranted() {
            scan()
        }

        override fun onPermissionRequest() {
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionPreviouslyDenied() {
            Toast.makeText(this@MainActivity, "Так нужно, чувак!", Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionDisabled() {
            Toast.makeText(this@MainActivity, "Включи разрешение сам, чувак!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // (Опционально!) Открывает активность с настройками приложения как новое действие
                data = Uri.fromParts("package", packageName, null)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TreeListAdapter(this, ArrayList<TreeItem>().apply {
            add(
                TreeItem("Directory 1")
                    .add(
                        TreeItem("Directory 1.1")
                            .add(TreeItem("File 1.1.1"))
                            .add(TreeItem("File 1.1.2"))
                    )
                    .add(
                        TreeItem("Directory 1.2")
                            .add(TreeItem("File 1.2.1"))
                    )
                    .add(TreeItem("File 1.3"))
            )
            add(
                TreeItem("Directory 2")
                    .add(TreeItem("File 2.1"))
            )
            add(TreeItem("File 3"))
        })
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        ).idListview.apply {
            this.adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
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
        return files
    }

    /** Возвращает true, если у приложения есть доступ к содержимому директории */
    private val File.childrenAreAvailable: Boolean
        get() {
            val sons = this.listFiles()
            return (sons != null && sons.isNotEmpty())
        }

    fun onClick(view: View) {
        PermissionUtils.checkPermission(
            this,
            READ_EXTERNAL_STORAGE,
            permissionAskListener,
            "permissionFlag"
        )
    }

    private fun scan() {
        val list = scanFiles(File("/storage/emulated/0"))
        Timber.d("Весь список получен")
        adapter.update(list)
    }
}