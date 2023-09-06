package fi.riista.common.domain.huntingControl.ui

import fi.riista.common.domain.huntingControl.model.HuntingControlHunterInfo

sealed class HuntingControlHunterInfoResponse {
    enum class ErrorReason {
        NOT_FOUND,
        NETWORK_ERROR,
    }
    data class Success(val hunter: HuntingControlHunterInfo): HuntingControlHunterInfoResponse()
    data class Error(val reason: ErrorReason): HuntingControlHunterInfoResponse()
}
