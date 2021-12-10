package fi.riista.common.groupHunting.model

import fi.riista.common.model.*
import kotlinx.serialization.Serializable

/**
 * Information about Person who is somehow related to a group hunting diary entry
 * (e.g. GroupHuntingHarvest)
 *
 * The main purpose of this class is to encapsulate lookup state i.e. when person
 * is not part of the group (guest hunters) and is being looked up by the hunter number.
 */
@Serializable
sealed class GroupHuntingPerson {

    @Serializable
    object Unknown : GroupHuntingPerson()

    @Serializable
    data class GroupMember(
        val personInformation: PersonWithHunterNumber,
    ): GroupHuntingPerson()

    @Serializable
    data class Guest(
        val personInformation: PersonWithHunterNumber,
    ): GroupHuntingPerson()

    @Serializable
    data class SearchingByHunterNumber(
        val hunterNumber: HunterNumber,
        val status: Status,
    ): GroupHuntingPerson() {

        enum class Status {
            ENTERING_HUNTER_NUMBER,
            INVALID_HUNTER_NUMBER,
            VALID_HUNTER_NUMBER_ENTERED,
            SEARCHING_PERSON_BY_HUNTER_NUMBER,
            SEARCH_FAILED,
        }

        fun withUpdatedHunterNumber(updatedHunterNumber: HunterNumber): SearchingByHunterNumber {
            return when (status) {
                Status.ENTERING_HUNTER_NUMBER,
                Status.INVALID_HUNTER_NUMBER,
                Status.SEARCH_FAILED -> {
                    val numberCount = updatedHunterNumber.trim().length
                    when {
                        numberCount == HUNTER_NUMBER_LENGTH -> {
                            val status = if (updatedHunterNumber.isHunterNumberValid()) {
                                Status.VALID_HUNTER_NUMBER_ENTERED
                            } else {
                                Status.INVALID_HUNTER_NUMBER
                            }
                            SearchingByHunterNumber(
                                hunterNumber = updatedHunterNumber,
                                status = status
                            )
                        }
                        numberCount > HUNTER_NUMBER_LENGTH -> {
                            SearchingByHunterNumber(
                                hunterNumber = updatedHunterNumber,
                                status = Status.INVALID_HUNTER_NUMBER
                            )
                        }
                        else -> {
                            SearchingByHunterNumber(
                                hunterNumber = updatedHunterNumber,
                                status = Status.ENTERING_HUNTER_NUMBER
                            )
                        }
                    }
                }
                // don't allow updating hunter number
                Status.VALID_HUNTER_NUMBER_ENTERED,
                Status.SEARCHING_PERSON_BY_HUNTER_NUMBER -> this
            }
        }

        companion object {
            // id for StringWithId
            const val ID: Long = -100

            fun startSearch() =
                SearchingByHunterNumber(hunterNumber = "", status = Status.ENTERING_HUNTER_NUMBER)
        }
    }

    val personWithHunterNumber: PersonWithHunterNumber?
        get() {
            return when (this) {
                is GroupMember -> personInformation
                is Guest -> personInformation
                is SearchingByHunterNumber, Unknown -> null
            }
        }
}


internal fun PersonWithHunterNumber.asGroupMember() = GroupHuntingPerson.GroupMember(this)
internal fun PersonWithHunterNumber.asGuest() = GroupHuntingPerson.Guest(this)