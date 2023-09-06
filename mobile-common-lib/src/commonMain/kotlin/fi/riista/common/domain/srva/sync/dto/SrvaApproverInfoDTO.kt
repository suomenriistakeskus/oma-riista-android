package fi.riista.common.domain.srva.sync.dto

import fi.riista.common.domain.srva.model.CommonSrvaEventApprover
import kotlinx.serialization.Serializable

@Serializable
data class SrvaApproverInfoDTO(
    val firstName: String? = null,
    val lastName: String? = null,
)

fun SrvaApproverInfoDTO.toCommonSrvaEventApprover(): CommonSrvaEventApprover {
    return CommonSrvaEventApprover(
        firstName = firstName,
        lastName = lastName,
    )
}

fun CommonSrvaEventApprover.toSrvaApproverInfoDTO(): SrvaApproverInfoDTO {
    return SrvaApproverInfoDTO(
        firstName = firstName,
        lastName = lastName,
    )
}
