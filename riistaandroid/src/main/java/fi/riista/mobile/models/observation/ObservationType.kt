package fi.riista.mobile.models.observation

enum class ObservationType {

    NAKO,
    JALKI,
    ULOSTE,
    AANI,
    RIISTAKAMERA,
    KOIRAN_RIISTATYO,
    MAASTOLASKENTA,
    KOLMIOLASKENTA,
    LENTOLASKENTA,
    HAASKA,
    SYONNOS,
    KELOMISPUU,
    KIIMAKUOPPA,
    MAKUUPAIKKA,
    PATO,
    PESA,
    PESA_KEKO,
    PESA_PENKKA,
    PESA_SEKA,
    SOIDIN,
    LUOLASTO,
    PESIMALUOTO,
    LEPAILYLUOTO,
    PESIMASUO,
    MUUTON_AIKAINEN_LEPAILYALUE,
    RIISTANKULKUPAIKKA,

    // Metsäkanalintujen (engl. grouse) elinympäristöä kuvaavat havaintotyypit
    POIKUEYMPARISTO,
    VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA,
    KUUSISEKOTTEINEN_METSA,
    VAIHTELEVARAKENTEINEN_MANTYSEKOTTEINEN_METSA,
    VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA,
    SUON_REUNAMETSA,
    HAKOMAMANTY,
    RUOKAILUKOIVIKKO,
    LEPPAKUUSIMETSA_TAI_KOIVUKUUSIMETSA,
    RUOKAILUPAJUKKO_TAI_KOIVIKKO,

    MUU;

    companion object {
        @JvmStatic
        fun fromString(s: String?): ObservationType? = if (s != null) valueOf(s) else null

        @JvmStatic
        fun toString(type: ObservationType?): String? = type?.name
    }
}
