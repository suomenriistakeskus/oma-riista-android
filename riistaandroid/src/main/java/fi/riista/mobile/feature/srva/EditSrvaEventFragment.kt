@file:Suppress("SpellCheckingInspection")

package fi.riista.mobile.feature.srva

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.riista.common.RiistaSDK
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.ui.modify.EditSrvaEventController
import fi.riista.common.domain.srva.ui.modify.EditableSrvaEvent
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory

/**
 * A fragment for editing [CommonSrvaEvent].
 */
class EditSrvaEventFragment
    : ModifySrvaEventFragment<
        EditSrvaEventController,
        EditSrvaEventFragment.Manager>() {

    interface Manager : BaseManager {
        fun onSaveSrvaEvent(srvaEvent: CommonSrvaEvent)
    }

    override val controller: EditSrvaEventController by lazy {
        EditSrvaEventController(
            metadataProvider = RiistaSDK.metadataProvider,
            stringProvider = ContextStringProviderFactory.createForContext(requireContext())
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        controller.editableSrvaEvent = getSrvaEventFromArgs(requireArguments())

        return view
    }

    override fun onSaveButtonClicked() {
        val srvaEvent = controller.getValidatedSrvaEvent()
            ?: kotlin.run {
                return
            }

        manager.onSaveSrvaEvent(srvaEvent = srvaEvent)
    }

    override fun getManagerFromContext(context: Context): Manager {
        return context as Manager
    }

    companion object {
        private const val ARGS_PREFIX = "EditSrvaEventFragment"
        private const val KEY_SRVA_EVENT = "${ARGS_PREFIX}_srva_event"

        fun create(editableSrvaEvent: EditableSrvaEvent): EditSrvaEventFragment {
            return EditSrvaEventFragment().apply {
                arguments = Bundle().also { bundle ->
                    editableSrvaEvent.serializeToBundleAsJson(bundle, key = KEY_SRVA_EVENT)
                }
            }
        }

        private fun getSrvaEventFromArgs(arguments: Bundle): EditableSrvaEvent {
            return requireNotNull(arguments.deserializeJson(key = KEY_SRVA_EVENT)) {
                "SRVA event required in args"
            }
        }
    }
}
