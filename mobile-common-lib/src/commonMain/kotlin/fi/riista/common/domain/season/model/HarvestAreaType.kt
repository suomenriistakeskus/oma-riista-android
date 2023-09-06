package fi.riista.common.domain.season.model

import fi.riista.common.model.RepresentsBackendEnum

enum class HarvestAreaType(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    HALLIALUE("HALLIALUE"),
    PORONHOITOALUE("PORONHOITOALUE"),
    NORPPAALUE("NORPPAALUE"),
}