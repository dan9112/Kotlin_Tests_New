package com.example.content_provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import androidx.room.*
import java.io.*

private const val LOG_TAG = "myLogs"

class CustomContentProvider : ContentProvider() {

    private lateinit var employeeDao: EmployeeDao
    override fun onCreate(): Boolean {
        val database = Room.databaseBuilder(context!!, CustomDatabase::class.java, MY_DATA_BASE)
            .build()

        employeeDao = database.employeeDao()
        return true
    }

    @Suppress("Name_Shadowing")
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor {
        var sortOrder = sortOrder
        return when (URI_MATCHER.match(uri)) {
            URI_EMPLOYEES -> {
                Log.d(LOG_TAG, "URI_EMPLOYEES")
                // если сортировка не указана, ставим свою - по id
                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_id ASC"
                Log.v(LOG_TAG, "$uri\t$projection\t$selection\t$selectionArgs\t$sortOrder")
                employeeDao.getAll(sortOrder!!)
            }

            URI_EMPLOYEES_ID -> {
                val id = uri.lastPathSegment!!.toLong()
                Log.d(LOG_TAG, "URI_EMPLOYEES_ID, $id")
                employeeDao.getById(id).apply {
                    Log.v(LOG_TAG, count.toString())
                }
            }

            else -> throw IllegalArgumentException("Wrong URI: $uri")
        }
            .apply {
                setNotificationUri(context!!.contentResolver, EMPLOYEES_CONTENT_URI)
            }
    }

    override fun getType(uri: Uri): String {
        Log.d(LOG_TAG, "getType, $uri")
        return when (URI_MATCHER.match(uri)) {
            URI_EMPLOYEES -> EMPLOYEES_CONTENT_TYPE
            URI_EMPLOYEES_ID -> EMPLOYEES_CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun getStreamTypes(uri: Uri, mimeTypeFilter: String): Array<String>? {
        Log.d(LOG_TAG, "getStreamType, $uri")
        return when (URI_STREAM_MATCHER.match(uri)) {
            URI_EMPLOYEES_ID -> arrayOf("text/plain")
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        Log.d(LOG_TAG, "insert, $uri")
        require(URI_MATCHER.match(uri) == URI_EMPLOYEES) { "Wrong URI: $uri" }
        val rowId = employeeDao.insert(
            values?.let {
                Employee(
                    name = it.getAsString("name") ?: "",
                    salary = it.getAsInteger("salary") ?: 0
                )
            } ?: Employee()
        )
        val resultUri = ContentUris.withAppendedId(EMPLOYEES_CONTENT_URI, rowId)
        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        context!!.contentResolver.notifyChange(resultUri, null)
        return resultUri
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val root = context!!.filesDir
//        val cacheDir = context!!.cacheDir

        val inputPath = uri.encodedPath!!
        val lastSlashIndex = inputPath.lastIndexOf('/')
        val directoryPath = inputPath.substring(0, lastSlashIndex)
        val fileName = inputPath.substring(lastSlashIndex + 1)
        Log.v(LOG_TAG, uri.toString())

        val path = File(root, directoryPath)
        path.mkdirs()
        val file = File(path, fileName)
        var imode = 0
        if (mode.contains("w")) {
            imode = imode or ParcelFileDescriptor.MODE_WRITE_ONLY
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        if (mode.contains("r")) imode = imode or ParcelFileDescriptor.MODE_READ_ONLY
        if (mode.contains("+")) imode = imode or ParcelFileDescriptor.MODE_APPEND
        return ParcelFileDescriptor.open(file, imode)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        Log.d(LOG_TAG, "delete, $uri")
        val cnt: Int
        when (URI_MATCHER.match(uri)) {
            URI_EMPLOYEES -> {
                Log.d("myLog", "Clear all data")
                cnt = employeeDao.delete()
            }

            URI_EMPLOYEES_ID -> {
                val id = uri.lastPathSegment!!.toLong()
                Log.d(LOG_TAG, "URI_EMPLOYEE_ID, $id")

                var name: String? = null
                var salary: Int? = null
                var docPath: String? = null

                selection
                    ?.split("?")
                    ?.zip(selectionArgs!!) { selectionPart, selectionArg ->
                        when {
                            selectionPart.matches(Regex(pattern = ".* ?name ?= ?")) -> name = selectionArg
                            selectionPart.matches(Regex(pattern = ".* ?salary ?= ?")) -> salary =
                                selectionArg.toInt()

                            selectionPart.matches(Regex(pattern = ".* ?docPath ?= ?")) -> docPath = selectionArg
                        }
                        selectionPart + selectionArg
                    }
                cnt = employeeDao.delete(id, name, salary, docPath)

            }

            else -> throw IllegalArgumentException("Wrong URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return cnt
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val cnt: Int
        when (URI_MATCHER.match(uri)) {
            URI_EMPLOYEES_ID -> {
                val id = uri.lastPathSegment!!.toLong()
                Log.d(LOG_TAG, "URI_EMPLOYEES_ID, $id")

                val name = values?.getAsString("name")
                val salary = values?.getAsInteger("salary")
                val docPath = values?.getAsString("docPath")

                Log.d(LOG_TAG, "id: $id\tname: $name\tsalary: $salary\tdocPath: $docPath")

                cnt = if (name == null && salary == null && docPath == null) 0
                else employeeDao.update(id, name, salary, docPath)
            }

            else -> throw IllegalArgumentException("Wrong URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return cnt
    }

    companion object {
        const val AUTHORITY = "ru.lord.providers.Company"

        private const val EMPLOYEES_PATH = "employees"

        val EMPLOYEES_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$EMPLOYEES_PATH")

        const val EMPLOYEES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.$EMPLOYEES_PATH"
        const val EMPLOYEES_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$AUTHORITY.$EMPLOYEES_PATH"

        private const val URI_EMPLOYEES = 1
        private const val URI_EMPLOYEES_ID = 2

        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, EMPLOYEES_PATH, URI_EMPLOYEES)
            addURI(AUTHORITY, "$EMPLOYEES_PATH/#", URI_EMPLOYEES_ID)
        }

        private val URI_STREAM_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "$EMPLOYEES_PATH/#", URI_EMPLOYEES_ID)
        }
    }
}


@Entity
data class Employee(
    var name: String = "",
    var salary: Int = 0,
    var docPath: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employee ORDER BY :sortOrder")
    fun getAll(sortOrder: String): Cursor

    @Query("SELECT * FROM employee WHERE _id = :id")
    fun getById(id: Long): Cursor

    @Insert
    fun insert(employee: Employee): Long

    @Query(
        "UPDATE employee " +
                "SET name = CASE WHEN :name IS NULL THEN name ELSE :name END, " +
                "salary = CASE WHEN :salary IS NULL THEN salary ELSE :salary END, " +
                "docPath = CASE WHEN :docPath IS NULL THEN docPath ELSE :docPath END " +
                "WHERE _id = :id"
    )
    fun update(id: Long, name: String? = null, salary: Int? = null, docPath: String? = null): Int

    @Query(
        "DELETE FROM employee WHERE (:id IS NULL OR _id LIKE :id) " +
                "AND (:name IS NULL OR name LIKE :name) " +
                "AND (:salary IS NULL OR salary LIKE :salary) " +
                "AND (:docPath IS NULL OR docPath LIKE :docPath)"
    )
    fun delete(id: Long? = null, name: String? = null, salary: Int? = null, docPath: String? = null): Int

    @Query("DELETE FROM employee")
    fun delete(): Int
}

const val MY_DATA_BASE = "database"

@Database(entities = [Employee::class], version = 1, exportSchema = false)
abstract class CustomDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
}
