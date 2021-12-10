package fi.riista.mobile.database.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import fi.riista.mobile.models.MetsahallitusPermit

@Dao
abstract class MetsahallitusPermitDAO {

    @Query("""
        SELECT *
        FROM mh_permit
        WHERE user_name = :username
        ORDER BY begin_date DESC, end_date DESC, permit_type ASC
        """)
    abstract fun findAllByUsername(username: String): LiveData<List<MetsahallitusPermit>>

    @Query("SELECT * FROM mh_permit WHERE permit_identifier = :permitIdentifier")
    abstract fun findOneByPermitIdentifier(permitIdentifier: String): LiveData<MetsahallitusPermit>

    @Insert
    abstract fun insertAll(mhPermits: List<MetsahallitusPermit>)

    @Transaction
    open fun update(username: String, mhPermits: List<MetsahallitusPermit>) {
        deleteAllByUsername(username)

        if (mhPermits.isNotEmpty()) {
            val permitIdentifiers: List<String> = mhPermits.map { it.permitIdentifier }
            deleteByPermitIdentifierIn(permitIdentifiers)
        }

        insertAll(mhPermits)
    }

    @Query("DELETE FROM mh_permit WHERE user_name = :username")
    abstract fun deleteAllByUsername(username: String)

    @Query("DELETE FROM mh_permit WHERE permit_identifier IN (:permitIdentifiers)")
    abstract fun deleteByPermitIdentifierIn(permitIdentifiers: List<String>)
}
