package lord.kotlin.tree_recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var mRecyclerView: RecyclerView

    private val mTreeItems: MutableList<TreeItem> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addList()
        mRecyclerView = findViewById(R.id.id_listview)
        mRecyclerView.adapter = TreeListAdapter(this, mTreeItems)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun addList() {
        mTreeItems.add(
            TreeItem("Game")
                .add(
                    TreeItem("Steam")
                        .add(TreeItem("CHi"))
                        .add(
                            TreeItem("Sha")
                                .add(TreeItem("bbbb"))
                                .add(TreeItem("cccc"))
                        )
                )
                .add(TreeItem("LOL"))
                .add(TreeItem("Car"))
        )
    }
}