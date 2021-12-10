package fi.riista.common.model

enum class OccupationType(override val rawBackendEnumValue: String) : RepresentsBackendEnum {
    COORDINATOR("TOIMINNANOHJAAJA"),
    SHOOTING_TEST_OFFICIAL("AMPUMAKOKEEN_VASTAANOTTAJA"),
    GROUP_HUNTING_DIRECTOR("RYHMAN_METSASTYKSENJOHTAJA"),
    CLUB_CONTACT_PERSON("SEURAN_YHDYSHENKILO"),
    CLUB_MEMBER("SEURAN_JASEN"),

    // remember to add serialized names to MockEnums.kt in tests if adding new OccupationType!
}
