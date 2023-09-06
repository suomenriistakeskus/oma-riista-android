package fi.riista.common.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * A [CoroutineDispatcher] meant to be used when database is written. This includes all queries
 * that modify database and all transactions since those are assumed to write db.
 *
 * SqlDelight only allows one write access simultaneously and thus we need to limit access to db. We do this
 * by ensuring that all write access / transactions are executed in suspended functions that use this context.
 *
 * For example:
 *
 *   suspend fun writeHarvestToDb(harvest: CommonHarvest) = withContext(DatabaseWriteContext) {
 *     harvestQueries.transaction {
 *       // write harvest to db
 *     }
 *   }
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal val DatabaseWriteContext: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
