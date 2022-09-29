package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.huntingControl.model.HuntingControlEventInspector
import fi.riista.common.domain.huntingControl.sync.model.GameWarden
import fi.riista.common.domain.huntingControl.sync.model.LoadHuntingControlEvent
import fi.riista.common.domain.huntingControl.sync.model.LoadRhyHuntingControlEvents
import fi.riista.common.domain.model.*
import fi.riista.common.model.*

object SyncTestData {
    fun getRhyEvents(): LoadRhyHuntingControlEvents {
        return LoadRhyHuntingControlEvents(
            specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION,
            rhy = Organization(
                id = 101,
                name = LocalizedString(
                    fi = "Test RHY fi",
                    sv = "Test RHY sv",
                    en = "Test RHY en"
                ),
                officialCode = "1234"
            ),
            gameWardens = listOf(
                GameWarden(
                    inspector = HuntingControlEventInspector(
                        id = 999,
                        firstName = "Game",
                        lastName = "Warden",
                    ),
                    beginDate = LocalDate(
                        year = 2022,
                        monthNumber = 1,
                        dayOfMonth = 12,
                    ),
                    endDate = LocalDate(
                        year = 2022,
                        monthNumber = 12,
                        dayOfMonth = 31,
                    )
                )
            ),
            events = listOf(
                LoadHuntingControlEvent(
                    specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION,
                    id = 123L,
                    rev = 1,
                    mobileClientRefId = null,
                    eventType = fi.riista.common.domain.huntingControl.model.HuntingControlEventType.MOOSELIKE_HUNTING_CONTROL.toBackendEnum(),
                    status = fi.riista.common.domain.huntingControl.model.HuntingControlEventStatus.PROPOSED.toBackendEnum(),
                    inspectors = listOf(
                        HuntingControlEventInspector(
                            id = 999,
                            firstName = "Game",
                            lastName = "Warden",
                        )
                    ),
                    cooperationTypes = listOf(
                        fi.riista.common.domain.huntingControl.model.HuntingControlCooperationType.METSAHALLITUS.toBackendEnum(),
                    ),
                    wolfTerritory = true,
                    otherParticipants = "Pena",
                    geoLocation = ETRMSGeoLocation(
                        latitude = 6000,
                        longitude = 8000,
                        source = fi.riista.common.model.GeoLocationSource.MANUAL.toBackendEnum(),
                        accuracy = 12.9,
                        altitude = 9.4,
                        altitudeAccuracy = 15.23,
                    ),
                    locationDescription = "Pyynikki",
                    date = LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 2),
                    beginTime = LocalTime(hour = 10, minute = 13, second = 33),
                    endTime = LocalTime(hour = 12, minute = 21, second = 2),
                    customers = 3,
                    proofOrders = 2,
                    description = "Kuvausta",
                    attachments = listOf(), // TODO: Add attachments
                    canEdit = true,
                )
            ),
        )
    }
}

