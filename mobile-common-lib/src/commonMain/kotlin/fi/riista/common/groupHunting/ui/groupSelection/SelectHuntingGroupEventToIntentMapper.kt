package fi.riista.common.groupHunting.ui.groupSelection

import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.model.HuntingYear
import fi.riista.common.model.OrganizationId
import fi.riista.common.model.SpeciesCode
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.intent.IntentHandler

internal class SelectHuntingGroupEventToIntentMapper(
    val intentHandler: IntentHandler<SelectHuntingGroupIntent>
) : SelectHuntingGroupEventDispatcher {
    override fun dispatchStringWithIdChanged(fieldId: SelectHuntingGroupField, value: StringWithId) {
        val intent = when (fieldId) {
            SelectHuntingGroupField.HUNTING_CLUB ->
                SelectHuntingGroupIntent.SelectClub(value.id)
            SelectHuntingGroupField.SEASON ->
                SelectHuntingGroupIntent.SelectSeason(value.id.toInt())
            SelectHuntingGroupField.SPECIES ->
                SelectHuntingGroupIntent.SelectSpecies(value.id.toInt())
            SelectHuntingGroupField.HUNTING_GROUP ->
                SelectHuntingGroupIntent.SelectHuntingGroup(value.id)

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