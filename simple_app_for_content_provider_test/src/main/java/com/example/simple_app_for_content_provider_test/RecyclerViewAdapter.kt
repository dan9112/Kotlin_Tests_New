package com.example.simple_app_for_content_provider_test

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_app_for_content_provider_test.MainActivity.Companion.EMPLOYEE_DOC_PATH
import com.example.simple_app_for_content_provider_test.MainActivity.Companion.EMPLOYEE_NAME
import com.example.simple_app_for_content_provider_test.MainActivity.Companion.EMPLOYEE_SALARY
import com.example.simple_app_for_content_provider_test.databinding.ItemBinding
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader

class RecyclerViewAdapter(cursor: Cursor, private val context: Context) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {
    private val dataList: MutableList<ItemData>

    inner class MyViewHolder(private val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(id: Long, name: String, salary: Int, title: String, content: String) = with(receiver = binding) {
            employeeId.text = id.toString()
            employeeName.text = name
            employeeSalary.text = salary.toString()
            employeeDocTitle.text = title
            employeeDocContent.text = content
        }
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = with(receiver = dataList.elementAt(position)) {
        holder.setData(id, name, salary, title, content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    fun update(cursor: Cursor) {
        val newDataList = cursor.getData()
        val employeeDiffUtilCallback = EmployeeDiffUtilCallback(dataList, newDataList)
        val employeeDiffResult = DiffUtil.calculateDiff(employeeDiffUtilCallback)
        dataList.clear()
        dataList.addAll(newDataList)
        employeeDiffResult.dispatchUpdatesTo(this)
    }

    @Suppress("Range")
    private fun Cursor.getData(): MutableList<ItemData> {
        val list = arrayListOf<ItemData>()
        if (moveToFirst()) do {
            val id = getLong(getColumnIndex("_id"))
            val name = getString(getColumnIndex(EMPLOYEE_NAME))
            val salary = getInt(getColumnIndex(EMPLOYEE_SALARY))
            val title = getString(getColumnIndex(EMPLOYEE_DOC_PATH)).substringAfterLast(delimiter = '/')
            val file = File.createTempFile("prefix", ".txt", context.cacheDir)
            getString(getColumnIndex(EMPLOYEE_DOC_PATH)).also {
                val outputStream = FileOutputStream(file)
                val inputStream = context.contentResolver.openInputStream(Uri.parse(it))!!
                ByteArray(inputStream.available()).also { data ->
                    inputStream.read(data)
                    outputStream.write(data)
                }
                inputStream.close()
                outputStream.close()
            }

            val content = with(receiver = FileReader(file)) {
                val text = readText()
                close()
                text
            }
            file.delete()
            list.add(ItemData(id, name, salary, title, content))
        } while (moveToNext())
        return list
    }

    init {
        dataList = cursor.getData()
    }

    private data class ItemData(val id: Long, val name: String, val salary: Int, val title: String, val content: String)

    private class EmployeeDiffUtilCallback(private val oldList: List<ItemData>, private val newList: List<ItemData>) :
        DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldEmployee = oldList[oldItemPosition]
            val newEmployee = newList[newItemPosition]
            return oldEmployee.content == newEmployee.content && oldEmployee.title == newEmployee.title && oldEmployee.name == newEmployee.name && oldEmployee.salary == newEmployee.salary
        }
    }
}