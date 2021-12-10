package fi.riista.mobile.database.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE mh_permit ADD COLUMN harvest_feedback_url TEXT")
            database.execSQL("ALTER TABLE mh_permit ADD COLUMN harvest_feedback_url_sv TEXT")
            database.execSQL("ALTER TABLE mh_permit ADD COLUMN harvest_feedback_url_en TEXT")
        }
    }
}
