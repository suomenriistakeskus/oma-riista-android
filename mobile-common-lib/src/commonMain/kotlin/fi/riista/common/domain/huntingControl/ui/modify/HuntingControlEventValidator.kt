package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.domain.huntingControl.model.HuntingControlEventType
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.model.BackendEnum
import fi.riista.common.ui.dataField.FieldSpecification

object HuntingControlEventValidator {
    enum class Error {
        MISSING_LOCATION,
        MISSING_EVENT_TYPE,
        MISSING_EVENT_DESCRIPTION,
        MISSING_START_TIME,
        MISSING_END_TIME,
        MISSING_INSPECTOR,
        NO_INSPECTORS_FOR_DATE,
        MISSING_COOPERATION,
        MISSING_CUSTOMERS,
        MISSING_PROOF_ORDERS,
        MISSING_WOLF_TERRITORY,
    }

    fun validate(
        event: HuntingControlEventData,
        displayedFields: List<FieldSpecification<HuntingControlEventField>>,
        gameWardens: List<HuntingControlGameWarden>,
    ): List<Error> {
        return displayedFields.mapNotNull { fieldSpecification ->
            when (fieldSpecification.fieldId.type) {
                HuntingControlEventField.Type.LOCATION -> {
                    listOfNotNull(
                        Error.MISSING_LOCATION.takeIf {
                            event.location is CommonLocation.Unknown
                        }
                    )
                }
                HuntingControlEventField.Type.EVENT_TYPE -> {
                    listOfNotNull(
                        Error.MISSING_EVENT_TYPE.takeIf {
                            event.eventType == BackendEnum.create<HuntingControlEventType>(null)
                        }
                    )
                }
                HuntingControlEventField.Type.EVENT_DESCRIPTION -> {
                    listOfNotNull(
                        Error.MISSING_EVENT_DESCRIPTION.takeIf {
                            event.eventType.value == HuntingControlEventType.OTHER && event.description.isNullOrBlank()
                        }
                    )
                }
                HuntingControlEventField.Type.START_AND_END_TIME -> {
                    listOfNotNull(
                        Error.MISSING_START_TIME.takeIf {
                            event.startTime == null
                        },
                        Error.MISSING_END_TIME.takeIf {
                            event.endTime == null
                        }
                    )
                }
                HuntingControlEventField.Type.INSPECTORS -> {
                    listOfNotNull(
                        if (gameWardens.isEmpty()) {
                            Error.NO_INSPECTORS_FOR_DATE
                        } else {
                            Error.MISSING_INSPECTOR.takeIf {
                                event.inspectors.isEmpty()
                            }
                        }
                    )
                }
                HuntingControlEventField.Type.COOPERATION -> {
                    listOfNotNull(
                        Error.MISSING_COOPERATION.takeIf {
                            event.cooperationTypes.isEmpty()
                        }
                    )
                }
                HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS -> {
                    listOfNotNull(
                        Error.MISSING_CUSTOMERS.takeIf {
                            event.customerCount == null
                        }
                    )
                }
                HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS -> {
                    listOfNotNull(
                        Error.MISSING_PROOF_ORDERS.takeIf {
                            event.proofOrderCount == null
                        }
                    )
                }
                HuntingControlEventField.Type.WOLF_TERRITORY -> {
                    listOfNotNull(
                        Error.MISSING_WOLF_TERRITORY.takeIf {
                            event.wolfTerritory == null
                        }
                    )
                }
                else -> {
                    null
                }
            }
        }.flatten()
    }
}
