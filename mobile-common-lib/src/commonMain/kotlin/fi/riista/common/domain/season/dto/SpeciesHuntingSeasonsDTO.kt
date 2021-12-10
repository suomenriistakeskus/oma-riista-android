package fi.riista.common.domain.season.dto

import fi.riista.common.dto.SpeciesCodeDTO
import kotlinx.serialization.Serializable

@Serializable
data class SpeciesHuntingSeasonsDTO(
    val speciesCode: SpeciesCodeDTO,
    val huntingSeasons: List<HuntingSeasonDTO>,
)
