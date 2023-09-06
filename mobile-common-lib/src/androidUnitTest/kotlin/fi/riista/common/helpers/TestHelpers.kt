package fi.riista.common.helpers

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.database.RiistaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext


actual val testCoroutineContext: CoroutineContext =
    Executors.newSingleThreadExecutor().asCoroutineDispatcher()
actual fun runBlockingTest(block: suspend CoroutineScope.() -> Unit) =
    runBlocking(testCoroutineContext) { this.block() }

actual fun createDatabaseDriverFactory(): DatabaseDriverFactory {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        RiistaDatabase.Schema.create(this)
    }
    driver.execute(null, "PRAGMA foreign_keys=ON", 0)
    return DatabaseDriverFactory(
        context = null,
        driver = driver,
    )
}
