package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.util.letIf


class GroupHuntingContext internal constructor(
    private val backendApiProvider: BackendApiProvider,
    private val isGroupHuntingAvailable: () -> Boolean,
): BackendApiProvider by backendApiProvider {

    private val _clubContextsProvider = GroupHuntingClubContextsFromNetworkProvider(backendApiProvider)
    val clubContextsProvider: GroupHuntingClubContextsProvider = _clubContextsProvider

    /**
     * A convenience getter for obtaining club contexts directly instead
     * of using the [clubContextsProvider].
     */
    val clubContexts: List<GroupHuntingClubContext>
        get() = clubContextsProvider.clubContexts

    /**
     * Is the group hunting available? Being available indicates that the functionality is enabled
     * for the current user and that there are are clubs and groups.
     */
    val groupHuntingAvailable: Boolean
        get() {
            return isGroupHuntingAvailable() && clubContexts.isNotEmpty()
        }

    /**
     * Returns true if the group hunting is enabled for the user and there are clubs contexts.
     * False otherwise.
     */
    suspend fun checkAvailabilityAndFetchClubs(refresh: Boolean = false) {
        if (!isGroupHuntingAvailable()) {
            logger.i { "Group hunting functionality not available." }

            // make sure user doesn't have access to club contexts any longer
            clear()

            return
        }

        clubContextsProvider.fetch(refresh = refresh)
    }

    /**
     * Clears all club contexts. Should be called after user logs out.
     */
    fun clear() {
        _clubContextsProvider.clear()
    }

    /**
     * Returns a [GroupHuntingClubContext] that matches the given [IdentifiesGroupHuntingClub].
     * Does not perform any asynchronous calls i.e. data must have already been loaded.
     */
    fun findClubContext(identifiesHuntingClub: IdentifiesGroupHuntingClub?): GroupHuntingClubContext? {
        return if (identifiesHuntingClub != null) {
            clubContexts.firstOrNull { it.club.id == identifiesHuntingClub.clubId }
        } else {
            null
        }
    }

    /**
     * Attempts to return a [GroupHuntingClubContext] that is identified by
     * the given [identifiesHuntingClub] and allowing e.g. network operations.
     *
     * Depending on the value of [allowCached] may return a context for an already fetched club
     * or fetch all the necessary data and then try to find the club from fetched data.
     */
    suspend fun fetchClubContext(identifiesHuntingClub: IdentifiesGroupHuntingClub?,
                                 allowCached: Boolean): GroupHuntingClubContext? {
        if (identifiesHuntingClub == null) {
            return null
        }

        findClubContext(identifiesHuntingClub)
            ?.letIf(allowCached) { clubContext ->
                return clubContext
            }

        checkAvailabilityAndFetchClubs()

        return findClubContext(identifiesHuntingClub)
    }

    /**
     * Returns a [GroupHuntingClubGroupContext] that is identified by the given
     * [identifiesClubAndGroup]. Does not perform any asynchronous calls i.e. data must
     * have already been loaded.
     */
    fun findHuntingGroupContext(identifiesClubAndGroup: IdentifiesHuntingGroup?):
            GroupHuntingClubGroupContext? {
        return if (identifiesClubAndGroup != null) {
            findClubContext(identifiesClubAndGroup)
                ?.findHuntingGroupContext(identifiesClubAndGroup)
        } else {
            null
        }
    }

    /**
     * Attempts to return a [GroupHuntingClubGroupContext] that is identified by
     * the given [identifiesClubAndGroup] and allowing e.g. network operations.
     *
     * Depending on the value of [allowCached] may return a context for an already fetched hunting
     * group or fetch all the necessary data and then try to find the hunting group from fetched data.
     */
    suspend fun fetchHuntingGroupContext(identifiesClubAndGroup: IdentifiesHuntingGroup?,
                                         allowCached: Boolean): GroupHuntingClubGroupContext? {
        return if (identifiesClubAndGroup != null) {
            fetchClubContext(identifiesClubAndGroup, allowCached)
                ?.findHuntingGroupContext(identifiesClubAndGroup)
        } else {
            null
        }
    }

    /**
     * Attempts to return a [GroupHuntingHarvest] that is identified by
     * the given [identifiesHarvest] and allowing e.g. network operations.
     *
     * Depending on the value of [allowCached] may return a context for an already fetched harvest
     * or fetch all the necessary data and then try to find the harvest from fetched data.
     */
    suspend fun fetchGroupHuntingHarvest(identifiesHarvest: IdentifiesGroupHuntingHarvest?,
                                         allowCached: Boolean):
            GroupHuntingHarvest? {
        return if (identifiesHarvest != null) {
            fetchHuntingGroupContext(identifiesHarvest, allowCached)
                ?.fetchHarvest(identifiesHarvest, allowCached)
        } else {
            null
        }
    }

    /**
     * Returns a [GroupHuntingDay] that is identified by the given [identifiesGroupHuntingDay].
     * Does not perform any asynchronous calls i.e. data must have already been loaded.
     */
    fun findGroupHuntingDay(identifiesGroupHuntingDay: IdentifiesGroupHuntingDay?):
            GroupHuntingDay? {
        return if (identifiesGroupHuntingDay != null) {
            findHuntingGroupContext(identifiesGroupHuntingDay)
                ?.findHuntingDay(identifiesGroupHuntingDay)
        } else {
            null
        }
    }

    suspend fun searchPersonByHunterNumber(hunterNumber: HunterNumber): PersonWithHunterNumberDTO? {
        val response = backendAPI.searchPersonByHunterNumber(hunterNumber)
        return response.transformSuccessData { _, responseDTO ->
            responseDTO.typed
        }
    }

    companion object {
        private val logger by getLogger(GroupHuntingContext::class)
    }
}
