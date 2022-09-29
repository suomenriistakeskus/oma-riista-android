package fi.riista.common.domain.specimens.ui

import fi.riista.common.ui.dataField.IndexedDataFieldId
import kotlinx.serialization.Serializable

@Serializable
data class SpecimenFieldId(
    override val type: SpecimenFieldType,
    override val index: Int = 0
) : IndexedDataFieldId<SpecimenFieldType>() {
    init {
        validateIndex()
    }

    companion object {
        fun fromInt(value: Int): SpecimenFieldId? {
            return toIndexedField(value) { type : SpecimenFieldType, index ->
                SpecimenFieldId(type, index)
            }
        }
    }
}
