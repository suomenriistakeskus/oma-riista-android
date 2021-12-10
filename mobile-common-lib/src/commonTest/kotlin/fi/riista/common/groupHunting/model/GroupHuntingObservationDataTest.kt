package fi.riista.common.groupHunting.model

import fi.riista.common.model.*
import kotlin.test.Test
import kotlin.test.assertEquals

class GroupHuntingObservationDataTest {

    @Test
    fun testObservationDataHasSameValuesAsObservation() {
        val observation = GroupHuntingObservation(
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
                ObservationSpecimen(id = 5, rev = 2)
            ) ,
            amount = 5,
            huntingDayId = GroupHuntingDayId.remote(5),
            authorInfo = PersonWithHunterNumber(
                1, 0,
                "Pentti0", "Mujunen", "12345678"
            ),
            actorInfo = PersonWithHunterNumber(
                2, 1,
                "Pertti", "Möjönen", "23456789"
            ),
            observerName = "observer",
            observerPhoneNumber = "1232456",
            linkedToGroupHuntingDay = true,
            observationType = ObservationType.NAKO.toBackendEnum(),
            observationCategory = ObservationCategory.MOOSE_HUNTING.toBackendEnum(),
            totalSpecimenAmount = 5,
            mobileClientRefId = 666,
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            litter = true,
            pack = true,
            deerHuntingType =DeerHuntingType.STAND_HUNTING.toBackendEnum(),
            deerHuntingTypeDescription = "deerHuntingType",
            mooselikeMaleAmount = 1,
            mooselikeFemaleAmount = 2,
            mooselikeCalfAmount = 3,
            mooselikeFemale1CalfAmount = 4,
            mooselikeFemale2CalfsAmount = 5,
            mooselikeFemale3CalfsAmount = 6,
            mooselikeFemale4CalfsAmount = 7,
            mooselikeUnknownSpecimenAmount = 8,
            inYardDistanceToResidence = 15,
            verifiedByCarnivoreAuthority = true,
            officialAdditionalInfo = "additionalInfo",
            rejected = true,
        )

        val data = observation.toGroupHuntingObservationData(groupMembers = listOf())

        assertEquals(observation.id, data.id)
        assertEquals(observation.rev, data.rev)
        assertEquals(observation.gameSpeciesCode, data.gameSpeciesCode)
        assertEquals(observation.geoLocation, data.geoLocation)
        assertEquals(observation.pointOfTime, data.pointOfTime)
        assertEquals(observation.description, data.description)
        assertEquals(observation.canEdit, data.canEdit)
        assertEquals(observation.imageIds, data.imageIds)
        assertEquals(observation.specimens, data.specimens)
        assertEquals(observation.amount, data.amount)
        assertEquals(observation.huntingDayId, data.huntingDayId)
        assertEquals(observation.authorInfo, data.authorInfo)
        assertEquals(observation.actorInfo, data.actorInfo?.personWithHunterNumber)
        assertEquals(observation.observerName, data.observerName)
        assertEquals(observation.observerPhoneNumber, data.observerPhoneNumber)
        assertEquals(observation.linkedToGroupHuntingDay, data.linkedToGroupHuntingDay)
        assertEquals(observation.observationType, data.observationType)
        assertEquals(observation.observationCategory, data.observationCategory)
        assertEquals(observation.totalSpecimenAmount, data.totalSpecimenAmount)
        assertEquals(observation.mobileClientRefId, data.mobileClientRefId)
        assertEquals(observation.observationSpecVersion, data.observationSpecVersion)
        assertEquals(observation.litter, data.litter)
        assertEquals(observation.pack, data.pack)
        assertEquals(observation.deerHuntingType, data.deerHuntingType)
        assertEquals(observation.deerHuntingTypeDescription, data.deerHuntingTypeDescription)
        assertEquals(observation.mooselikeMaleAmount, data.mooselikeMaleAmount)
        assertEquals(observation.mooselikeFemaleAmount, data.mooselikeFemaleAmount)
        assertEquals(observation.mooselikeCalfAmount, data.mooselikeCalfAmount)
        assertEquals(observation.mooselikeFemale1CalfAmount, data.mooselikeFemale1CalfAmount)
        assertEquals(observation.mooselikeFemale2CalfsAmount, data.mooselikeFemale2CalfsAmount)
        assertEquals(observation.mooselikeFemale3CalfsAmount, data.mooselikeFemale3CalfsAmount)
        assertEquals(observation.mooselikeFemale4CalfsAmount, data.mooselikeFemale4CalfsAmount)
        assertEquals(observation.mooselikeUnknownSpecimenAmount, data.mooselikeUnknownSpecimenAmount)
        assertEquals(observation.inYardDistanceToResidence, data.inYardDistanceToResidence)
        assertEquals(observation.verifiedByCarnivoreAuthority, data.verifiedByCarnivoreAuthority)
        assertEquals(observation.officialAdditionalInfo, data.officialAdditionalInfo)
        assertEquals(observation.rejected, data.rejected)
    }

    @Test
    fun testObservationHasSameValuesAsObservationData() {
        val data = GroupHuntingObservationData(
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
                ObservationSpecimen(id = 5, rev = 2)
            ) ,
            amount = 5,
            huntingDayId = GroupHuntingDayId.remote(5),
            authorInfo = PersonWithHunterNumber(
                1, 0,
                "Pentti0", "Mujunen", "12345678"
            ),
            actorInfo = PersonWithHunterNumber(
                2, 1,
                "Pertti", "Möjönen", "23456789"
            ).asGroupMember(),
            observerName = "observer",
            observerPhoneNumber = "1232456",
            linkedToGroupHuntingDay = true,
            observationType = ObservationType.NAKO.toBackendEnum(),
            observationCategory = ObservationCategory.MOOSE_HUNTING.toBackendEnum(),
            totalSpecimenAmount = 5,
            mobileClientRefId = 666,
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            litter = true,
            pack = true,
            deerHuntingType =DeerHuntingType.STAND_HUNTING.toBackendEnum(),
            deerHuntingTypeDescription = "deerHuntingType",
            mooselikeMaleAmount = 1,
            mooselikeFemaleAmount = 2,
            mooselikeCalfAmount = 3,
            mooselikeFemale1CalfAmount = 4,
            mooselikeFemale2CalfsAmount = 5,
            mooselikeFemale3CalfsAmount = 6,
            mooselikeFemale4CalfsAmount = 7,
            mooselikeUnknownSpecimenAmount = 8,
            inYardDistanceToResidence = 15,
            verifiedByCarnivoreAuthority = true,
            officialAdditionalInfo = "additionalInfo",
            rejected = false,
        )

        val observation = data.toGroupHuntingObservation()!!

        assertEquals(data.id, observation.id)
        assertEquals(data.rev, observation.rev)
        assertEquals(data.gameSpeciesCode, observation.gameSpeciesCode)
        assertEquals(data.geoLocation, observation.geoLocation)
        assertEquals(data.pointOfTime, observation.pointOfTime)
        assertEquals(data.description, observation.description)
        assertEquals(data.canEdit, observation.canEdit)
        assertEquals(data.imageIds, observation.imageIds)
        assertEquals(data.specimens, observation.specimens)
        assertEquals(data.amount, observation.amount)
        assertEquals(data.huntingDayId, observation.huntingDayId)
        assertEquals(data.authorInfo, observation.authorInfo)
        assertEquals(data.actorInfo.personWithHunterNumber, observation.actorInfo)
        assertEquals(data.observerName, observation.observerName)
        assertEquals(data.observerPhoneNumber, observation.observerPhoneNumber)
        assertEquals(data.linkedToGroupHuntingDay, observation.linkedToGroupHuntingDay)
        assertEquals(data.observationType, observation.observationType)
        assertEquals(data.observationCategory, observation.observationCategory)
        assertEquals(data.totalSpecimenAmount, observation.totalSpecimenAmount)
        assertEquals(data.mobileClientRefId, observation.mobileClientRefId)
        assertEquals(data.observationSpecVersion, observation.observationSpecVersion)
        assertEquals(data.litter, observation.litter)
        assertEquals(data.pack, observation.pack)
        assertEquals(data.deerHuntingType, observation.deerHuntingType)
        assertEquals(data.deerHuntingTypeDescription, observation.deerHuntingTypeDescription)
        assertEquals(data.mooselikeMaleAmount, observation.mooselikeMaleAmount)
        assertEquals(data.mooselikeFemaleAmount, observation.mooselikeFemaleAmount)
        assertEquals(data.mooselikeCalfAmount, observation.mooselikeCalfAmount)
        assertEquals(data.mooselikeFemale1CalfAmount, observation.mooselikeFemale1CalfAmount)
        assertEquals(data.mooselikeFemale2CalfsAmount, observation.mooselikeFemale2CalfsAmount)
        assertEquals(data.mooselikeFemale3CalfsAmount, observation.mooselikeFemale3CalfsAmount)
        assertEquals(data.mooselikeFemale4CalfsAmount, observation.mooselikeFemale4CalfsAmount)
        assertEquals(data.mooselikeUnknownSpecimenAmount, observation.mooselikeUnknownSpecimenAmount)
        assertEquals(data.inYardDistanceToResidence, observation.inYardDistanceToResidence)
        assertEquals(data.verifiedByCarnivoreAuthority, observation.verifiedByCarnivoreAuthority)
        assertEquals(data.officialAdditionalInfo, observation.officialAdditionalInfo)
        assertEquals(data.rejected, observation.rejected)
    }

}
