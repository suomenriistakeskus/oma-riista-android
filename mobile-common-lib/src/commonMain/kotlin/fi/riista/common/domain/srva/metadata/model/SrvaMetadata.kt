package fi.riista.common.domain.srva.metadata.model

import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.model.BackendEnum

data class SrvaMetadata(
    /**
     * Species for which SRVA events can be created.
     */
    val species: List<Species.Known>,

    /**
     * Possible age values for specimen.
     */
    val ages: List<BackendEnum<GameAge>>,

    /**
     * Possible gender values for specimen.
     */
    val genders: List<BackendEnum<Gender>>,

    /**
     * Possible SRVA event categories and their definitions.
     */
    val eventCategories: List<SrvaEventCategory>,
) {
    fun hasCategory(type: BackendEnum<SrvaEventCategoryType>): Boolean =
        getCategory(type) != null

    fun getCategory(type: BackendEnum<SrvaEventCategoryType>): SrvaEventCategory? =
        eventCategories.firstOrNull { it.categoryType == type }
}
