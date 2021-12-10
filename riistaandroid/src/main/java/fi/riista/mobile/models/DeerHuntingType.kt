package fi.riista.mobile.models

enum class DeerHuntingType {

    // In Finnish: "Kyttääminen / vahtiminen"
    STAND_HUNTING,

    // In Finnish: "Seuruemetsästys koiran kanssa"
    DOG_HUNTING,

    OTHER;

    companion object {
        @JvmStatic
        fun fromString(s: String?): DeerHuntingType? = if (s != null) valueOf(s) else null

        @JvmStatic
        fun toString(type: DeerHuntingType?): String? = type?.name
    }
}
