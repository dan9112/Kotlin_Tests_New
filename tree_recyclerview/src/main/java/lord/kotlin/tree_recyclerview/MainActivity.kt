package lord.kotlin.tree_recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager

import lord.kotlin.tree_recyclerview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        ).idListview.apply {
            adapter = TreeListAdapter(this@MainActivity, defaultList)
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    /** Список элементов по умолчанию */
    private val defaultList = ArrayList<TreeItem>().apply {
        add(
            TreeItem("Game")
                .add(TreeItem("Steam")
                        .add(TreeItem("CHi"))
                        .add(TreeItem("Sha")
                                .add(TreeItem("bbbb"))
                                .add(TreeItem("cccc"))))
                .add(TreeItem("LOL"))
                .add(TreeItem("Car"))
        )
    }
}