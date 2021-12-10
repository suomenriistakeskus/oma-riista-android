package fi.riista.mobile.models.observation

enum class ObservationCategory {

    NORMAL,

    // Observation is done within moose hunting
    MOOSE_HUNTING,

    // Observation is done within white-tailed deer hunting
    DEER_HUNTING;

    companion object {
        @JvmStatic
        fun fromString(s: String?): ObservationCategory? = if (s != null) valueOf(s) else null

        @JvmStatic
        fun toString(type: ObservationCategory?): String? = type?.name
    }
}
