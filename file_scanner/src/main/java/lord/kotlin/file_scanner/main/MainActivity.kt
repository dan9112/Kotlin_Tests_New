package lord.kotlin.file_scanner.main

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment.isExternalStorageManager
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import lord.kotlin.file_scanner.PermissionUtils
import lord.kotlin.file_scanner.R
import lord.kotlin.file_scanner.TreeItem
import lord.kotlin.file_scanner.TreeListAdapter
import lord.kotlin.file_scanner.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TreeListAdapter
    internal lateinit var progressBar: ProgressBar
    internal lateinit var scanButton: Button
    private lateinit var viewModel: MainViewModel

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) scan()
            scanButton.isEnabled = true
        }

    private val permissionAskListener = object : PermissionUtils.PermissionAskListener {
        override fun onPermissionGranted() {
            scan()
        }

        override fun onPermissionRequest() {
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionPreviouslyDenied() {
            Toast.makeText(this@MainActivity, "?????? ??????????, ??????????!", Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionDisabled() {
            Toast.makeText(this@MainActivity, "???????????? ???????????????????? ??????, ??????????!", Toast.LENGTH_SHORT)
                .show()
            startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // (??????????????????????!) ?????????????????? ???????????????????? ?? ?????????????????????? ???????????????????? ?????? ?????????? ????????????????
                data = Uri.fromParts("package", packageName, null)
            })
            scanButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        adapter = TreeListAdapter(this, viewModel.list)
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        ).apply {
            idListview.apply {
                this.adapter = this@MainActivity.adapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
            this@MainActivity.progressBar = progressBar
            scanButton = button.apply { setOnClickListener {
                isEnabled = false
                if (SDK_INT >= VERSION_CODES.R) {
                    if (isExternalStorageManager()) scan()
                    else startActivity(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // (??????????????????????!) ?????????????????? ???????????????????? ?? ?????????????????????? ???????????????????? ?????? ?????????? ????????????????
                    })
                } else PermissionUtils.checkPermission(
                    this@MainActivity,
                    READ_EXTERNAL_STORAGE,
                    permissionAskListener,
                    "permissionFlag"
                )
            } }
        }
    }

    private fun scanFiles(dir: File): ArrayList<TreeItem> {
        val files = ArrayList<TreeItem>()
        for (file in dir.listFiles()!!) {
            val item = TreeItem(file.name)
            Timber.d("item: " + item.string)
            if (file.isDirectory) {
                // ??????????????. ?????????? ????-???? ????????, ?????? ?? ?????????????????? ?????????????? ?????? ?????????????? ?? ??????????????????????
                // ?????????????????? ????????????????????, ?????? ???????????????? ?????????? ?????? ???????????? ?????? ?????????????? ??????????????:
                // isDirectory ?? isFile ???????????????? ??????????????????, ???? ???????????????????? ???? ?????????? ????????????????
                // ????????????????????
                if (file.childrenAreAvailable) scanFiles(file).forEach { item.add(it) }
                else item.add(TreeItem(null))
            }
            files.add(item)
        }
        // ???????????????????? ?? ???????????????????? ??????????????
        files.sortWith { previous, next ->
            if (previous.string != null && next.string != null) {
                if (previous.string!!.lowercase() > next.string!!.lowercase()) 1 else if (previous.string!!.lowercase() < next.string!!.lowercase()) -1 else 0
            } else 0
        }
        // ???????????????????? ???????????????????? / ??????????
        files.sortWith { previous, next ->
            if (!previous.sons.isNullOrEmpty() && next.sons.isNullOrEmpty()) -1 else if (previous.sons.isNullOrEmpty() && !next.sons.isNullOrEmpty()) 1 else 0
        }
        return files
    }

    /** ???????????????????? true, ???????? ?? ???????????????????? ???????? ???????????? ?? ?????????????????????? ???????????????????? */
    private val File.childrenAreAvailable: Boolean
        get() {
            val sons = this.listFiles()
            return (sons != null && sons.isNotEmpty())
        }

    override fun onResume() {
        super.onResume()
        if (SDK_INT >= VERSION_CODES.R && isExternalStorageManager()) scanButton.isEnabled =
            true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun scan() {
        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Main) { progressBar.visibility = VISIBLE }
            viewModel.replaceList(scanFiles(File("/storage/emulated/0")))
            Timber.d("???????? ???????????? ??????????????")
            withContext(Dispatchers.Main) {
                adapter.notifyDataSetChanged()
                progressBar.visibility = GONE
                scanButton.isEnabled = true
            }
        }
    }
}
