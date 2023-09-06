package fi.riista.common.domain.huntingControl.ui.hunterInfo

import fi.riista.common.domain.huntingControl.model.HuntingControlHunterInfo
import fi.riista.common.domain.model.HUNTER_NUMBER_LENGTH
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.isHunterNumberValid
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields
import kotlinx.serialization.Serializable

@Serializable
sealed class SearchTerm {
    @Serializable
    data class SearchableHunterNumber(
        val hunterNumber: HunterNumber
    ): SearchTerm()
    @Serializable
    data class SearchableSsn(
        val ssn: String
    ): SearchTerm()
}

@Serializable
data class HunterSearch(
    val searchTerm: SearchTerm,
    val status: Status
) {
    enum class Status {
        ENTERING_HUNTER_NUMBER,
        INVALID_HUNTER_NUMBER,
        VALID_SEARCH_TERM_ENTERED,
        SEARCHING_PERSON_BY_HUNTER_NUMBER,
        SEARCHING_PERSON_BY_SSN,
        SEARCH_FAILED_HUNTER_NOT_FOUND,
        SEARCH_FAILED_NETWORK_ERROR,
        SSN_SEARCH_FAILED_HUNTER_NOT_FOUND,
        SSN_SEARCH_FAILED_NETWORK_ERROR,
        HUNTER_FOUND,
    }

    fun withUpdatedHunterNumber(updatedHunterNumber: HunterNumber): HunterSearch {
        return when (status) {
            Status.ENTERING_HUNTER_NUMBER,
            Status.INVALID_HUNTER_NUMBER,
            Status.SEARCH_FAILED_HUNTER_NOT_FOUND,
            Status.SEARCH_FAILED_NETWORK_ERROR -> {
                val numberCount = updatedHunterNumber.trim().length
                when {
                    numberCount == HUNTER_NUMBER_LENGTH -> {
                        val status = if (updatedHunterNumber.isHunterNumberValid()) {
                            Status.VALID_SEARCH_TERM_ENTERED
                        } else {
                            Status.INVALID_HUNTER_NUMBER
                        }
                        HunterSearch(
                            searchTerm = SearchTerm.SearchableHunterNumber(updatedHunterNumber),
                            status = status
                        )
                    }
                    numberCount > HUNTER_NUMBER_LENGTH -> {
                        HunterSearch(
                            searchTerm = SearchTerm.SearchableHunterNumber(updatedHunterNumber),
                            status = Status.INVALID_HUNTER_NUMBER
                        )
                    }
                    else -> {
                        HunterSearch(
                            searchTerm = SearchTerm.SearchableHunterNumber(updatedHunterNumber),
                            status = Status.ENTERING_HUNTER_NUMBER
                        )
                    }
                }
            }
            // don't allow updating hunter number
            Status.SEARCHING_PERSON_BY_SSN,
            Status.SSN_SEARCH_FAILED_HUNTER_NOT_FOUND,
            Status.SSN_SEARCH_FAILED_NETWORK_ERROR,
            Status.VALID_SEARCH_TERM_ENTERED,
            Status.HUNTER_FOUND,
            Status.SEARCHING_PERSON_BY_HUNTER_NUMBER -> this
        }
    }
}

data class HunterInfoViewModel(
    internal val hunterInfo: HuntingControlHunterInfo?,
    internal val hunterSearch: HunterSearch,
    override val fields: DataFields<HunterInfoField>,
) : DataFieldViewModel<HunterInfoField>()
