package fi.riista.common.domain.srva.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum

enum class SrvaEventState(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    UNFINISHED("UNFINISHED"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),

    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<SrvaEventState> = value.toBackendEnum()
    }
}
