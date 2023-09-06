package fi.riista.common.domain.dao

import fi.riista.common.domain.model.Address
import kotlinx.serialization.Serializable


/**
 * Remember to bump DAO_VERSION if/when making backwards incompatible changes.
 *
 * Also check where this class has been used in order to bump those versions as well.
 */
@Serializable
internal data class AddressDAO(
    val id: Long,
    val rev: Int,
    val editable: Boolean,
    val streetAddress: String?,
    val postalCode: String?,
    val city: String?,
    val country: String?,
) {
    companion object {
        internal const val DAO_VERSION = 1
    }
}

internal fun Address.toAddressDAO(): AddressDAO {
    return AddressDAO(
        id = id,
        rev = rev,
        editable = editable,
        streetAddress = streetAddress,
        postalCode = postalCode,
        city = city,
        country = country,
    )
}

internal fun AddressDAO.toAddress(): Address {
    return Address(
        id = id,
        rev = rev,
        editable = editable,
        streetAddress = streetAddress,
        postalCode = postalCode,
        city = city,
        country = country,
    )
}

