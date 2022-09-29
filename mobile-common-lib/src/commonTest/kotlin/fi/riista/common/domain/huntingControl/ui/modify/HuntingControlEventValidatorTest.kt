package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.huntingControl.model.*
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.model.*
import fi.riista.common.model.*
import fi.riista.common.ui.dataField.*
import kotlin.test.Test
import kotlin.test.assertEquals

class HuntingControlEventValidatorTest {
    @Test
    fun testValidData() {
        val event = getHuntingControlEventData()
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(0, errors.size)
    }

    @Test
    fun testNoLocation() {
        val event = getHuntingControlEventData().copy(location = CommonLocation.Unknown)
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_LOCATION, errors[0])
    }

    @Test
    fun testNoEventType() {
        val event = getHuntingControlEventData().copy(eventType = BackendEnum.create(null))
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_EVENT_TYPE, errors[0])
    }

    @Test
    fun testNoEventDescription() {
        val event = getHuntingControlEventData()

        with (event) {
            val errors = HuntingControlEventValidator.validate(
                event = this,
                displayedFields = getFieldSpecifications(this),
                gameWardens = getGameWardens(),
            )
            assertEquals(0, errors.size, "valid description")
        }

        with (event.copy(description = null)) {
            val errors = HuntingControlEventValidator.validate(
                event = this,
                displayedFields = getFieldSpecifications(this),
                gameWardens = getGameWardens(),
            )
            assertEquals(0, errors.size, "null description")
        }

        with (event.copy(description = "")) {
            val errors = HuntingControlEventValidator.validate(
                event = this,
                displayedFields = getFieldSpecifications(this),
                gameWardens = getGameWardens(),
            )
            assertEquals(0, errors.size, "empty description")
        }
    }

    @Test
    fun testNoEventDescriptionWithOtherEventType() {
        val event = getHuntingControlEventData().copy(
            eventType = HuntingControlEventType.OTHER.toBackendEnum(),
        )

        with (event) {
            val errors = HuntingControlEventValidator.validate(
                event = this,
                displayedFields = getFieldSpecifications(this),
                gameWardens = getGameWardens(),
            )
            assertEquals(0, errors.size, "valid description")
        }

        with (event.copy(description = null)) {
            val errors = HuntingControlEventValidator.validate(
                event = this,
                displayedFields = getFieldSpecifications(this),
                gameWardens = getGameWardens(),
            )
            assertEquals(1, errors.size, "null description")
            assertEquals(HuntingControlEventValidator.Error.MISSING_EVENT_DESCRIPTION, errors[0], "null description")
        }

        with (event.copy(description = "")) {
            val errors = HuntingControlEventValidator.validate(
                event = this,
                displayedFields = getFieldSpecifications(this),
                gameWardens = getGameWardens(),
            )
            assertEquals(1, errors.size, "empty description")
            assertEquals(HuntingControlEventValidator.Error.MISSING_EVENT_DESCRIPTION, errors[0], "empty description")
        }

        with (event.copy(description = "  ")) {
            val errors = HuntingControlEventValidator.validate(
                event = this,
                displayedFields = getFieldSpecifications(this),
                gameWardens = getGameWardens(),
            )
            assertEquals(1, errors.size, "whitespace description")
            assertEquals(HuntingControlEventValidator.Error.MISSING_EVENT_DESCRIPTION, errors[0], "whitespace description")
        }
    }

    @Test
    fun testNoStartTime() {
        val event = getHuntingControlEventData().copy(startTime = null)
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_START_TIME, errors[0])
    }

    @Test
    fun testNoEndTime() {
        val event = getHuntingControlEventData().copy(endTime = null)
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_END_TIME, errors[0])
    }

    @Test
    fun testMissingInspector() {
        val event = getHuntingControlEventData().copy(inspectors = listOf())
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_INSPECTOR, errors[0])
    }

    @Test
    fun testNoInspectorsForDate() {
        val event = getHuntingControlEventData().copy(inspectors = listOf())
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = listOf(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.NO_INSPECTORS_FOR_DATE, errors[0])
    }

    @Test
    fun testNoCooperation() {
        val event = getHuntingControlEventData().copy(cooperationTypes = listOf())
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_COOPERATION, errors[0])
    }

    @Test
    fun testNoCustomers() {
        val event = getHuntingControlEventData().copy(customerCount = null)
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_CUSTOMERS, errors[0])
    }

    @Test
    fun testNoProofOrders() {
        val event = getHuntingControlEventData().copy(proofOrderCount = null)
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_PROOF_ORDERS, errors[0])
    }

    @Test
    fun testNoWolfTerritory() {
        val event = getHuntingControlEventData().copy(wolfTerritory = null)
        val errors = HuntingControlEventValidator.validate(
            event = event,
            displayedFields = getFieldSpecifications(event),
            gameWardens = getGameWardens(),
        )
        assertEquals(1, errors.size)
        assertEquals(HuntingControlEventValidator.Error.MISSING_WOLF_TERRITORY, errors[0])
    }

    private fun getHuntingControlEventData(): HuntingControlEventData {
        return HuntingControlEventData(
            specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION,
            mobileClientRefId = 1234L,
            rhyId = 1L,
            eventType = HuntingControlEventType.DOG_DISCIPLINE_CONTROL.toBackendEnum(),
            status = HuntingControlEventStatus.PROPOSED.toBackendEnum(),
            inspectors = listOf(
                HuntingControlEventInspector(
                    id = 888,
                    firstName = "Byte",
                    lastName = "Vakt",
                )
            ),
            cooperationTypes = listOf(
                HuntingControlCooperationType.OMA.toBackendEnum(),
            ),
            wolfTerritory = false,
            otherParticipants = "Pena ja Turo",
            location = ETRMSGeoLocation(
                latitude = 7000,
                longitude = 9000,
                source = GeoLocationSource.GPS_DEVICE.toBackendEnum(),
                accuracy = 13.9,
                altitude = 19.4,
                altitudeAccuracy = 12.23,
            ).asKnownLocation(),
            locationDescription = "Pyynikin tori",
            date = LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 3),
            startTime = LocalTime(hour = 10, minute = 12, second = 33),
            endTime = LocalTime(hour = 12, minute = 22, second = 2),
            customerCount = 2,
            proofOrderCount = 1,
            description = "Kuvausta",
            canEdit = false,
            modified = true,
            attachments = listOf(),
        )
    }

    private fun getFieldSpecifications(event: HuntingControlEventData): List<FieldSpecification<HuntingControlEventField>> {
        return FieldSpecificationListBuilder<HuntingControlEventField>()
            .add(
                HuntingControlEventField.Type.LOCATION.toField().required(),
                HuntingControlEventField.Type.LOCATION_DESCRIPTION.toField().voluntary(),
                HuntingControlEventField.Type.WOLF_TERRITORY.toField().required(),
                HuntingControlEventField.Type.EVENT_TYPE.toField().required(),
                HuntingControlEventField.Type.EVENT_DESCRIPTION.toField().voluntary(),
                HuntingControlEventField.Type.DATE.toField().required(),
                HuntingControlEventField.Type.START_AND_END_TIME.toField().required(),
                HuntingControlEventField.Type.DURATION.toField().noRequirement(),
                HuntingControlEventField.Type.INSPECTORS.toField().required(),
                HuntingControlEventField.Type.INSPECTOR_NAMES.toField().noRequirement()
                    .takeIf { event.inspectors.isNotEmpty() },
                HuntingControlEventField.Type.NUMBER_OF_INSPECTORS.toField().required(),
                HuntingControlEventField.Type.COOPERATION.toField().required(),
                HuntingControlEventField.Type.OTHER_PARTICIPANTS.toField().voluntary(),
                HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS.toField().required(),
                HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS.toField().required()
            ).toList()
    }

    private fun getGameWardens(): List<HuntingControlGameWarden> {
        return listOf(
            HuntingControlGameWarden(
                remoteId = 888,
                firstName = "Byte",
                lastName = "Vakt",
                startDate = LocalDate(2022, 1, 1),
                endDate = LocalDate(2022, 12, 31),
            )
        )
    }
}
