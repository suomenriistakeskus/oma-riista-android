package fi.riista.common.domain.harvest.model

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.asGroupMember
import fi.riista.common.domain.groupHunting.model.toGroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.toCommonHarvestData
import fi.riista.common.domain.model.*
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GroupHuntingHarvestDataTest {

    @Test
    fun testHarvestDataHasSameValuesAsHarvest() {
        val harvest = GroupHuntingHarvest(
            id = 1,
            rev = 2,
            gameSpeciesCode = SpeciesCodes.MOOSE_ID,
            geoLocation = ETRMSGeoLocation(
                30, 20, BackendEnum.create(GeoLocationSource.GPS_DEVICE),
                40.0, 50.0, 60.0
            ),
            pointOfTime = LocalDateTime(2021, 6, 7, 16, 49, 37),
            description = "description",
            canEdit = true,
            imageIds = listOf("image_1"),
            specimens = listOf(
                CommonHarvestSpecimen(
                    id = 7,
                    rev = null,
                    gender = BackendEnum.create(null),
                    age = BackendEnum.create(null),
                    antlersLost = null,
                    notEdible = null,
                    alone = null,
                    weightEstimated = null,
                    weightMeasured = null,
                    fitnessClass = BackendEnum.create(null),
                    antlersType = BackendEnum.create(null),
                    antlersWidth = null,
                    antlerPointsLeft = null,
                    antlerPointsRight = null,
                    antlersGirth = null,
                    antlersLength = null,
                    antlersInnerWidth = null,
                    antlerShaftWidth = null,
                    additionalInfo = null,
                    weight = null,
                )
            ),
            amount = 1,
            huntingDayId = GroupHuntingDayId.remote(2),
            authorInfo = PersonWithHunterNumber(
                1, 0,
                "Pentti0", "Mujunen", "12345678"
            ),
            actorInfo = PersonWithHunterNumber(
                2, 1,
                "Pertti", "Möjönen", "23456789"
            ),
            harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
            harvestReportRequired = true,
            harvestReportState = BackendEnum.create(HarvestReportState.APPROVED),
            permitNumber = "1234",
            permitType = "permit",
            stateAcceptedToHarvestPermit = BackendEnum.create(StateAcceptedToHarvestPermit.ACCEPTED),
            deerHuntingType = BackendEnum.create(DeerHuntingType.OTHER),
            deerHuntingOtherTypeDescription = "ritsalla ammuttu",
            mobileClientRefId = 99,
            harvestReportDone = true,
            rejected = true,
        )

        val data = harvest.toCommonHarvestData(groupMembers = listOf())

        assertEquals(harvest.id, data.id)
        assertEquals(harvest.rev, data.rev)
        assertEquals(harvest.gameSpeciesCode, data.species.knownSpeciesCodeOrNull())
        assertEquals(harvest.geoLocation, data.location.etrsLocationOrNull)
        assertEquals(harvest.pointOfTime, data.pointOfTime)
        assertEquals(harvest.description, data.description)
        assertEquals(harvest.canEdit, data.canEdit)
        assertEquals(harvest.imageIds, data.images.remoteImageIds)
        assertEquals(harvest.specimens, data.specimens.map { it.toCommonHarvestSpecimen() })
        assertEquals(harvest.amount, data.amount)
        assertEquals(harvest.huntingDayId, data.huntingDayId)
        assertEquals(harvest.authorInfo, data.authorInfo)
        assertEquals(harvest.actorInfo, data.actorInfo.personWithHunterNumber)
        assertEquals(harvest.harvestSpecVersion, data.harvestSpecVersion)
        assertEquals(harvest.harvestReportRequired, data.harvestReportRequired)
        assertEquals(harvest.harvestReportState, data.harvestReportState)
        assertEquals(harvest.permitNumber, data.permitNumber)
        assertEquals(harvest.permitType, data.permitType)
        assertEquals(harvest.stateAcceptedToHarvestPermit, data.stateAcceptedToHarvestPermit)
        assertEquals(harvest.deerHuntingType, data.deerHuntingType)
        assertEquals(harvest.deerHuntingOtherTypeDescription, data.deerHuntingOtherTypeDescription)
        assertEquals(harvest.mobileClientRefId, data.mobileClientRefId)
        assertEquals(harvest.harvestReportDone, data.harvestReportDone)
        assertEquals(harvest.rejected, data.rejected)
    }

    @Test
    fun testHarvestHasSameValuesAsHarvestData() {
        val data = CommonHarvestData(
            localId = null,
            localUrl = null,
            id = 1,
            rev = 2,
            species = Species.Known(SpeciesCodes.MOOSE_ID),
            location = ETRMSGeoLocation(
                30, 20, BackendEnum.create(GeoLocationSource.GPS_DEVICE),
                40.0, 50.0, 60.0
            ).asKnownLocation(),
            pointOfTime = LocalDateTime(2021, 6, 7, 16, 49, 37),
            description = "description",
            canEdit = true,
            modified = true,
            deleted = false,
            images = EntityImages(listOf("image_1"), listOf()),
            specimens = listOf(
                CommonSpecimenData.createForTests(remoteId = 7)
            ),
            amount = 1,
            huntingDayId = GroupHuntingDayId.remote(2),
            authorInfo = PersonWithHunterNumber(
                1, 0,
                "Pentti0", "Mujunen", "12345678"
            ),
            actorInfo = PersonWithHunterNumber(
                2, 1,
                "Pertti", "Möjönen", "23456789"
            ).asGroupMember(),
            selectedClub = SearchableOrganization.Unknown,
            harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
            harvestReportRequired = true,
            harvestReportState = BackendEnum.create(HarvestReportState.APPROVED),
            permitNumber = "1234",
            permitType = "permit",
            stateAcceptedToHarvestPermit = BackendEnum.create(StateAcceptedToHarvestPermit.ACCEPTED),
            deerHuntingType = BackendEnum.create(DeerHuntingType.OTHER),
            deerHuntingOtherTypeDescription = "ritsalla ammuttu",
            mobileClientRefId = 99,
            harvestReportDone = true,
            rejected = false,
            feedingPlace = null,
            taigaBeanGoose = null,
            greySealHuntingMethod = BackendEnum.create(null),
        )

        val harvest = data.toGroupHuntingHarvest()!!

        assertEquals(data.id, harvest.id)
        assertEquals(data.rev, harvest.rev)
        assertEquals(data.species.knownSpeciesCodeOrNull(), harvest.gameSpeciesCode)
        assertEquals(data.location, harvest.geoLocation.asKnownLocation())
        assertEquals(data.pointOfTime, harvest.pointOfTime)
        assertEquals(data.description, harvest.description)
        assertEquals(data.canEdit, harvest.canEdit)
        assertEquals(data.images.remoteImageIds, harvest.imageIds)
        assertEquals(data.specimens, harvest.specimens.map { it.toCommonSpecimenData() })
        assertEquals(data.amount, harvest.amount)
        assertEquals(data.huntingDayId, harvest.huntingDayId)
        assertEquals(data.authorInfo, harvest.authorInfo)
        assertEquals(data.actorInfo.personWithHunterNumber, harvest.actorInfo)
        assertEquals(data.harvestSpecVersion, harvest.harvestSpecVersion)
        assertEquals(data.harvestReportRequired, harvest.harvestReportRequired)
        assertEquals(data.harvestReportState, harvest.harvestReportState)
        assertEquals(data.permitNumber, harvest.permitNumber)
        assertEquals(data.permitType, harvest.permitType)
        assertEquals(data.stateAcceptedToHarvestPermit, harvest.stateAcceptedToHarvestPermit)
        assertEquals(data.deerHuntingType, harvest.deerHuntingType)
        assertEquals(data.deerHuntingOtherTypeDescription, harvest.deerHuntingOtherTypeDescription)
        assertEquals(data.mobileClientRefId, harvest.mobileClientRefId)
        assertEquals(data.harvestReportDone, harvest.harvestReportDone)
        assertEquals(data.rejected, harvest.rejected)
    }

    @Test
    fun testInvalidDataWontProduceHarvest() {
        val originalData = CommonHarvestData(
            localId = null,
            localUrl = null,
            id = 1,
            rev = 2,
            species = Species.Known(SpeciesCodes.MOOSE_ID),
            location = ETRMSGeoLocation(
                30, 20, BackendEnum.create(GeoLocationSource.GPS_DEVICE),
                40.0, 50.0, 60.0
            ).asKnownLocation(),
            pointOfTime = LocalDateTime(2021, 6, 7, 16, 49, 37),
            description = "description",
            canEdit = true,
            modified = true,
            deleted = false,
            images = EntityImages(listOf("image_1"), listOf()),
            specimens = listOf(
                CommonSpecimenData.createForTests(remoteId = 7)
            ),
            amount = 1,
            huntingDayId = GroupHuntingDayId.remote(2),
            authorInfo = PersonWithHunterNumber(
                1, 0,
                "Pentti0", "Mujunen", "12345678"
            ),
            actorInfo = PersonWithHunterNumber(
                2, 1,
                "Pertti", "Möjönen", "23456789"
            ).asGroupMember(),
            selectedClub = SearchableOrganization.Unknown,
            harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
            harvestReportRequired = true,
            harvestReportState = BackendEnum.create(HarvestReportState.APPROVED),
            permitNumber = "1234",
            permitType = "permit",
            stateAcceptedToHarvestPermit = BackendEnum.create(StateAcceptedToHarvestPermit.ACCEPTED),
            deerHuntingType = BackendEnum.create(DeerHuntingType.OTHER),
            deerHuntingOtherTypeDescription = "ritsalla ammuttu",
            mobileClientRefId = 99,
            harvestReportDone = true,
            rejected = false,
            feedingPlace = null,
            taigaBeanGoose = null,
            greySealHuntingMethod = BackendEnum.create(null),
        )

        // should produce valid harvest
        assertNotNull(originalData.toGroupHuntingHarvest())

        assertNull(originalData.copy(id = null).toGroupHuntingHarvest())
        assertNull(originalData.copy(rev = null).toGroupHuntingHarvest())
        assertNull(originalData.copy(authorInfo = null).toGroupHuntingHarvest())
        assertNull(originalData.copy(actorInfo = GroupHuntingPerson.Unknown).toGroupHuntingHarvest())
        assertNull(originalData.copy(actorInfo = GroupHuntingPerson.SearchingByHunterNumber.startSearch()).toGroupHuntingHarvest())
    }
}
