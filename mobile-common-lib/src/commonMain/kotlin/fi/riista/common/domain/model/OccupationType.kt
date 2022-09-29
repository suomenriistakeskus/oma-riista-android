package fi.riista.common.domain.model

import fi.riista.common.model.RepresentsBackendEnum

enum class OccupationType(override val rawBackendEnumValue: String) : RepresentsBackendEnum {
    COORDINATOR("TOIMINNANOHJAAJA"),
    SHOOTING_TEST_OFFICIAL("AMPUMAKOKEEN_VASTAANOTTAJA"),
    GROUP_HUNTING_DIRECTOR("RYHMAN_METSASTYKSENJOHTAJA"),
    CLUB_CONTACT_PERSON("SEURAN_YHDYSHENKILO"),
    CLUB_MEMBER("SEURAN_JASEN"),
    CARNIVORE_AUTHORITY("PETOYHDYSHENKILO"),

    // remember to add serialized names to MockEnums.kt in tests if adding new OccupationType!
}
