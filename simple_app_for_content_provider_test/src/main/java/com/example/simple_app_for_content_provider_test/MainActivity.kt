package com.example.simple_app_for_content_provider_test

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.util.*
import kotlin.properties.Delegates
import kotlin.random.Random

private const val LOG_TAG = "myLogs"

class MainActivity : AppCompatActivity() {

    private lateinit var cursor: Cursor

    private var size by Delegates.notNull<Int>()

    /** Called when the activity is first created.  */
    @SuppressLint("Range")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        contentResolver.query(
            EMPOYEE_URI, null, null, null, null
        )?.let { cursor = it } ?: run {
            Toast.makeText(
                this, "Connection error! Content provider returns null!", Toast.LENGTH_LONG
            ).show()
            finish()
        }
        size = cursor.count

        startManagingCursor(cursor)
        val from = arrayOf(EMPLOYEE_NAME, EMPLOYEE_SALARY)
        val to = intArrayOf(android.R.id.text1, android.R.id.text2)
        val adapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, from, to)
        val lvContact = findViewById<ListView>(R.id.lvContact)
        lvContact.adapter = adapter
    }

    @SuppressLint("Range")
    private fun getId(position: Int? = null) = with(cursor) {
        if (
            run {
                if (position != null) moveToPosition(position) else moveToLast()
            }
        ) getLong(cursor.getColumnIndex("_id"))
        else null
    }


    @SuppressLint("Range")
    fun onClickInsert(v: View?) {

        val contentValues = ContentValues().apply {
            put(EMPLOYEE_NAME, "name ${getId()?.let { it + 1 } ?: 0L}")
        }
        val newUri = contentResolver.insert(EMPOYEE_URI, contentValues)

        val file = File.createTempFile("prefix", ".txt", cacheDir)

        with(receiver = FileWriter(file)) {
            append("Some text\t${Random.nextInt()}")
            flush()
            close()
        }
        val textFile = "${newUri.toString()}/text.txt"

        var inputStream: InputStream = FileInputStream(file)
        var outputStream = contentResolver.openOutputStream(Uri.parse(textFile))!!
        ByteArray(inputStream.available()).also { data ->
            inputStream.read(data)
            outputStream.write(data)
        }
        inputStream.close()
        outputStream.close()

        with(receiver = contentValues) {
            clear()
            put(EMPLOYEE_DOC_PATH, textFile)
        }
        contentResolver.update(newUri!!, contentValues, null, null)
        file.delete()

        Toast.makeText(
            this@MainActivity,
            contentResolver.query(newUri, null, null, null, null)?.run {
                if (moveToFirst()) {
                    getString(getColumnIndex(EMPLOYEE_DOC_PATH))?.also {
                        outputStream = FileOutputStream(file)
                        inputStream = contentResolver.openInputStream(Uri.parse(it))!!
                        ByteArray(inputStream.available()).also { data ->
                            inputStream.read(data)
                            outputStream.write(data)
                        }
                        inputStream.close()
                        outputStream.close()
                    }
                    close()

                    with(receiver = FileReader(file)) {
                        val text = readText()
                        close()
                        text
                    }
                } else {
                    "Record not found!"
                }
            } ?: "Error reading inserted record!",
            Toast.LENGTH_SHORT
        ).show()

        size++
        Log.d(LOG_TAG, "insert, result Uri : $newUri;\tfile deleted - ${file.delete()}")
    }

    fun onClickUpdate(v: View) {
        val cv = ContentValues().apply {
            put(EMPLOYEE_SALARY, Random.nextInt())
        }
        Log.d(
            LOG_TAG,
            if (size > 0) {
                val uri = ContentUris.withAppendedId(EMPOYEE_URI, getId(Random.nextInt(size))!!)
                val cnt = contentResolver.update(uri, cv, null, null)
                "update, count = $cnt"
            } else {
                "Database is empty!"
            }
        )
    }

    fun onClickDelete(v: View?) {
        if (size == 0) Log.d(LOG_TAG, "Database is empty!")
        else {
            val uri = ContentUris.withAppendedId(EMPOYEE_URI, getId(Random.nextInt(size))!!)
            try {
                val cnt = contentResolver.delete(uri, null, null)
                size--
                Log.d(LOG_TAG, "delete, count = $cnt")
            } catch (e: IllegalArgumentException) {
                Log.d(LOG_TAG, "Incorrect arguments for command: ${e.message}")
            } catch (e: Exception) {
                Log.w(LOG_TAG, "Error caught: ${e.message}")
            }
        }
    }

    fun onClickError(v: View?) {

        val uri = Uri.parse("content://ru.start_android.providers.AddressBook/phones")
        try {

            contentResolver.query(
                EMPOYEE_URI, null, null,
                null, null
            )

            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.close()
        } catch (ex: Exception) {
            Log.d(LOG_TAG, "Error: ${ex.javaClass}, ${ex.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopManagingCursor(cursor)
    }

    private companion object {
        val EMPOYEE_URI = Uri.parse("content://ru.lord.providers.Company/employees")
        const val EMPLOYEE_NAME = "name"
        const val EMPLOYEE_SALARY = "salary"
        const val EMPLOYEE_DOC_PATH = "docPath"
    }
}
