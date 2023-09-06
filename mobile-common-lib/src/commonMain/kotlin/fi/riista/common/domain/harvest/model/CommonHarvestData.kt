package fi.riista.common.domain.harvest.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GreySealHuntingMethod
import fi.riista.common.domain.model.HarvestReportState
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.domain.model.SearchableOrganization
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.StateAcceptedToHarvestPermit
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Data needed when creating/editing new group hunting harvests.
 */
@Serializable
internal data class CommonHarvestData(
    val localId: Long?,
    val localUrl: String?, // Used as localId on iOS
    val id: CommonHarvestId?,
    val rev: Int?,
    val species: Species,
    val location: CommonLocation,
    val pointOfTime: LocalDateTime,
    val description: String?,
    val canEdit: Boolean,
    val modified: Boolean,
    val deleted: Boolean,
    val images: EntityImages,
    val specimens: List<CommonSpecimenData>,
    val amount: Int?,
    val huntingDayId: GroupHuntingDayId?,
    val authorInfo: PersonWithHunterNumber?,
    val actorInfo: GroupHuntingPerson,
    // club for which this harvest has been recorded / logged
    val selectedClub: SearchableOrganization,
    val harvestSpecVersion: Int,
    val harvestReportRequired: Boolean,
    val harvestReportState: BackendEnum<HarvestReportState>,
    val permitNumber: String?,
    val permitType: String?,
    val stateAcceptedToHarvestPermit: BackendEnum<StateAcceptedToHarvestPermit>,
    val deerHuntingType: BackendEnum<DeerHuntingType>,
    val deerHuntingOtherTypeDescription: String?,
    val mobileClientRefId: Long?,
    val harvestReportDone: Boolean,
    val rejected: Boolean,
    val feedingPlace: Boolean?,
    val taigaBeanGoose: Boolean?,
    val greySealHuntingMethod: BackendEnum<GreySealHuntingMethod>,
) {
    val acceptStatus: AcceptStatus
        get() {
            return if (rejected) {
                AcceptStatus.REJECTED
            } else {
                if (huntingDayId != null) {
                    AcceptStatus.ACCEPTED
                } else {
                    AcceptStatus.PROPOSED
                }
            }
        }

    val harvestState: HarvestState? by lazy {
        HarvestState.combinedState(
            harvestReportState = harvestReportState.value,
            stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit.value,
            harvestReportRequired = harvestReportRequired,
        )
    }

    val unknownGenderAllowed: Boolean
        get() {
            return species.knownSpeciesCodeOrNull() == SpeciesCodes.GREY_SEAL_ID &&
                    greySealHuntingMethod.value == GreySealHuntingMethod.SHOT_BUT_LOST
        }

    val unknownAgeAllowed: Boolean
        get() {
            return species.knownSpeciesCodeOrNull() == SpeciesCodes.GREY_SEAL_ID &&
                    greySealHuntingMethod.value == GreySealHuntingMethod.SHOT_BUT_LOST
        }

}

