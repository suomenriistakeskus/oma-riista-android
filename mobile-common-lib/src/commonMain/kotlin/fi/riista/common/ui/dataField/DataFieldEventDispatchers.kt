package fi.riista.common.ui.dataField

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermit
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.model.*
import fi.riista.common.ui.intent.IntentHandler


/**
 * Interfaces for classes that are able to dispatch [DataField] changes.
 *
 * The main purpose of these interfaces is to help make a clear separation between
 * - UI (how [DataField]s are displayed)
 * - sending change events (using these interfaces)
 * - reacting to change events (probably mapping events to intents, see [IntentHandler])
 * - updating model (e.g. a controller reacting to intents)
 *
 * Having these interfaces therefore will help separating UI from controller.
 */

fun interface BooleanEventDispatcher<FieldId> {
    fun dispatchBooleanChanged(fieldId: FieldId, value: Boolean)
}

fun interface DoubleEventDispatcher<FieldId> {
    fun dispatchDoubleChanged(fieldId: FieldId, value: Double?)
}

fun interface IntEventDispatcher<FieldId> {
    fun dispatchIntChanged(fieldId: FieldId, value: Int?)
}

fun interface StringEventDispatcher<FieldId> {
    fun dispatchStringChanged(fieldId: FieldId, value: String)
}

fun interface StringWithIdEventDispatcher<FieldId> {
    fun dispatchStringWithIdChanged(fieldId: FieldId, value: List<StringWithId>)
}

fun interface StringWithIdClickEventDispatcher<FieldId> {
    fun dispatchStringWithIdClicked(fieldId: FieldId, value: StringWithId)
}

fun interface LocalDateTimeEventDispatcher<FieldId> {
    fun dispatchLocalDateTimeChanged(fieldId: FieldId, value: LocalDateTime)
}

fun interface LocalDateEventDispatcher<FieldId> {
    fun dispatchLocalDateChanged(fieldId: FieldId, value: LocalDate)
}

fun interface LocalTimeEventDispatcher<FieldId> {
    fun dispatchLocalTimeChanged(fieldId: FieldId, value: LocalTime)
}

fun interface HoursAndMinutesEventDispatcher<FieldId> {
    fun dispatchHoursAndMinutesChanged(fieldId: FieldId, value: HoursAndMinutes)
}

fun interface HuntingDayIdEventDispatcher<FieldId> {
    fun dispatchHuntingDayChanged(fieldId: FieldId, value: GroupHuntingDayId)
}

fun interface GenderEventDispatcher<FieldId> {
    fun dispatchGenderChanged(fieldId: FieldId, value: Gender?)
}

fun interface AgeEventDispatcher<FieldId> {
    fun dispatchAgeChanged(fieldId: FieldId, value: GameAge?)
}

fun interface LocationEventDispatcher<FieldId> {
    fun dispatchLocationChanged(fieldId: FieldId, value: ETRMSGeoLocation)
}

fun interface SpeciesEventDispatcher<FieldId> {
    fun dispatchSpeciesChanged(fieldId: FieldId, value: Species)
}

fun interface EntityImageDispatcher {
    fun setEntityImage(image: EntityImage)
}

fun interface SpecimenDataEventDispatcher<FieldId> {
    fun dispatchSpecimenDataChanged(fieldId: FieldId, value: SpecimenFieldDataContainer)
}

fun interface ActionEventDispatcher<FieldId> {
    fun dispatchEvent(fieldId: FieldId)
}

fun interface AttachmentEventDispatcher<FieldId> {
    fun addAttachment(attachment: HuntingControlAttachment)
}

fun interface PermitEventDispatcher {
    fun selectPermit(permit: CommonHarvestPermit, speciesCode: SpeciesCode?)
}
