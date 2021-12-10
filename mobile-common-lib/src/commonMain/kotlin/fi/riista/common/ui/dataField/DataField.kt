package fi.riista.common.ui.dataField

import fi.riista.common.groupHunting.model.GroupHuntingDayId
import fi.riista.common.model.*
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

interface DataFieldId {
    /**
     * [DataField] ids must be representable as [Int]. This allows using stable ids in
     * RecyclerView.
     */
    fun toInt(): Int
}

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
    override val settings: Settings
) : DataField<DataId>() {
    enum class Type {
        CAPTION,
        ERROR,
        INFO,
    }

    internal constructor(
        id: DataId,
        text: String,
        type: Type,
        configureSettings: (DefaultSettings.() -> Unit)? = null
    ): this(id = id,
            text = text,
            type = type,
            settings = DefaultSettings().configuredBy(configureSettings)
    )
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
    // allow unknown value
    val value: Boolean?,
    override val settings: BooleanFieldSettings
) : DataField<DataId>() {

    interface BooleanFieldSettings : EditableSettings {
        val label: String?
    }

    internal data class DefaultBooleanFieldSettings(
        override var label: String? = null,
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

// data class IntField<DataId>(override val id: DataId, val value: Int): DataField<DataId>()


// Custom data field types

/**
 * SpeciesCode is actually Int but use a separate data field since we're not able to provide
 * species name + icon from RiistaSDK currently --> app can handle these differently
 */
data class SpeciesCodeField<DataId : DataFieldId>(
    override val id: DataId,
    val speciesCode: SpeciesCode,
    override val settings: EditableSettings,
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        speciesCode: SpeciesCode,
        configureSettings: (DefaultEditableSettings.() -> Unit)? = null
    ): this(id = id,
            speciesCode = speciesCode,
            settings = DefaultEditableSettings().configuredBy(configureSettings)
    )
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

data class LocationField<DataId : DataFieldId>(
    override val id: DataId,
    val location: ETRMSGeoLocation,
    override val settings: EditableSettings
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        location: ETRMSGeoLocation,
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
    val gender: Gender,
    override val settings: EditableSettings
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        gender: Gender,
        configureSettings: (DefaultEditableSettings.() -> Unit)? = null
    ): this(id = id,
            gender = gender,
            settings = DefaultEditableSettings().configuredBy(configureSettings)
    )
}

data class AgeField<DataId : DataFieldId>(
    override val id: DataId,
    val age: GameAge,
    override val settings: EditableSettings
) : DataField<DataId>() {

    internal constructor(
        id: DataId,
        age: GameAge,
        configureSettings: (DefaultEditableSettings.() -> Unit)? = null
    ): this(id = id,
            age = age,
            settings = DefaultEditableSettings().configuredBy(configureSettings)
    )
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
        val selected: StringId?,
        override val settings: StringListFieldSettings
) : DataField<DataId>() {

    /**
     * Settings for the external view if selection is made in such way.
     */
    @Serializable
    data class ExternalViewConfiguration(
        // title for the view
        val title: String,
        //val filterEnabled: Boolean, // should we have this?
        val filterLabelText: String,
        val filterTextHint: String,
    )

    interface StringListFieldSettings : EditableSettings {
        val label: String?

        /**
         * Should the selection be made using an external view if possible?
         */
        val preferExternalViewForSelection: Boolean

        /**
         * Configuration for the external view
         */
        val externalViewConfiguration: ExternalViewConfiguration?
    }

    internal data class DefaultStringListFieldSettings(
        override var label: String? = null,
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
            selected: StringId?,
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
        selected: StringId?,
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
    }

    internal data class DefaultSelectDurationFieldSettings(
        override var label: String? = null,
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
