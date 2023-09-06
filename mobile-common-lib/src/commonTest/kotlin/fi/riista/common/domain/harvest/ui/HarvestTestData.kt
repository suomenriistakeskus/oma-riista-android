package fi.riista.common.domain.harvest.ui

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.HarvestReportState
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.StateAcceptedToHarvestPermit
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermit
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermitSpeciesAmount
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.util.MockDateTimeProvider
import kotlinx.coroutines.runBlocking

internal object HarvestTestData {
    const val HARVEST_SPECIES_CODE = SpeciesCodes.BEAR_ID

    val HARVEST_LOCATION = ETRMSGeoLocation(
        latitude = 6789568,
        longitude = 330224,
        source = BackendEnum.create(GeoLocationSource.MANUAL),
        accuracy = 1.2,
        altitude = null,
        altitudeAccuracy = null,
    )

    val HARVEST_DATE_TIME = LocalDateTime(2022, 5, 1, 18, 0, 0)

    val MOCK_USER_INFO = UserInfoDTO(
        username = "user",
        personId = 123L,
        firstName = "user_first",
        lastName = "user_last",
        birthDate = null,
        address = null,
        homeMunicipality = LocalizedStringDTO(null, null, null),
        rhy = null,
        hunterNumber = null,
        hunterExamDate = null,
        huntingCardStart = null,
        huntingCardEnd = null,
        huntingBanStart = null,
        huntingBanEnd = null,
        huntingCardValidNow = true,
        qrCode = null,
        timestamp = "2022-01-01",
        shootingTests = emptyList(),
        occupations = emptyList(),
        enableSrva = true,
        enableShootingTests = false,
        deerPilotUser = true,
    )

    val PERMIT = CommonHarvestPermit(
        permitNumber = "1234",
        permitType = "permit",
        speciesAmounts = listOf(
            CommonHarvestPermitSpeciesAmount(
                speciesCode = SpeciesCodes.BEAR_ID,
                validityPeriods = listOf(
                    LocalDatePeriod(
                        beginDate = LocalDate(2014, 1, 1),
                        endDate = LocalDate(2024, 12, 31),
                    )
                ),
                amount = 1.0,
                ageRequired = false,
                genderRequired = false,
                weightRequired = false,
            ),
        ),
        available = true,
    )

    fun getHarvestContext(backendAPIMock: BackendAPIMock): HarvestContext {
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())

        val mockUserContextProvider = CurrentUserContextProviderFactory.createMocked()
        runBlocking {
            mockUserContextProvider.userLoggedIn(MOCK_USER_INFO)
        }

        return HarvestContext(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPIMock
            },
            preferences = MockPreferences(),
            localDateTimeProvider = MockDateTimeProvider(),
            commonFileProvider = CommonFileProviderMock(),
            database = database,
            currentUserContextProvider = mockUserContextProvider,
        )
    }

    fun createHarvest(speciesCode: SpeciesCode = SpeciesCodes.BEAR_ID) =
        CommonHarvest(
            localId = null,
            localUrl = null,
            id = 2,
            rev = 1,
            species = Species.Known(speciesCode = speciesCode),
            geoLocation = HARVEST_LOCATION,
            pointOfTime = HARVEST_DATE_TIME,
            description = "",
            canEdit = true,
            modified = false,
            deleted = false,
            images = EntityImages.noImages(),
            specimens = listOf(
                CommonHarvestSpecimen(
                    id = 1,
                    rev = 1,
                    gender = Gender.FEMALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                    weight = 123.4,
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
                )
            ),
            amount = 1,
            harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
            harvestReportRequired = true,
            harvestReportState = HarvestReportState.SENT_FOR_APPROVAL.toBackendEnum(),
            permitNumber = "123",
            permitType = "permit",
            stateAcceptedToHarvestPermit = StateAcceptedToHarvestPermit.PROPOSED.toBackendEnum(),
            deerHuntingType = null.toBackendEnum(),
            deerHuntingOtherTypeDescription = null,
            mobileClientRefId = null,
            harvestReportDone = false,
            rejected = false,
            feedingPlace = null,
            taigaBeanGoose = null,
            greySealHuntingMethod = null.toBackendEnum(),
            actorInfo = GroupHuntingPerson.Unknown,
            selectedClub = null,
        )
}
