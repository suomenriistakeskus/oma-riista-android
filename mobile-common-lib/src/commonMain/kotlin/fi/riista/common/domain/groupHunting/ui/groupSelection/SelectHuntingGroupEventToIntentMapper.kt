package fi.riista.common.domain.groupHunting.ui.groupSelection

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.intent.IntentHandler

internal class SelectHuntingGroupEventToIntentMapper(
    val intentHandler: IntentHandler<SelectHuntingGroupIntent>
) : SelectHuntingGroupEventDispatcher {
    override fun dispatchStringWithIdChanged(fieldId: SelectHuntingGroupField, value: List<StringWithId>) {
        if (value.size != 1) {
            throw RuntimeException("Wrong number of values for field $fieldId (newValue: $value)")
        }

        val intent = when (fieldId) {
            SelectHuntingGroupField.HUNTING_CLUB ->
                SelectHuntingGroupIntent.SelectClub(value[0].id)
            SelectHuntingGroupField.SEASON ->
                SelectHuntingGroupIntent.SelectSeason(value[0].id.toInt())
            SelectHuntingGroupField.SPECIES ->
                SelectHuntingGroupIntent.SelectSpecies(value[0].id.toInt())
            SelectHuntingGroupField.HUNTING_GROUP ->
                SelectHuntingGroupIntent.SelectHuntingGroup(value[0].id)

            else -> throw RuntimeException("Didn't expect $fieldId with StringWithId param.")
        }

        intentHandler.handleIntent(intent)
    }
}

sealed class SelectHuntingGroupIntent {
    class SelectClub(val clubId: OrganizationId): SelectHuntingGroupIntent()
    class SelectSeason(val huntingYear: HuntingYear): SelectHuntingGroupIntent()
    class SelectSpecies(val speciesCode: SpeciesCode): SelectHuntingGroupIntent()
    class SelectHuntingGroup(val huntingGroupId: HuntingGroupId): SelectHuntingGroupIntent()
}
