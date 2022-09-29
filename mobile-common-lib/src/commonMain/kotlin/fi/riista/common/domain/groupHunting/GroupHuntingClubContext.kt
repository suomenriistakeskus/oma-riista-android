package fi.riista.common.domain.groupHunting

import co.touchlab.stately.collections.IsoMutableMap
import fi.riista.common.domain.groupHunting.model.HuntingGroup
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.groupHunting.model.IdentifiesHuntingGroup
import fi.riista.common.domain.model.Organization
import fi.riista.common.network.BackendApiProvider

/**
 * A group hunting context for a single club (e.g. Nokian Metsästysseura). Provides means for
 * obtaining a group hunting context for the identified hunting group group.
 */
@Suppress("unused")
class GroupHuntingClubContext internal constructor(
    private val backendApiProvider: BackendApiProvider,
    val club: Organization,
    internal val huntingGroups: List<HuntingGroup>,
) {
    private val huntingGroupContexts = IsoMutableMap<HuntingGroupId, GroupHuntingClubGroupContext>()

    fun getHuntingGroupContext(huntingGroup: HuntingGroup): GroupHuntingClubGroupContext {
        return huntingGroupContexts.getOrPut(huntingGroup.id) {
            GroupHuntingClubGroupContext(backendApiProvider, huntingGroup)
        }
    }

    fun findHuntingGroupContext(identifiesHuntingGroup: IdentifiesHuntingGroup): GroupHuntingClubGroupContext? {
        val huntingGroupId = identifiesHuntingGroup.huntingGroupId

        if (huntingGroupContexts.containsKey(huntingGroupId)) {
            return huntingGroupContexts[huntingGroupId]
        }

        return huntingGroups
            .firstOrNull { it.id == huntingGroupId }
            ?.let { huntingGroup ->
                getHuntingGroupContext(huntingGroup)
            }
    }
}
