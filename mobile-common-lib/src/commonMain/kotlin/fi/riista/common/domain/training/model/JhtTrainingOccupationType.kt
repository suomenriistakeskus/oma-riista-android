package fi.riista.common.domain.training.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class JhtTrainingOccupationType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    METSASTYKSENVALVOJA(
        rawBackendEnumValue = "METSASTYKSENVALVOJA",
        resourcesStringId = RR.string.jht_training_occupation_type_metsastyksenvalvoja,
    ),
    METSASTAJATUTKINNON_VASTAANOTTAJA(
        rawBackendEnumValue = "METSASTAJATUTKINNON_VASTAANOTTAJA",
        resourcesStringId = RR.string.jht_training_occupation_type_metsastajatutkinnon_vastaanottaja
    ),
    AMPUMAKOKEEN_VASTAANOTTAJA(
        rawBackendEnumValue = "AMPUMAKOKEEN_VASTAANOTTAJA",
        resourcesStringId = RR.string.jht_training_occupation_type_ampumakokeen_vastaanottaja
    ),
    RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA(
        rawBackendEnumValue = "RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA",
        resourcesStringId = RR.string.jht_training_occupation_type_rhyn_edustaja_riistavahinkojen_maastokatselmuksessa,
    ),
    ;
}
