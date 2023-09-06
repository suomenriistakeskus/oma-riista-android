package fi.riista.common.domain.model

import fi.riista.common.domain.constants.Constants
import kotlinx.serialization.Serializable

@Serializable
sealed class SearchableOrganization {

    val organization: Organization?
        get() {
            return when (this) {
                is Found -> result
                is Searching, Unknown -> null
            }
        }

    fun exists() = organization != null

    @Serializable
    object Unknown : SearchableOrganization() {
        // id for StringWithId
        const val ID: Long = -90
    }

    @Serializable
    data class Found(
        val result: Organization,
    ): SearchableOrganization()

    @Serializable
    data class Searching(
        val officialCode: String,
        val status: Status,
    ): SearchableOrganization() {

        enum class Status {
            ENTERING_OFFICIAL_CODE,
            INVALID_OFFICIAL_CODE,
            VALID_OFFICIAL_CODE_ENTERED,
            SEARCHING,
            SEARCH_FAILED,
        }

        fun withUpdatedOfficialCode(updatedOfficialCode: String): Searching {
            return when (status) {
                Status.ENTERING_OFFICIAL_CODE,
                Status.INVALID_OFFICIAL_CODE,
                Status.SEARCH_FAILED -> {
                    val numberCount = updatedOfficialCode.trim().length
                    when {
                        numberCount == Constants.HUNTING_CLUB_OFFICIAL_CODE_LENGTH -> {
                            Searching(
                                officialCode = updatedOfficialCode,
                                status = Status.VALID_OFFICIAL_CODE_ENTERED
                            )
                        }
                        numberCount > Constants.HUNTING_CLUB_OFFICIAL_CODE_LENGTH -> {
                            Searching(
                                officialCode = updatedOfficialCode,
                                status = Status.INVALID_OFFICIAL_CODE
                            )
                        }
                        else -> {
                            Searching(
                                officialCode = updatedOfficialCode,
                                status = Status.ENTERING_OFFICIAL_CODE
                            )
                        }
                    }
                }
                // don't allow updating official code if valid or already searching
                Status.VALID_OFFICIAL_CODE_ENTERED,
                Status.SEARCHING -> this
            }
        }

        companion object {
            // id for StringWithId
            const val ID: Long = -100

            fun startSearch() = Searching(officialCode = "", status = Status.ENTERING_OFFICIAL_CODE)
        }
    }
}

internal fun Organization?.asSearchableOrganization(): SearchableOrganization {
    return if (this != null) {
        SearchableOrganization.Found(result = this)
    } else {
        SearchableOrganization.Unknown
    }
}
