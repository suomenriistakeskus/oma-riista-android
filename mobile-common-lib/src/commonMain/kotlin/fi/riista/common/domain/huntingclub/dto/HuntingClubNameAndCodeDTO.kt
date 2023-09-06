package fi.riista.common.domain.huntingclub.dto

import fi.riista.common.domain.model.Organization
import fi.riista.common.model.LocalizedString
import kotlinx.serialization.Serializable

@Serializable
internal data class HuntingClubNameAndCodeDTO(
    val id: Long,
    /**
     * Official code should exist when receiving club information. If not, consider that as an error.
     *
     * Depending on API it is not necessary when sending information (e.g. when updating harvest)
     */
    val officialCode: String? = null,
    val nameFI: String? = null,
    val nameSV: String? = null,
)

internal fun HuntingClubNameAndCodeDTO.toOrganization(): Organization? {
    if (officialCode == null) {
        return null
    }

    return Organization(
        id = id,
        name = LocalizedString(
            fi = nameFI,
            sv = nameSV,
            en = null,
        ),
        officialCode = officialCode,
    )
}

internal fun Organization.toHuntingClubNameAndCodeDTO() =
    HuntingClubNameAndCodeDTO(
        id = id,
        officialCode = officialCode,
        nameFI = name.fi,
        nameSV = name.sv,
    )
