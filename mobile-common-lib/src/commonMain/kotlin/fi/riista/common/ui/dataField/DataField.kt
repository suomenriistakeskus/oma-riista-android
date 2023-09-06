package fi.riista.common.ui.dataField

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalTime
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.resources.RR
import io.matthewnelson.component.base64.decodeBase64ToArray
import kotlinx.serialization.Serializable


/**
 * Design philosophy / Main points of implementation
 *
 * 1. Don't use Observables in DataFields.
 *
 * The fields are intended to be displayed in lists (e.g. in RecyclerView on android).
 * The lifecycle management with Observables would probably be much harder to handle as there
 * would need to be item specific DisposeBag or subscription would need to be handled manually.
 *
 * Instead use immutable fields and update the UI manually when data has changed. To make
 * that easier..
 *
 * 2. Use data classes
 *
 * Data classes provide automatically equals() check for all the fields. We can utilize that
 * to check whether each piece of data (i.e. DataField) has changed or not and only update
 * UI when necessary.
 *
 * It is not possible to create sealed data classes (as base class) so instead of trying to do
 * that, let the child classes (i.e. actual data containers) be data classes.
 *
 * todo: check sealed interface when kotlin 1.5.0 is available for kotlin multiplatform
 */

typealias DataFields<FieldId> = List<DataField<FieldId>>

enum class Padding {
    NONE,
    SMALL,
    SMALL_MEDIUM,
    MEDIUM,
    MEDIUM_LARGE,
    LARGE
}

/**
 * The base class for data fields.
 *
 * Keep the actual data in the subclasses as single data field may consist of multiple fields.
 * This is the case e.g. with select-something fields where we need to have a value for
 * the currently selected value and also for possible values.
 */
sealed class DataField<FieldId : DataFieldId> {
    abstract val id: FieldId
    abstract val settings: Settings

    /**
     * Exposed [DataField] settings that are common to all DataFields and accessible
     * through the base class (no DataField downcasting required)
     */
    interface Settings {
        val paddingTop: Padding
        val paddingBottom: Padding
    }

    /**
     * The settings for [DataField]s that can be edited.
     */
    interface EditableSettings : Settings {
        /**
         * Can the data be at least partially edited (=false) or not (=true)?
         */
        val readOnly: Boolean

        /**
         * The requirement status for the field. Should only be taken into account
         * if field can be edited.
         */
        val requirementStatus: FieldRequirement
    }

    /**
     * Is this `DataField` representing data for the same field as `other` i.e.
     * are the fields same?
     */
    fun isSame(other: DataField<FieldId>): Boolean {
        return id.toInt() == other.id.toInt()
    }

    /**
     * Is the data encapsulated in this `DataField` same as in `other` i.e.
     * are the field contents same?
     */
    fun isContentSame(other: DataField<FieldId>): Boolean {
        return this == other
    }
}


// Common data field types

data class StringField<DataId : DataFieldId>(
    override val id: DataId,
    val value: String,
    override val settings: StringFieldSettings
) : DataField<DataId>() {

    interface StringFieldSettings : EditableSettings {
        val label: String?

        /**
         * Should the label + string be displayed on the same line?
         */
        val singleLine: Boolean
    }

    internal data class DefaultStringFieldSettings(
        override var label: String? = null,
        override var singleLine: Boolean = false,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : StringFieldSettings

    internal constructor(
        id: DataId,
        value: String,
        configureSettings: (DefaultStringFieldSettings.() -> Unit)? = null
    ): this(id = id,
            value = value,
            settings = DefaultStringFieldSettings().configuredBy(configureSettings)
    )
}

data class LabelField<DataId : DataFieldId>(
    override val id: DataId,
    val text: String,
    val type: Type,
    override val settings: LabelFieldSettings
) : DataField<DataId>() {
    enum class Type {
        CAPTION,
        ERROR,
        INFO,
        LINK,
        INDICATOR,
    }

    /**
     * Can be shown next to a caption text.
     */
    enum class Icon {
        VERIFIED,
    }

    enum class TextAlignment {
        LEFT,
        CENTER,
        JUSTIFIED,
    }

    interface LabelFieldSettings : Settings {
        val allCaps: Boolean
        val captionIcon: Icon?
        val textAlignment: TextAlignment // applicable for info

        val indicatorColor: IndicatorColor
        val highlightBackground: Boolean
    }

    internal data class DefaultLabelFieldSettings(
        override var allCaps: Boolean = false,
        override var captionIcon: Icon? = null,
        override var textAlignment: TextAlignment = TextAlignment.LEFT,

        override var indicatorColor: IndicatorColor = IndicatorColor.INVISIBLE,
        override var highlightBackground: Boolean = false,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
    ) : LabelFieldSettings

    internal constructor(
        id: DataId,
        text: String,
        type: Type,
        configureSettings: (DefaultLabelFieldSettings.() -> Unit)? = null
    ): this(id = id,
            text = text,
            type = type,
            settings = DefaultLabelFieldSettings().configuredBy(configureSettings)
    )

}

data class AttachmentField<DataId : DataFieldId>(
    override val id: DataId,
    val localId: Long?,
    val filename: String,
    val isImage: Boolean,
    val thumbnailBase64: String?,
    override val settings: EditableSettings,
) : DataField<DataId>() {

    internal data class DefaultAttachmentFieldSettings(
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : EditableSettings

    internal constructor(
        id: DataId,
        localId: Long?,
        filename: String,
        isImage: Boolean,
        thumbnailBase64: String?,
        configureSettings: (DefaultAttachmentFieldSettings.() -> Unit)? = null
    ): this(id = id,
        localId = localId,
        filename = filename,
        isImage = isImage,
        thumbnailBase64 = thumbnailBase64,
        settings = DefaultAttachmentFieldSettings().configuredBy(configureSettings)
    )

    val thumbnailAsByteArray: ByteArray?
        get() {
            return thumbnailBase64?.decodeBase64ToArray()
        }
}

data class IntField<DataId : DataFieldId>(
        override val id: DataId,
        val value: Int?,
        override val settings: IntFieldSettings
) : DataField<DataId>() {

    interface IntFieldSettings : EditableSettings {
        val label: String?
        val maxValue: Int?
    }

    internal data class DefaultIntFieldSettings(
        override var label: String? = null,
        override var maxValue: Int? = null,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : IntFieldSettings

    internal constructor(
            id: DataId,
            value: Int?,
            configureSettings: (DefaultIntFieldSettings.() -> Unit)? = null
    ): this(id = id,
            value = value,
            settings = DefaultIntFieldSettings().configuredBy(configureSettings)
    )
}

data class DoubleField<DataId : DataFieldId>(
        override val id: DataId,
        val value: Double?,
        override val settings: DoubleFieldSettings
) : DataField<DataId>() {

    interface DoubleFieldSettings : EditableSettings {
        val label: String?
        val maxValue: Double?
        val decimals: Int? // How many decimals are shown
    }

    internal data class DefaultDoubleFieldSettings(
        override var label: String? = null,
        override var maxValue: Double? = null,
        override var decimals: Int? = null,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : DoubleFieldSettings

    internal constructor(
            id: DataId,
            value: Double?,
            configureSettings: (DefaultDoubleFieldSettings.() -> Unit)? = null
    ): this(id = id,
            value = value,
            settings = DefaultDoubleFieldSettings().configuredBy(configureSettings)
    )
}

data class BooleanField<DataId : DataFieldId>(
    override val id: DataId,
    // allow unknown/unselected value by allowing null
    val value: Boolean?,
    override val settings: BooleanFieldSettings
) : DataField<DataId>() {

    enum class Appearance {
        YES_NO_BUTTONS,
        CHECKBOX,
        SWITCH,
    }

    interface BooleanFieldSettings : EditableSettings {
        val label: String?
        val text: String? // Currently only supported when appearance == SWITCH
        val appearance: Appearance
    }

    internal data class DefaultBooleanFieldSettings(
        override var label: String? = null,
        override var text: String? = null,
        override var appearance: Appearance = Appearance.YES_NO_BUTTONS,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : BooleanFieldSettings

    internal constructor(
        id: DataId,
        value: Boolean?,
        configureSettings: (DefaultBooleanFieldSettings.() -> Unit)? = null
    ): this(id = id,
            value = value,
            settings = DefaultBooleanFieldSettings().configuredBy(configureSettings)
    )
}


// Custom data field types

/**
 * RiistaSDK currently doesn't have means for providing icon information. Thus wraps
 * only species and it is app's responsibility to be able to provide icon + species name.
 */
data class SpeciesField<DataId : DataFieldId>(
    override val id: DataId,
    val species: Species,
    val entityImage: EntityImage?,
    override val settings: SpeciesFieldSettings,
) : DataField<DataId>() {

    sealed class SelectableSpecies {
        object All : SelectableSpecies()
        data class Listed(val species: List<Species>): SelectableSpecies()

        fun contains(candidate: Species): Boolean {
            return when (this) {
                All -> true
                is Listed -> this.species.contains(candidate)
            }
        }
    }

    interface SpeciesFieldSettings: EditableSettings {
        val showEntityImage: Boolean
        val selectableSpecies: SelectableSpecies
    }

    internal constructor(
        id: DataId,
        speciesCode: SpeciesCode,
        entityImage: EntityImage? = null,
        configureSettings: (DefaultSpeciesFieldSettings.() -> Unit)? = null
    ): this(id = id,
            species = Species.Known(speciesCode = speciesCode),
            entityImage = entityImage,
            configureSettings = configureSettings
    )

    internal constructor(
        id: DataId,
        species: Species,
        entityImage: EntityImage? = null,
        configureSettings: (DefaultSpeciesFieldSettings.() -> Unit)? = null
    ): this(id = id,
            species = species,
            entityImage = entityImage,
            settings = DefaultSpeciesFieldSettings().configuredBy(configureSettings)
    )

    internal data class DefaultSpeciesFieldSettings(
        override var showEntityImage: Boolean = false,
        override var selectableSpecies: SelectableSpecies = SelectableSpecies.All,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ): SpeciesFieldSettings
}

/**
 * A data field containing all specimen data related to an entity / event.
 *
 * [SpecimenFieldDataContainer] is intended to be passed to a separate controller in
 * a separate view in order to display / edit specimen data.
 */
data class SpecimenField<DataId : DataFieldId>(
    override val id: DataId,
    val specimenData: SpecimenFieldDataContainer,
    override val settings: SpecimenFieldSettings,
) : DataField<DataId>() {

    interface SpecimenFieldSettings: EditableSettings {
        val label: String?
    }

    internal constructor(
        id: DataId,
        specimenData: SpecimenFieldDataContainer,
        configureSettings: (DefaultSpecimenFieldSettings.() -> Unit)? = null
    ): this(id = id,
        specimenData = specimenData,
        settings = DefaultSpecimenFieldSettings().configuredBy(configureSettings)
    )

    internal data class DefaultSpecimenFieldSettings(
        override var label: String? = null,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ): SpecimenFieldSettings
}

data class DateAndTimeField<DataId : DataFieldId>(
    override val id: DataId,
    val dateAndTime: LocalDateTime,
    override val settings: DateAndTimeFieldSettings
) : DataField<DataId>() {

    interface DateAndTimeFieldSettings : EditableSettings {
        val label: String?

        /**
         * Is the date part readonly?
         */
        val readOnlyDate: Boolean

        /**
         * Is the time part readonly?
         */
        val readOnlyTime: Boolean

        /**
         * The minimum allowed date time value.
         */
        val minDateTime: LocalDateTime?

        /**
         * The maximum allowed date time value.
         */
        val maxDateTime: LocalDateTime?
    }

    internal data class DefaultDateAndTimeFieldSettings(
        override var label: String? = null,
        override var readOnlyDate: Boolean = true,
        override var readOnlyTime: Boolean = true,
        override var minDateTime: LocalDateTime? = null,
        override var maxDateTime: LocalDateTime? = null,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : DateAndTimeFieldSettings {

        // Prevent impossible states by not having a backing field and not allowing modifying it.
        // Instead let changing readOnly value change both readOnlyDate and readOnlyTime
        override var readOnly: Boolean
            get() {
                return readOnlyDate && readOnlyTime
            }
            set(value) {
                readOnlyDate = value
                readOnlyTime = value
            }
    }

    internal constructor(
        id: DataId,
        dateAndTime: LocalDateTime,
        configureSettings: (DefaultDateAndTimeFieldSettings.() -> Unit)? = null
    ): this(id = id,
            dateAndTime = dateAndTime,
            settings = DefaultDateAndTimeFieldSettings().configuredBy(configureSettings)
    )
}

data class DateField<DataId : DataFieldId>(
    override val id: DataId,
    val date: LocalDate,
    override val settings: DateFieldSettings
) : DataField<DataId>() {

    interface DateFieldSettings : EditableSettings {
        val label: String?

        /**
         * The minimum allowed date value.
         */
        val minDate: LocalDate?

        /**
         * The maximum allowed date value.
         */
        val maxDate: LocalDate?
    }

    internal data class DefaultDateFieldSettings(
        override var label: String? = null,
        override var minDate: LocalDate? = null,
        override var maxDate: LocalDate? = null,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : DateFieldSettings {
    }

    internal constructor(
        id: DataId,
        date: LocalDate,
        configureSettings: (DefaultDateFieldSettings.() -> Unit)? = null
    ) : this(
        id = id,
        date = date,
        settings = DefaultDateFieldSettings().configuredBy(configureSettings)
    )
}

data class TimespanField<DataId : DataFieldId>(
    override val id: DataId,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val startFieldId: DataId,
    val endFieldId: DataId,
    override val settings: TimespanFieldSettings
) : DataField<DataId>() {

    interface TimespanFieldSettings : EditableSettings {
        val startLabel: String?
        val endLabel: String?
    }

    internal data class DefaultTimespanFieldSettings(
        override var startLabel: String? = null,
        override var endLabel: String? = null,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : TimespanFieldSettings {
    }

    internal constructor(
        id: DataId,
        startTime: LocalTime?,
        endTime: LocalTime?,
        startFieldId: DataId,
        endFieldId: DataId,
        configureSettings: (DefaultTimespanFieldSettings.() -> Unit)? = null
    ): this(
        id = id,
        startTime = startTime,
        endTime = endTime,
        startFieldId = startFieldId,
        endFieldId = endFieldId,
        settings = DefaultTimespanFieldSettings().configuredBy(configureSettings)
    )
}

data class LocationField<DataId : DataFieldId>(
    override val id: DataId,
    val location: CommonLocation,
    override val settings: EditableSettings
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        location: CommonLocation,
        configureSettings: (DefaultEditableSettings.() -> Unit)? = null
    ): this(id = id,
            location = location,
            settings = DefaultEditableSettings(
                    paddingTop = Padding.NONE,
                    paddingBottom = Padding.NONE
            ).configuredBy(configureSettings)
    )
}

data class GenderField<DataId : DataFieldId>(
    override val id: DataId,
    val gender: Gender?,
    override val settings: GenderFieldSettings
) : DataField<DataId>() {

    interface GenderFieldSettings : EditableSettings {
        val showUnknown: Boolean
    }

    internal constructor(
        id: DataId,
        gender: Gender?,
        configureSettings: (DefaultGenderFieldSettings.() -> Unit)? = null
    ): this(id = id,
            gender = gender,
            settings = DefaultGenderFieldSettings().configuredBy(configureSettings)
    )

    internal data class DefaultGenderFieldSettings(
        override var showUnknown: Boolean = false,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : GenderFieldSettings
}

data class AgeField<DataId : DataFieldId>(
    override val id: DataId,
    val age: GameAge?,
    override val settings: AgeFieldSettings
) : DataField<DataId>() {

    interface AgeFieldSettings : EditableSettings {
        val showUnknown: Boolean
    }

    internal constructor(
        id: DataId,
        age: GameAge?,
        configureSettings: (DefaultAgeFieldSettings.() -> Unit)? = null
    ): this(id = id,
            age = age,
            settings = DefaultAgeFieldSettings().configuredBy(configureSettings)
    )

    internal data class DefaultAgeFieldSettings(
        override var showUnknown: Boolean = false,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : AgeFieldSettings
}

data class StringListField<DataId : DataFieldId>(
    override val id: DataId,
    /**
     * Possible values. The strings provide default information about each value.
     */
    val values: List<StringWithId>,

    /**
     * Possible values but with more details. The strings provide more detailed information
     * about each value.
     *
     * Ids are required to match ids found in values.
     */
    val detailedValues: List<StringWithId>,
    val selected: List<StringId>?,
    override val settings: StringListFieldSettings
) : DataField<DataId>() {

    enum class Mode {
        MULTI,
        SINGLE,
    }

    /**
     * Settings for the external view if selection is made in such way.
     */
    @Serializable
    data class ExternalViewConfiguration(
        // title for the view
        val title: String,
        val filterEnabled: Boolean = true,
        val filterLabelText: String? = null,
        val filterTextHint: String? = null,
    )

    interface StringListFieldSettings : EditableSettings {
        val mode: Mode
        val label: String?

        /**
         * Text that is shown in a place where SINGLE mode has selected value.
         */
        val multiModeChooseText: String?

        /**
         * Should the selection be made using an external view if possible?
         * When MODE == MULTI external view is always used.
         */
        val preferExternalViewForSelection: Boolean

        /**
         * Configuration for the external view
         */
        val externalViewConfiguration: ExternalViewConfiguration?
    }

    internal data class DefaultStringListFieldSettings(
        override var mode: Mode = Mode.SINGLE,
        override var label: String? = null,
        override var multiModeChooseText: String? = null,

        override var preferExternalViewForSelection: Boolean = false,
        override var externalViewConfiguration: ExternalViewConfiguration? = null,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : StringListFieldSettings

    internal constructor(
        id: DataId,
        values: List<StringWithId>,
        selected: List<StringId>?,
        configureSettings: (DefaultStringListFieldSettings.() -> Unit)? = null
    ): this(id = id,
            values = values,
            detailedValues = values,
            selected = selected,
            configureSettings = configureSettings
    )

    internal constructor(
        id: DataId,
        values: List<StringWithId>,
        detailedValues: List<StringWithId>,
        selected: List<StringId>?,
        configureSettings: (DefaultStringListFieldSettings.() -> Unit)? = null
    ): this(id = id,
            values = values,
            detailedValues = detailedValues,
            selected = selected,
            settings = DefaultStringListFieldSettings().configuredBy(configureSettings)
    )
}

data class SelectDurationField<DataId : DataFieldId>(
    override val id: DataId,
    val value: HoursAndMinutes,
    val possibleValues: List<HoursAndMinutes>,
    override val settings: SelectDurationFieldSettings
) : DataField<DataId>() {

    interface SelectDurationFieldSettings : EditableSettings {
        val label: String?
        val zeroMinutesStringId: RR.string?
    }

    internal data class DefaultSelectDurationFieldSettings(
        override var label: String? = null,
        override var zeroMinutesStringId: RR.string? = null,
        override var readOnly: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : SelectDurationFieldSettings

    internal constructor(
        id: DataId,
        value: HoursAndMinutes,
        possibleValues: List<HoursAndMinutes>,
        configureSettings: (DefaultSelectDurationFieldSettings.() -> Unit)? = null
    ): this(id = id,
            value = value,
            possibleValues = possibleValues,
            settings = DefaultSelectDurationFieldSettings().configuredBy(configureSettings)
    )
}

data class HuntingDayAndTimeField<DataId : DataFieldId>(
    override val id: DataId,
    // the current day id
    val huntingDayId: GroupHuntingDayId?,
    // the date and time of the harvest/observation/etc.
    val dateAndTime: LocalDateTime,
    override val settings: HuntingDayAndTimeFieldSettings
) : DataField<DataId>() {

    interface HuntingDayAndTimeFieldSettings : EditableSettings {
        val label: String?
        val readOnlyDate: Boolean
        val readOnlyTime: Boolean
    }

    internal data class DefaultHuntingDayAndTimeFieldSettings(
        override var label: String? = null,
        override var readOnlyDate: Boolean = true,
        override var readOnlyTime: Boolean = true,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : HuntingDayAndTimeFieldSettings {
        // Prevent impossible states by not having a backing field and not allowing modifying it.
        // Instead let changing readOnly value change both readOnlyDate and readOnlyTime
        override var readOnly: Boolean
            get() {
                return readOnlyDate && readOnlyTime
            }
            set(value) {
                readOnlyDate = value
                readOnlyTime = value
            }
    }

    internal constructor(
        id: DataId,
        huntingDayId: GroupHuntingDayId?,
        dateAndTime: LocalDateTime,
        configureSettings: (DefaultHuntingDayAndTimeFieldSettings.() -> Unit)? = null
    ): this(id = id,
            huntingDayId = huntingDayId,
            dateAndTime = dateAndTime,
            settings = DefaultHuntingDayAndTimeFieldSettings().configuredBy(configureSettings)
    )
}

data class InstructionsField<DataId : DataFieldId>(
    override val id: DataId,
    val type: Type,
    override val settings: Settings,
) : DataField<DataId>() {

    enum class Type {
        MOOSE_ANTLER_INSTRUCTIONS,
        WHITE_TAILED_DEER_ANTLER_INSTRUCTIONS,
        ROE_DEER_ANTLER_INSTRUCTIONS,
    }

    internal constructor(
        id: DataId,
        type: Type,
        configureSettings: (DefaultSettings.() -> Unit)? = null
    ): this(id = id,
        type = type,
        settings = DefaultSettings().configuredBy(configureSettings)
    )
}

data class HarvestField<DataId : DataFieldId>(
    override val id: DataId,
    val harvestId: Long,
    val speciesCode: SpeciesCode,
    val pointOfTime: LocalDateTime,
    val amount: Int,
    val acceptStatus: AcceptStatus,
    override val settings: Settings,
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        harvestId: Long,
        speciesCode: SpeciesCode,
        pointOfTime: LocalDateTime,
        amount: Int,
        acceptStatus: AcceptStatus,
        configureSettings: (DefaultSettings.() -> Unit)? = null
    ): this(id = id,
            harvestId = harvestId,
            speciesCode = speciesCode,
            pointOfTime = pointOfTime,
            amount = amount,
            acceptStatus = acceptStatus,
            settings = DefaultSettings().configuredBy(configureSettings)
    )
}

data class ObservationField<DataId : DataFieldId>(
    override val id: DataId,
    val observationId: Long,
    val speciesCode: SpeciesCode,
    val pointOfTime: LocalDateTime,
    val amount: Int,
    val acceptStatus: AcceptStatus,
    override val settings: Settings,
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        observationId: Long,
        speciesCode: SpeciesCode,
        pointOfTime: LocalDateTime,
        amount: Int,
        acceptStatus: AcceptStatus,
        configureSettings: (DefaultSettings.() -> Unit)? = null
    ): this(id = id,
            observationId = observationId,
            speciesCode = speciesCode,
            pointOfTime = pointOfTime,
            amount = amount,
            acceptStatus = acceptStatus,
            settings = DefaultSettings().configuredBy(configureSettings)
    )
}

/**
 * A [DataField] for requesting custom UI to be displayed.
 *
 * This field can be used e.g. for text + button that should be displayed between
 * other fields in some particular case. The user interface (e.g. fragments on android,
 * viewcontrollers on iOS) need to be able to handle these.
 */
data class CustomUserInterfaceField<DataId : DataFieldId>(
    override val id: DataId,
    override val settings: Settings,
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        configureSettings: (DefaultSettings.() -> Unit)? = null
    ): this(id = id,
            settings = DefaultSettings().configuredBy(configureSettings)
    )
}

data class ButtonField<DataId : DataFieldId>(
    override val id: DataId,
    val text: String,
    override val settings: Settings
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        text: String,
        configureSettings: (DefaultSettings.() -> Unit)? = null
    ): this(id = id,
        text = text,
        settings = DefaultSettings().configuredBy(configureSettings)
    )
}

/**
 * selectedIds are used in TOGGLE mode to list which chipds are selected.
 */
data class ChipField<DataId : DataFieldId>(
    override val id: DataId,
    val chips: List<StringWithId>,
    val selectedIds: List<StringId>?,
    override val settings: ChipFieldSettings,
) : DataField<DataId>() {

    enum class Mode {
        VIEW,
        DELETE,
        TOGGLE,
    }

    interface ChipFieldSettings : EditableSettings {
        val mode: Mode
        val label: String?
    }

    internal data class DefaultChipFieldSettings(
        override var mode: Mode = Mode.VIEW,
        override var label: String? = null,
        override var paddingTop: Padding = Padding.MEDIUM,
        override var paddingBottom: Padding = Padding.MEDIUM,
        override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
    ) : ChipFieldSettings {
        override val readOnly: Boolean
            get() {
                return mode == Mode.VIEW
            }
    }

    internal constructor(
        id: DataId,
        chips: List<StringWithId>,
        selectedIds: List<StringId>? = null,
        configureSettings: (DefaultChipFieldSettings.() -> Unit)? = null
    ): this(id = id,
        chips = chips,
        selectedIds = selectedIds,
        settings = DefaultChipFieldSettings().configuredBy(configureSettings)
    )
}

// Helpers

/**
 * Internal [DataField.Settings] that can be modified when creating data fields. Contains
 * the default values.
 */
internal data class DefaultSettings(
    override var paddingTop: Padding = Padding.MEDIUM,
    override var paddingBottom: Padding = Padding.MEDIUM,
): DataField.Settings

/**
 * Internal [DataField.EditableSettings] that can be modified when creating data fields. Contains
 * the default values.
 */
internal data class DefaultEditableSettings(
    override var readOnly: Boolean = true,
    override var paddingTop: Padding = Padding.MEDIUM,
    override var paddingBottom: Padding = Padding.MEDIUM,
    override var requirementStatus: FieldRequirement = FieldRequirement.voluntary(),
): DataField.EditableSettings

private fun <T : DataField.Settings> T.configuredBy(configureSettings: (T.() -> Unit)? = null): T {
    if (configureSettings != null) {
        this.configureSettings()
    }

    return this
}
