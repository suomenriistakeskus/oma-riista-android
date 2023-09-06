package fi.riista.common.domain.specimens.ui

enum class SpecimenFieldType {
    SPECIMEN_HEADER,
    GENDER,
    AGE,
    WEIGHT,
    WIDTH_OF_PAW,
    LENGTH_OF_PAW,
    STATE_OF_HEALTH,
    MARKING,
    ;

    fun toField(index: Int) = SpecimenFieldId(type = this, index = index)
}
