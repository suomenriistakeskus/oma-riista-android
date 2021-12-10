package fi.riista.mobile.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fi.riista.mobile.models.MetsahallitusPermit

@Database(
        entities = [MetsahallitusPermit::class],
        version = 2,
        exportSchema = true)
@TypeConverters(Converters::class)
abstract class RiistaDatabase : RoomDatabase() {

    abstract fun metsahallitusPermitDao(): MetsahallitusPermitDAO
}
