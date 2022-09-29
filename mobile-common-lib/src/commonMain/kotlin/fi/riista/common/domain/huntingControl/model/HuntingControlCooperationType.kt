package fi.riista.common.domain.huntingControl.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class HuntingControlCooperationType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
) : RepresentsBackendEnum, LocalizableEnum {
    POLIISI("POLIISI", RR.string.hunting_control_cooperation_type_poliisi),
    RAJAVARTIOSTO("RAJAVARTIOSTO", RR.string.hunting_control_cooperation_type_rajavartiosto),
    METSAHALLITUS("MH", RR.string.hunting_control_cooperation_type_mh),
    OMA("OMA", RR.string.hunting_control_cooperation_type_oma),
}
