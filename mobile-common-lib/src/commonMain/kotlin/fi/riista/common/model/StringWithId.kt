package fi.riista.common.model

import kotlinx.serialization.Serializable

typealias StringId = Long

@Serializable
data class StringWithId(val string: String, val id: StringId) {
    // empty companion object so that it can be extended if necessary
    internal companion object {}
}
