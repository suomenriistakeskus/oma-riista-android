package fi.riista.mobile.feature.harvest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.ui.modify.EditHarvestController
import fi.riista.common.domain.harvest.ui.modify.EditableHarvest
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.mobile.riistaSdkHelpers.AppSpeciesResolver
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory

/**
 * A fragment for editing [CommonHarvest].
 */
class EditHarvestFragment
    : ModifyHarvestFragment<
        EditHarvestController,
        EditHarvestFragment.Manager>() {

    interface Manager : BaseManager {
        fun onHarvestSaveCompleted(harvest: CommonHarvest)
    }

    override val controller: EditHarvestController by lazy {
        EditHarvestController(
            harvestSeasons = RiistaSDK.harvestSeasons,
            harvestContext = RiistaSDK.harvestContext,
            permitProvider = permitProvider,
            speciesResolver = AppSpeciesResolver(),
            stringProvider = ContextStringProviderFactory.createForContext(requireContext())
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        controller.editableHarvest = getHarvestFromArgs(requireArguments())

        return view
    }

    override fun notifyManagerAboutSuccessfulSave(harvest: CommonHarvest) {
        manager.onHarvestSaveCompleted(harvest)
    }

    override fun getManagerFromContext(context: Context): Manager {
        return context as Manager
    }

    companion object {
        private const val ARGS_PREFIX = "EditHarvestFragment"
        private const val KEY_HARVEST = "${ARGS_PREFIX}_harvest"

        fun create(editableHarvest: EditableHarvest): EditHarvestFragment {
            return EditHarvestFragment().apply {
                arguments = Bundle().also { bundle ->
                    editableHarvest.serializeToBundleAsJson(bundle, key = KEY_HARVEST)
                }
            }
        }

        private fun getHarvestFromArgs(arguments: Bundle): EditableHarvest {
            return requireNotNull(arguments.deserializeJson(key = KEY_HARVEST)) {
                "Harvest required in args"
            }
        }
    }
}
