package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class ObservationType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {

    NAKO("NAKO", RR.string.observation_type_nako),
    POIKUE("POIKUE", RR.string.observation_type_poikue),
    PARI("PARI", RR.string.observation_type_pari),
    JALKI("JALKI", RR.string.observation_type_jalki),
    ULOSTE("ULOSTE", RR.string.observation_type_uloste),
    AANI("AANI", RR.string.observation_type_aani),
    RIISTAKAMERA("RIISTAKAMERA", RR.string.observation_type_riistakamera),
    KOIRAN_RIISTATYO("KOIRAN_RIISTATYO", RR.string.observation_type_koiran_riistatyo),
    MAASTOLASKENTA("MAASTOLASKENTA", RR.string.observation_type_maastolaskenta),
    KOLMIOLASKENTA("KOLMIOLASKENTA", RR.string.observation_type_kolmiolaskenta),
    LENTOLASKENTA("LENTOLASKENTA", RR.string.observation_type_lentolaskenta),
    HAASKA("HAASKA", RR.string.observation_type_haaska),
    SYONNOS("SYONNOS", RR.string.observation_type_syonnos),
    KELOMISPUU("KELOMISPUU", RR.string.observation_type_kelomispuu),
    KIIMAKUOPPA("KIIMAKUOPPA", RR.string.observation_type_kiimakuoppa),
    MAKUUPAIKKA("MAKUUPAIKKA", RR.string.observation_type_makuupaikka),
    PATO("PATO", RR.string.observation_type_pato),
    PESA("PESA", RR.string.observation_type_pesa),
    PESA_KEKO("PESA_KEKO", RR.string.observation_type_pesa_keko),
    PESA_PENKKA("PESA_PENKKA", RR.string.observation_type_pesa_penkka),
    PESA_SEKA("PESA_SEKA", RR.string.observation_type_pesa_seka),
    SOIDIN("SOIDIN", RR.string.observation_type_soidin),
    LUOLASTO("LUOLASTO", RR.string.observation_type_luolasto),
    PESIMALUOTO("PESIMALUOTO", RR.string.observation_type_pesimaluoto),
    LEPAILYLUOTO("LEPAILYLUOTO", RR.string.observation_type_lepailyluoto),
    PESIMASUO("PESIMASUO", RR.string.observation_type_pesimasuo),
    MUUTON_AIKAINEN_LEPAILYALUE("MUUTON_AIKAINEN_LEPAILYALUE",
        RR.string.observation_type_muuton_aikainen_lepailyalue),
    RIISTANKULKUPAIKKA("RIISTANKULKUPAIKKA", RR.string.observation_type_riistankulkupaikka),

    // Metsäkanalintujen (engl. grouse) elinympäristöä kuvaavat havaintotyypit
    POIKUEYMPARISTO("POIKUEYMPARISTO", RR.string.observation_type_poikueymparisto),
    VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA("VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA",
        RR.string.observation_type_vaihtelevarakenteinen_mustikkametsa),
    KUUSISEKOTTEINEN_METSA("KUUSISEKOTTEINEN_METSA", RR.string.observation_type_kuusisekoitteinen_metsa),
    VAIHTELEVARAKENTEINEN_MANTYSEKOTTEINEN_METSA("VAIHTELEVARAKENTEINEN_MANTYSEKOTTEINEN_METSA",
        RR.string.observation_type_vaihtelevarakenteinen_mantysekoitteinen_metsa),
    VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA("VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA",
        RR.string.observation_type_vaihtelevarakenteinen_lehtipuusekoitteinen_metsa),
    SUON_REUNAMETSA("SUON_REUNAMETSA", RR.string.observation_type_suon_reunametsa),
    HAKOMAMANTY("HAKOMAMANTY", RR.string.observation_type_hakomamanty),
    RUOKAILUKOIVIKKO("RUOKAILUKOIVIKKO", RR.string.observation_type_ruokailukoivikko),
    LEPPAKUUSIMETSA_TAI_KOIVUKUUSIMETSA("LEPPAKUUSIMETSA_TAI_KOIVUKUUSIMETSA",
        RR.string.observation_type_leppakuusimetsa_tai_koivikuusimetsa),
    RUOKAILUPAJUKKO_TAI_KOIVIKKO("RUOKAILUPAJUKKO_TAI_KOIVIKKO",
        RR.string.observation_type_ruokailupajukko_tai_koivikko),

    MUU("MUU", RR.string.observation_type_muu),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<ObservationType> = value.toBackendEnum()
    }
}
