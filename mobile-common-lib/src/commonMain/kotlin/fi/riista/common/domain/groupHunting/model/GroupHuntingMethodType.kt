package fi.riista.common.domain.groupHunting.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class GroupHuntingMethodType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,

    /**
     * Does the hunting method require at least one hound?
     */
    val requiresHound: Boolean,
) : RepresentsBackendEnum, LocalizableEnum {

    PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA(
            rawBackendEnumValue = "PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA",
            resourcesStringId = RR.string.group_hunting_method_passilinja_koira_ohjaajineen_metsassa,
            requiresHound = true,
    ),
    HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE(
            rawBackendEnumValue = "HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE",
            resourcesStringId = RR.string.group_hunting_method_hiipiminen_pysayttavalle_koiralle,
            requiresHound = true,
    ),
    PASSILINJA_JA_TIIVIS_AJOKETJU(
            rawBackendEnumValue = "PASSILINJA_JA_TIIVIS_AJOKETJU",
            resourcesStringId = RR.string.group_hunting_method_passilinja_ja_tiivis_ajoketju,
            requiresHound = false,
    ),
    PASSILINJA_JA_MIESAJO_JALJITYKSENA(
            rawBackendEnumValue = "PASSILINJA_JA_MIESAJO_JALJITYKSENA",
            resourcesStringId = RR.string.group_hunting_method_passilinja_ja_miesajo_jaljityksena,
            requiresHound = false,
    ),
    JALJITYS_ELI_NAAKIMINEN_ILMAN_PASSEJA(
            rawBackendEnumValue = "JALJITYS_ELI_NAAKIMINEN_ILMAN_PASSEJA",
            resourcesStringId = RR.string.group_hunting_method_jaljitys_eli_naakiminen_ilman_passeja,
            requiresHound = false,
    ),
    VAIJYNTA_KULKUPAIKOILLA(
            rawBackendEnumValue = "VAIJYNTA_KULKUPAIKOILLA",
            resourcesStringId = RR.string.group_hunting_method_vaijynta_kulkupaikoilla,
            requiresHound = false,
    ),
    VAIJYNTA_RAVINTOKOHTEILLA(
            rawBackendEnumValue = "VAIJYNTA_RAVINTOKOHTEILLA",
            resourcesStringId = RR.string.group_hunting_method_vaijynta_ravintokohteilla,
            requiresHound = false,
    ),
    HOUKUTTELU(
            rawBackendEnumValue = "HOUKUTTELU",
            resourcesStringId = RR.string.group_hunting_method_houkuttelu,
            requiresHound = false,
    ),
    MUU(
            rawBackendEnumValue = "MUU",
            resourcesStringId = RR.string.group_hunting_method_muu,
            requiresHound = false,
    ),
}
