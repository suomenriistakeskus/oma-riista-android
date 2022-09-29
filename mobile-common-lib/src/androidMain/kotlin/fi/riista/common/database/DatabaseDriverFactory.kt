package fi.riista.common.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

/**
 * On real environment give context. On tests give driver.
 */
actual class DatabaseDriverFactory(
    private val context: Context?,
    private val driver: SqlDriver? = null,
) {
    actual fun createDriver(): SqlDriver {
        when {
            context != null ->
                return AndroidSqliteDriver(
                    schema = RiistaDatabase.Schema,
                    context = context,
                    name = "common.db",
                    callback = object : AndroidSqliteDriver.Callback(RiistaDatabase.Schema) {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            // Enable foreign keys (disabled by default on Sqlite)
                            db.execSQL("PRAGMA foreign_keys=ON;")
                        }
                    }
                )
            driver != null -> return driver
            else -> throw RuntimeException("Unable to return driver")
        }
    }
}
