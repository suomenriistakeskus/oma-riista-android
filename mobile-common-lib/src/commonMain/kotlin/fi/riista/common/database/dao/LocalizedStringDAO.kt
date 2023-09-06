package fi.riista.common.database.dao

import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.model.LocalizedString
import kotlinx.serialization.Serializable


/**
 * Remember to bump DAO_VERSION if/when making backwards incompatible changes.
 *
 * Also check where this class has been used in order to bump those versions as well.
 */
@Serializable
internal data class LocalizedStringDAO(
    val fi: String?,
    val sv: String?,
    val en: String?,
) {
    companion object {
        internal const val DAO_VERSION = 1
    }
}

internal fun LocalizedStringDTO.toLocalizedStringDAO() = LocalizedStringDAO(fi = fi, sv = sv, en = en)
internal fun LocalizedStringDAO.toLocalizedStringDTO() = LocalizedStringDTO(fi = fi, sv = sv, en = en)

internal fun LocalizedString.toLocalizedStringDAO() = LocalizedStringDAO(fi = fi, sv = sv, en = en)
internal fun LocalizedStringDAO.toLocalizedString() = LocalizedString(fi = fi, sv = sv, en = en)