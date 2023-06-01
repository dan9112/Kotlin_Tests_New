package com.example.content_provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.room.*

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
        var selection = selection
        var sortOrder = sortOrder
        return when (URI_MATCHER.match(uri)) {
            URI_EMPLOYEES -> {
                Log.d(LOG_TAG, "URI_EMPLOYEES")
                // если сортировка не указана, ставим свою - по id
                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_id ASC"
                Log.w(LOG_TAG, "$uri\t$projection\t$selection\t$selectionArgs\t$sortOrder")
                employeeDao.getAll(sortOrder!!)
            }

            URI_EMPLOYEES_ID -> {
                val id = uri.lastPathSegment!!.toLong()
                Log.d(LOG_TAG, "URI_EMPLOYEES_ID, $id")
                // добавляем ID к условию выборки
                selection = if (TextUtils.isEmpty(selection)) "_id = $id"
                else "${
                    selection!!
                        .split("?")
                        .zip(selectionArgs!!) { selectionPart, selectionArg -> selectionPart + selectionArg }
                } AND _id = $id"
                employeeDao.getById(id, selection)
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

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        Log.d(LOG_TAG, "insert, $uri")
        require(URI_MATCHER.match(uri) == URI_EMPLOYEES) { "Wrong URI: $uri" }
        val rowId = employeeDao.insert(
            values?.let {
                Employee(
                    name = it.getAsString("name") ?: "",
                    salary = it.getAsInteger("salary") ?: 0,
                    imageUri = it.getAsString("imageUri") ?: ""
                )
            } ?: Employee()
        )
        val resultUri = ContentUris.withAppendedId(EMPLOYEES_CONTENT_URI, rowId)
        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        context!!.contentResolver.notifyChange(resultUri, null)
        return resultUri
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
                var imageUri: String? = null

                selection
                    ?.split("?")
                    ?.zip(selectionArgs!!) { selectionPart, selectionArg ->
                        when {
                            selectionPart.matches(Regex(pattern = ".* ?name ?= ?")) -> name = selectionArg
                            selectionPart.matches(Regex(pattern = ".* ?salary ?= ?")) -> salary =
                                selectionArg.toInt()

                            selectionPart.matches(Regex(pattern = ".* ?imageUri ?= ?")) -> imageUri = selectionArg
                        }
                        selectionPart + selectionArg
                    }
                cnt = employeeDao.delete(id, name, salary, imageUri)

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
                val imageUri = values?.getAsString("imageUri")

                Log.d(LOG_TAG, "id: $id\tname: $name\tsalary: $salary\timageUri: $imageUri")

                cnt = if (name == null && salary == null && imageUri == null) 0
                else employeeDao.update(id, name, salary, imageUri)
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
    }
}


@Entity
data class Employee(
    var name: String = "",
    var salary: Int = 0,
    var imageUri: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employee ORDER BY :sortOrder")
    fun getAll(sortOrder: String): Cursor

    @Query("SELECT * FROM employee WHERE _id = :id AND :selection")
    fun getById(id: Long, selection: String): Cursor

    @Insert
    fun insert(employee: Employee): Long

    @Query(
        "UPDATE employee " +
                "SET name = CASE WHEN :name IS NULL THEN name ELSE :name END, " +
                "salary = CASE WHEN :salary IS NULL THEN salary ELSE :salary END, " +
                "imageUri = CASE WHEN :imageUri IS NULL THEN imageUri ELSE :imageUri END " +
                "WHERE _id = :id"
    )
    fun update(id: Long, name: String? = null, salary: Int? = null, imageUri: String? = null): Int

    @Query(
        "DELETE FROM employee WHERE (:id IS NULL OR _id LIKE :id) " +
                "AND (:name IS NULL OR name LIKE :name) " +
                "AND (:salary IS NULL OR salary LIKE :salary) " +
                "AND (:imageUri IS NULL OR imageUri LIKE :imageUri)"
    )
    fun delete(id: Long? = null, name: String? = null, salary: Int? = null, imageUri: String? = null): Int

    @Query("DELETE FROM employee")
    fun delete(): Int
}

const val MY_DATA_BASE = "database"

@Database(entities = [Employee::class], version = 1, exportSchema = false)
abstract class CustomDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
}
