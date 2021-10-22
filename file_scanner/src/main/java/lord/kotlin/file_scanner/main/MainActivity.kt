package lord.kotlin.file_scanner.main

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.drawable.ColorDrawable
import android.net.Uri.fromParts
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment.isExternalStorageManager
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import lord.kotlin.file_scanner.PermissionUtils
import lord.kotlin.file_scanner.R
import lord.kotlin.file_scanner.TreeItem
import lord.kotlin.file_scanner.TreeListAdapter
import lord.kotlin.file_scanner.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/** Главная активность приложения */
class MainActivity : AppCompatActivity() {
    /** Адаптер RecyclerView */
    private lateinit var adapter: TreeListAdapter

    /** Кнопка запуска сканирования */
    private lateinit var scanButton: Button

    /** Визуальная модель класса */
    internal lateinit var viewModel: MainViewModel

    /** Путь к директории, в которой осуществляется сканирование */
    internal val rootDirectoryPath = "/storage/emulated/0"

    /** Флаг активности ночного режима */
    internal val isDarkModeOn: Boolean
        get() = (resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES

    /** Средство запуска запроса разрешения */
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) scan()
            scanButton.isEnabled = true
        }

    /** Реализация интерфейса обработки результата проверки разрешения */
    private val permissionAskListener = object : PermissionUtils.PermissionAskListener {
        override fun onPermissionGranted() {
            scan()
        }

        override fun onPermissionRequest() {
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionPreviouslyDenied() {
            Toast.makeText(
                this@MainActivity,
                "Так нужно, чувак!",
                Toast.LENGTH_SHORT
            )// Заменить на диалог-фрагмент
                .show()
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionDisabled() {
            Toast.makeText(
                this@MainActivity,
                "Включи разрешение сам, чувак!",
                Toast.LENGTH_SHORT
            )// Заменить на диалог-фрагмент
                .show()
            startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK) // (Опционально!) Открывает активность с настройками приложения как новое действие
                data = fromParts("package", packageName, null)
            })
            scanButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    if (isDarkModeOn) R.color.black_light else R.color.white_dark
                )
            )
        )
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        adapter = TreeListAdapter(this, viewModel.list)
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        ).apply {
            viewModel.apply {
                isInProcess.observe(this@MainActivity, { value ->
                    when (value) {
                        true -> {
                            progressBar.visibility = VISIBLE
                            scanButton.isEnabled = false
                        }
                        false -> {
                            progressBar.visibility = GONE
                            scanButton.isEnabled = true
                        }
                    }
                })
            }

            idListview.apply {
                this.adapter = this@MainActivity.adapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
            scanButton = button.apply {
                setOnClickListener {
                    isEnabled = false
                    if (SDK_INT >= VERSION_CODES.R) {
                        if (isExternalStorageManager()) scan()
                        else startActivity(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                            addFlags(FLAG_ACTIVITY_NEW_TASK) // (Опционально!) Открывает активность с настройками приложения как новое действие
                        })
                    } else PermissionUtils.checkPermission(
                        this@MainActivity,
                        READ_EXTERNAL_STORAGE,
                        permissionAskListener,
                        "permissionFlag"
                    )
                }
            }
        }
    }

    /** Дерево файлов и директорий в файле (директории) */
    private val File.scanFiles: ArrayList<TreeItem>
        get() = ArrayList<TreeItem>().apply {
            for (file in listFiles()!!) {
                file.apply {
                    val item = TreeItem(name)
                    Timber.d("item: " + item.string)
                    if (isDirectory) {
                        // Костыли. Нужны из-за того, что в последних версиях нет доступа к содержимому
                        // некоторых директорий, что вызывает вылет без ошибок при попытке доступа:
                        // isDirectory и isFile работают корректно, но приложение не может получить
                        // содержимое
                        if (childrenAreAvailable) scanFiles.forEach { item.add(it) }
                        else item.add(TreeItem(null))
                    }
                    add(item)
                }
            }
            sortWith { previous, next ->// Сортировка в алфавитном порядке
                if (previous.string != null && next.string != null) {
                    if (previous.string!!.lowercase() > next.string!!.lowercase()) 1 else if (previous.string!!.lowercase() < next.string!!.lowercase()) -1 else 0
                } else 0
            }
            sortWith { previous, next ->// Сортировка директории / файлы
                if (!previous.sons.isNullOrEmpty() && next.sons.isNullOrEmpty()) -1 else if (previous.sons.isNullOrEmpty() && !next.sons.isNullOrEmpty()) 1 else 0
            }
        }

    /** Возвращает true, если у приложения есть доступ к содержимому директории */
    private val File.childrenAreAvailable: Boolean
        get() = !listFiles().isNullOrEmpty()

    override fun onResume() {
        super.onResume()
        if (SDK_INT >= VERSION_CODES.R && isExternalStorageManager()) scanButton.isEnabled =
            true
    }

    /** Функция сканирования файлов в корневой директории устройства.
     *
     * Запускает сопрограмму, в которой происходит [получение дерева файлов и директорий][File.scanFiles] и замена им [списка, используемого адаптером][MainViewModel.list] */
    @SuppressLint("NotifyDataSetChanged")
    private fun scan() {
        CoroutineScope(Default).launch {
            viewModel.apply {
                withContext(Main) { processStarted() }
                replaceList(File(rootDirectoryPath).scanFiles)
                withContext(Main) {
                    adapter.notifyDataSetChanged()
                    Timber.d("Весь список получен")
                    processStopped()
                }
            }
        }
    }
}
