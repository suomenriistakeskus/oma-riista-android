package fi.riista.common.groupHunting.model

import fi.riista.common.model.*
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
                        HarvestSpecimen(id = 7)
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
                stateAcceptedToHarvestPermit = BackendEnum.create(StateAcceptedToHarvestPermit.ACCEPTED),
                deerHuntingType = BackendEnum.create(DeerHuntingType.OTHER),
                deerHuntingOtherTypeDescription = "ritsalla ammuttu",
                mobileClientRefId = 99,
                harvestReportDone = true,
                rejected = true,
        )

        val data = harvest.toGroupHuntingHarvestData(groupMembers = listOf())

        assertEquals(harvest.id, data.id)
        assertEquals(harvest.rev, data.rev)
        assertEquals(harvest.gameSpeciesCode, data.gameSpeciesCode)
        assertEquals(harvest.geoLocation, data.geoLocation)
        assertEquals(harvest.pointOfTime, data.pointOfTime)
        assertEquals(harvest.description, data.description)
        assertEquals(harvest.canEdit, data.canEdit)
        assertEquals(harvest.imageIds, data.imageIds)
        assertEquals(harvest.specimens, data.specimens)
        assertEquals(harvest.amount, data.amount)
        assertEquals(harvest.huntingDayId, data.huntingDayId)
        assertEquals(harvest.authorInfo, data.authorInfo)
        assertEquals(harvest.actorInfo, data.actorInfo.personWithHunterNumber)
        assertEquals(harvest.harvestSpecVersion, data.harvestSpecVersion)
        assertEquals(harvest.harvestReportRequired, data.harvestReportRequired)
        assertEquals(harvest.harvestReportState, data.harvestReportState)
        assertEquals(harvest.permitNumber, data.permitNumber)
        assertEquals(harvest.stateAcceptedToHarvestPermit, data.stateAcceptedToHarvestPermit)
        assertEquals(harvest.deerHuntingType, data.deerHuntingType)
        assertEquals(harvest.deerHuntingOtherTypeDescription, data.deerHuntingOtherTypeDescription)
        assertEquals(harvest.mobileClientRefId, data.mobileClientRefId)
        assertEquals(harvest.harvestReportDone, data.harvestReportDone)
        assertEquals(harvest.rejected, data.rejected)
    }

    @Test
    fun testHarvestHasSameValuesAsHarvestData() {
        val data = GroupHuntingHarvestData(
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
                        HarvestSpecimen(id = 7)
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
                harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
                harvestReportRequired = true,
                harvestReportState = BackendEnum.create(HarvestReportState.APPROVED),
                permitNumber = "1234",
                stateAcceptedToHarvestPermit = BackendEnum.create(StateAcceptedToHarvestPermit.ACCEPTED),
                deerHuntingType = BackendEnum.create(DeerHuntingType.OTHER),
                deerHuntingOtherTypeDescription = "ritsalla ammuttu",
                mobileClientRefId = 99,
                harvestReportDone = true,
                rejected = false,
        )

        val harvest = data.toGroupHuntingHarvest()!!

        assertEquals(data.id, harvest.id)
        assertEquals(data.rev, harvest.rev)
        assertEquals(data.gameSpeciesCode, harvest.gameSpeciesCode)
        assertEquals(data.geoLocation, harvest.geoLocation)
        assertEquals(data.pointOfTime, harvest.pointOfTime)
        assertEquals(data.description, harvest.description)
        assertEquals(data.canEdit, harvest.canEdit)
        assertEquals(data.imageIds, harvest.imageIds)
        assertEquals(data.specimens, harvest.specimens)
        assertEquals(data.amount, harvest.amount)
        assertEquals(data.huntingDayId, harvest.huntingDayId)
        assertEquals(data.authorInfo, harvest.authorInfo)
        assertEquals(data.actorInfo.personWithHunterNumber, harvest.actorInfo)
        assertEquals(data.harvestSpecVersion, harvest.harvestSpecVersion)
        assertEquals(data.harvestReportRequired, harvest.harvestReportRequired)
        assertEquals(data.harvestReportState, harvest.harvestReportState)
        assertEquals(data.permitNumber, harvest.permitNumber)
        assertEquals(data.stateAcceptedToHarvestPermit, harvest.stateAcceptedToHarvestPermit)
        assertEquals(data.deerHuntingType, harvest.deerHuntingType)
        assertEquals(data.deerHuntingOtherTypeDescription, harvest.deerHuntingOtherTypeDescription)
        assertEquals(data.mobileClientRefId, harvest.mobileClientRefId)
        assertEquals(data.harvestReportDone, harvest.harvestReportDone)
        assertEquals(data.rejected, harvest.rejected)
    }

    @Test
    fun testInvalidDataWontProduceHarvest() {
        val originalData = GroupHuntingHarvestData(
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
                        HarvestSpecimen(id = 7)
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
                harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
                harvestReportRequired = true,
                harvestReportState = BackendEnum.create(HarvestReportState.APPROVED),
                permitNumber = "1234",
                stateAcceptedToHarvestPermit = BackendEnum.create(StateAcceptedToHarvestPermit.ACCEPTED),
                deerHuntingType = BackendEnum.create(DeerHuntingType.OTHER),
                deerHuntingOtherTypeDescription = "ritsalla ammuttu",
                mobileClientRefId = 99,
                harvestReportDone = true,
                rejected = false,
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
