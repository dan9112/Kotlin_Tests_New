package lord.kotlin.file_scanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import lord.kotlin.file_scanner.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TreeListAdapter

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
        var i = 0
        val files = ArrayList<TreeItem>()
        for (file in dir.listFiles()!!) {
            i++
            val item = TreeItem(file.name)
            Log.i("my", "item $i: ${item.string}")
            if (file.isDirectory && file.isAccepted) {
                scanFiles(file).forEach { item.add(it) }
            }
            files.add(item)
        }
        return files
    }

    private val File.isAccepted: Boolean
        get() {
            return when (name) {
                "obb", "data" -> false
                else -> true
            }
        }

    fun onClick(view: View) {
        val list = scanFiles(File("/storage/emulated/0"))
        Log.i("my", "Весь список получен")
        adapter.update(list)
    }
}