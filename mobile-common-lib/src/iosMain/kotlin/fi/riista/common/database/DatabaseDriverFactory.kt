package fi.riista.common.database

import co.touchlab.sqliter.DatabaseConfiguration
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val schema = RiistaDatabase.Schema
        return NativeSqliteDriver(
            configuration = DatabaseConfiguration(
                name = "common.db",
                version = schema.version,
                create = { connection ->
                    wrapConnection(connection) { schema.create(it) }
                },
                upgrade = { connection, oldVersion, newVersion ->
                    wrapConnection(connection) { schema.migrate(it, oldVersion, newVersion) }
                },
                extendedConfig = DatabaseConfiguration.Extended(
                    foreignKeyConstraints = true
                )
            ),
            maxReaderConnections = 1
        )
    }
}
