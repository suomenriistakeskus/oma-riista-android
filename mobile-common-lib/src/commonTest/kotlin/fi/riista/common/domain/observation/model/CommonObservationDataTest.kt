package fi.riista.common.domain.observation.model

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.generateMobileClientRefId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CommonObservationDataTest {

    @Test
    fun testObservationHasSameValuesAsObservationData() {
        val data = createValidObservationData()

        val observation = data.toCommonObservation()!!

        assertEquals(observation.localId, data.localId)
        assertEquals(observation.localUrl, data.localUrl)
        assertEquals(observation.remoteId, data.remoteId)
        assertEquals(observation.revision, data.revision)
        assertEquals(observation.mobileClientRefId, data.mobileClientRefId)
        assertEquals(observation.observationSpecVersion, data.observationSpecVersion)
        assertEquals(observation.species, data.species)
        assertEquals(observation.observationCategory, data.observationCategory)
        assertEquals(observation.observationType, data.observationType)
        assertEquals(observation.deerHuntingType, data.deerHuntingType)
        assertEquals(observation.deerHuntingOtherTypeDescription, data.deerHuntingOtherTypeDescription)
        assertEquals(observation.location, data.location.etrsLocationOrNull)
        assertEquals(observation.pointOfTime, data.pointOfTime)
        assertEquals(observation.description, data.description)
        assertEquals(observation.images, data.images)
        assertEquals(observation.totalSpecimenAmount, data.totalSpecimenAmount)
        assertEquals(observation.specimens, data.specimens?.map { it.toObservationSpecimen() })
        assertEquals(createSpecimenData(remoteId = 1), data.specimens!![0])
        assertEquals(observation.canEdit, data.canEdit)
        assertEquals(observation.modified, data.modified)
        assertEquals(observation.deleted, data.deleted)
        assertEquals(observation.mooselikeMaleAmount, data.mooselikeMaleAmount)
        assertEquals(observation.mooselikeFemaleAmount, data.mooselikeFemaleAmount)
        assertEquals(observation.mooselikeFemale1CalfAmount, data.mooselikeFemale1CalfAmount)
        assertEquals(observation.mooselikeFemale2CalfsAmount, data.mooselikeFemale2CalfsAmount)
        assertEquals(observation.mooselikeFemale3CalfsAmount, data.mooselikeFemale3CalfsAmount)
        assertEquals(observation.mooselikeFemale4CalfsAmount, data.mooselikeFemale4CalfsAmount)
        assertEquals(observation.mooselikeCalfAmount, data.mooselikeCalfAmount)
        assertEquals(observation.mooselikeUnknownSpecimenAmount, data.mooselikeUnknownSpecimenAmount)
        assertEquals(observation.observerName, data.observerName)
        assertEquals(observation.observerPhoneNumber, data.observerPhoneNumber)
        assertEquals(observation.officialAdditionalInfo, data.officialAdditionalInfo)
        assertEquals(observation.verifiedByCarnivoreAuthority, data.verifiedByCarnivoreAuthority)
        assertEquals(observation.inYardDistanceToResidence, data.inYardDistanceToResidence)
        assertEquals(observation.litter, data.litter)
        assertEquals(observation.pack, data.pack)
    }

    @Test
    fun testConversionBackAndForth() {
        val originalData = createValidObservationData()

        val observation = originalData.toCommonObservation()
        assertNotNull(observation)

        val newData = observation.toObservationData()
        assertNotNull(newData)

        assertEquals(originalData, newData)
    }

    @Test
    fun testInvalidDataWontProduceObservation() {
        val originalData = createValidObservationData()

        // should produce valid observation
        assertNotNull(originalData.toCommonObservation())

        assertNull(originalData.copy(location = CommonLocation.Unknown).toCommonObservation())
    }

    private fun createValidObservationData(): CommonObservationData {
        return CommonObservationData(
            localId = 1,
            localUrl = "1",
            remoteId = 2,
            revision = 3,
            mobileClientRefId = generateMobileClientRefId(),
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            species = Species.Unknown,
            observationCategory = BackendEnum.create(ObservationCategory.MOOSE_HUNTING),
            observationType = BackendEnum.create(ObservationType.NAKO),
            deerHuntingType = BackendEnum.create(DeerHuntingType.OTHER),
            deerHuntingOtherTypeDescription = "other",
            location = ETRMSGeoLocation(
                30, 20, BackendEnum.create(GeoLocationSource.GPS_DEVICE),
                40.0, 50.0, 60.0
            ).asKnownLocation(),
            pointOfTime = LocalDateTime(2022, 1, 1, 12, 0, 0),
            description = "description",
            images = EntityImages.noImages(),
            totalSpecimenAmount = 1,
            specimens = listOf(
                createSpecimenData(remoteId = 1)
            ),
            canEdit = true,
            modified = true,
            deleted = false,
            mooselikeMaleAmount = 1,
            mooselikeFemaleAmount = 2,
            mooselikeFemale1CalfAmount = 3,
            mooselikeFemale2CalfsAmount = 4,
            mooselikeFemale3CalfsAmount = 5,
            mooselikeFemale4CalfsAmount = 6,
            mooselikeCalfAmount = 7,
            mooselikeUnknownSpecimenAmount = 8,
            observerName = "Pena",
            observerPhoneNumber = "123",
            officialAdditionalInfo = "nope",
            verifiedByCarnivoreAuthority = true,
            inYardDistanceToResidence = 23,
            litter = false,
            pack = true,
        )
    }

    private fun createSpecimenData(remoteId: Long) =
        CommonSpecimenData(
            remoteId = remoteId,
            revision = 2,
            gender = Gender.FEMALE.toBackendEnum(),
            age = GameAge.ADULT.toBackendEnum(),
            stateOfHealth = ObservationSpecimenState.HEALTHY.toBackendEnum(),
            marking = ObservationSpecimenMarking.EARMARK.toBackendEnum(),
            lengthOfPaw = 3.0,
            widthOfPaw = 4.5,
            weight = null,
            weightEstimated = null,
            weightMeasured = null,
            fitnessClass = null,
            antlersLost = null,
            antlersType = null,
            antlersWidth = null,
            antlerPointsLeft = null,
            antlerPointsRight = null,
            antlersGirth = null,
            antlersLength = null,
            antlersInnerWidth = null,
            antlerShaftWidth = null,
            notEdible = null,
            alone = null,
            additionalInfo = null,
        )
}
