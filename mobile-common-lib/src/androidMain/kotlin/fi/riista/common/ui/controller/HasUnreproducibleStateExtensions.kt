package fi.riista.common.ui.controller

import android.os.Bundle
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson

inline fun <reified State> HasUnreproducibleState<State>.saveToBundle(bundle: Bundle,
                                                                      keyPrefix: String) {
    getUnreproducibleState()
        ?.serializeToJson()
        ?.let {
            bundle.putString("${keyPrefix}_unreproducible_state", it)
        }
}

inline fun <reified State> HasUnreproducibleState<State>.restoreFromBundle(bundle: Bundle,
                                                                           keyPrefix: String) {
    bundle.getString("${keyPrefix}_unreproducible_state")
        ?.deserializeFromJson<State>()
        ?.let { state ->
            restoreUnreproducibleState(state)
        }
}