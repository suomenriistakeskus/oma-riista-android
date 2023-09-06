package fi.riista.common.util

import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.model.LocalizedString

internal fun LocalizedString.toLocalizedStringDTO() = LocalizedStringDTO(fi, sv, en)
