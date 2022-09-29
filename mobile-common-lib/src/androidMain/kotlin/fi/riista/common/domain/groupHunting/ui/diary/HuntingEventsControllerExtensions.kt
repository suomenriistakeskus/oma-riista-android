package fi.riista.common.domain.groupHunting.ui.diary

import android.os.Bundle
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson

fun DiaryController.saveToBundle(bundle: Bundle) {
    getUnreproducibleState()
        ?.serializeToJson()
        ?.let {
            bundle.putString(KeyControllerState, it)
        }
}

fun DiaryController.loadFromBundle(bundle: Bundle) {
    bundle.getString(KeyControllerState)
        ?.deserializeFromJson<DiaryController.State>()
        ?.let { state ->
            restoreUnreproducibleState(state)
        }
}

private const val KeyControllerState = "HEC_key_controller_state"
