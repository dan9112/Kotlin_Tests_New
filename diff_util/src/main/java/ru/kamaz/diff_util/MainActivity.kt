package ru.kamaz.diff_util

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ru.kamaz.diff_util.databinding.ActivityMainBinding
import ru.kamaz.diff_util.recycler_view.RecyclerViewAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerViewAdapter = RecyclerViewAdapter(
            items = ArrayList(
                if (savedInstanceState != null && savedInstanceState.containsKey(listKey))
                    savedInstanceState.getParcelableArrayList(listKey)!!
                else listOf(
                    Item(id = 1, name = "Name1", value = "100"),
                    Item(id = 2, name = "Name2", value = "200"),
                    Item(id = 3, name = "Name3", value = "300"),
                    Item(id = 4, name = "Name4", value = "400"),
                    Item(id = 5, name = "Name5", value = "500")
                )
            )
        )

        with(receiver = binding) {
            with(receiver = rv) {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = recyclerViewAdapter
            }
            with(receiver = update) {
                setOnClickListener {
                    recyclerViewAdapter.setItems(
                        listOf(
                            Item(id = 4, name = "Name4", value = "400"),
                            Item(id = 2, name = "Имя_2", value = "две_сотни"),
                            Item(id = 5, name = "Имя 5", value = "пятьсот"),
                            Item(id = 1, name = "Name1", value = "100")
                        )
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState.apply {
            putParcelableArrayList(listKey, recyclerViewAdapter.items)
        })
    }

    companion object {
        const val listKey = "listKey"
    }
}
