package fi.riista.common.domain.groupHunting.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.isDeer
import fi.riista.common.model.*
import fi.riista.common.model.extensions.fromEpochSeconds
import fi.riista.common.model.extensions.secondsFromEpoch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = GroupHuntingDayIdSerializer::class)
class GroupHuntingDayId private constructor(
    /**
     * The id of the [GroupHuntingDay] received from the backend.
     */
    override val remoteId: BackendId?,

    /**
     * The date acts as an unique identifier for [GroupHuntingDay]s that have not been received
     * from the backend.
     *
     * NOTE, uniquely identifies hunting day assuming following restrictions:
     * - day needs to be identified within a hunting group context
     * - there cannot be multiple hunting days for the same date within one hunting group
     */
    val date: LocalDate?,
): EntityId {
    init {
        require(remoteId != null || date != null) {
            "Either remoteId or date must be non-null!"
        }
    }

    fun isRemote() = remoteId != null
    fun isLocal() = date != null

    override fun toLong(): Long {
        // require remote ids to be positive so that negative values
        // can be used for expressing local ids
        return if (remoteId != null) {
            require(remoteId >= 0)
            remoteId
        } else {
            require(date != null)
            return -date.secondsFromEpoch()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupHuntingDayId) return false

        if (remoteId != other.remoteId) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = remoteId?.hashCode() ?: 0
        result = 31 * result + (date?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return when {
            remoteId != null -> "GroupHuntingDayId(remoteId=$remoteId)"
            date != null -> "GroupHuntingDayId(date=$date)"
            else -> throw IllegalStateException("Either remoteId or date must be non-null!")
        }
    }

    companion object {
        fun remote(remoteId: BackendId) = GroupHuntingDayId(remoteId, null)
        fun remote(remoteId: BackendId?) = remoteId?.let { remote(it) }

        fun local(date: LocalDate) = GroupHuntingDayId(null, date)

        fun fromLong(value: Long): GroupHuntingDayId {
            return when {
                value >= 0 -> GroupHuntingDayId(remoteId = value, date = null)
                else -> GroupHuntingDayId(remoteId = null, date = LocalDate.fromEpochSeconds(-value))
            }
        }
    }
}

object GroupHuntingDayIdSerializer: KSerializer<GroupHuntingDayId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("GroupHuntingDayId", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): GroupHuntingDayId {
        val value = decoder.decodeLong()
        return GroupHuntingDayId.fromLong(value)
    }

    override fun serialize(encoder: Encoder, value: GroupHuntingDayId) {
        encoder.encodeLong(value.toLong())
    }
}

fun LocalDate.toLocalHuntingDay(
    huntingGroupId: HuntingGroupId,
    speciesCode: SpeciesCode,
    now: LocalDateTime,
): GroupHuntingDay {
    val startDateTime = LocalDateTime(
        date = this,
        time = when {
            speciesCode.isDeer() -> LocalTime(0, 0, 0)
            else -> LocalTime(6, 0, 0)
        }
    ).coerceAtMost(now)

    val endDateTime = LocalDateTime(
        date = this,
        time = when {
            speciesCode.isDeer() -> LocalTime(23, 59, 59)
            else -> LocalTime(21, 0, 0)
        }
    ).coerceAtMost(now)

    return GroupHuntingDay(
        id = GroupHuntingDayId.local(date = this),
        type = Entity.Type.LOCAL,
        rev = null,
        huntingGroupId = huntingGroupId,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        breakDurationInMinutes = null,
        snowDepth = null,
        huntingMethod = BackendEnum.create(null),
        numberOfHunters = null,
        numberOfHounds = null,
        createdBySystem = false,
    )
}

fun GroupHuntingDayId.toLocalHuntingDay(
    huntingGroupId: HuntingGroupId,
    speciesCode: SpeciesCode,
    now: LocalDateTime,
): GroupHuntingDay? {
    return when {
        isLocal() && date != null -> date.toLocalHuntingDay(huntingGroupId, speciesCode, now)
        else -> null
    }
}