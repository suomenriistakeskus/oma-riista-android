package fi.riista.mobile.feature.observation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.ui.modify.EditObservationController
import fi.riista.common.domain.observation.ui.modify.EditableObservation
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory

/**
 * A fragment for editing [CommonObservation].
 */
class EditObservationFragment
    : ModifyObservationFragment<
        EditObservationController,
        EditObservationFragment.Manager>() {

    interface Manager : BaseManager {
        fun onObservationSaveCompleted(observation: CommonObservation)
    }

    override val controller: EditObservationController by lazy {
        EditObservationController(
            userContext = RiistaSDK.currentUserContext,
            observationContext = RiistaSDK.observationContext,
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

        controller.editableObservation = getObservationFromArgs(requireArguments())

        return view
    }

    override fun notifyManagerAboutSuccessfulSave(observation: CommonObservation) {
        manager.onObservationSaveCompleted(observation)
    }

    override fun getManagerFromContext(context: Context): Manager {
        return context as Manager
    }

    companion object {
        private const val ARGS_PREFIX = "EditObservationFragment"
        private const val KEY_OBSERVATION = "${ARGS_PREFIX}_observation"

        fun create(editableObservation: EditableObservation): EditObservationFragment {
            return EditObservationFragment().apply {
                arguments = Bundle().also { bundle ->
                    editableObservation.serializeToBundleAsJson(bundle, key = KEY_OBSERVATION)
                }
            }
        }

        private fun getObservationFromArgs(arguments: Bundle): EditableObservation {
            return requireNotNull(arguments.deserializeJson(key = KEY_OBSERVATION)) {
                "Observation required in args"
            }
        }
    }
}
