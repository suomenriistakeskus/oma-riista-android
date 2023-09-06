package fi.riista.common.domain.harvest.ui.view

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.CommonHarvestId
import fi.riista.common.domain.harvest.model.toCommonHarvestData
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields
import fi.riista.common.domain.permit.harvestPermit.HarvestPermitProvider
import fi.riista.common.domain.season.HarvestSeasons
import fi.riista.common.logging.getLogger
import fi.riista.common.preferences.Preferences
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * A controller for viewing [CommonHarvest] information
 */
class ViewHarvestController internal constructor(
    private val harvestId: CommonHarvestId,
    private val harvestContext: HarvestContext,
    private val commonHarvestFields: CommonHarvestFields,
    harvestPermitProvider: HarvestPermitProvider,
    stringProvider: StringProvider,
    languageProvider: LanguageProvider,
) : ControllerWithLoadableModel<ViewHarvestViewModel>() {

    // main constructor to be used from outside
    constructor(
        harvestId: CommonHarvestId,
        harvestContext: HarvestContext,
        harvestSeasons: HarvestSeasons,
        speciesResolver: SpeciesResolver,
        harvestPermitProvider: HarvestPermitProvider,
        preferences: Preferences,
        stringProvider: StringProvider,
        languageProvider: LanguageProvider,
    ): this(
        harvestId = harvestId,
        harvestContext = harvestContext,
        commonHarvestFields = CommonHarvestFields(
            harvestSeasons = harvestSeasons,
            speciesResolver = speciesResolver,
            preferences = preferences,
        ),
        harvestPermitProvider = harvestPermitProvider,
        stringProvider = stringProvider,
        languageProvider = languageProvider,
    )

    private val dataFieldProducer = ViewHarvestFieldProducer(harvestPermitProvider, stringProvider, languageProvider)

    suspend fun deleteHarvest(updateToBackend: Boolean): Boolean {
        val harvestId = getLoadedViewModelOrNull()?.harvest?.localId ?: kotlin.run {
            logger.w { "No harvest found, cannot delete" }
            return false
        }

        val deletedHarvest = harvestContext.deleteHarvest(harvestId)
        return if (deletedHarvest != null) {
            if (updateToBackend) {
                harvestContext.deleteHarvestInBackend(deletedHarvest)
            }
            true
        } else {
            false
        }
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ViewHarvestViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        harvestContext.harvestProvider.fetch(refresh = refresh)

        val harvestData = harvestContext.harvestProvider.getByLocalId(harvestId)?.toCommonHarvestData()
        if (harvestData != null) {
            emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    harvest = harvestData,
                )
            ))
        } else {
            logger.w { "Did you forget to set harvest before loading viewModel?" }
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    private fun createViewModel(
        harvest: CommonHarvestData,
    ): ViewHarvestViewModel {
        return ViewHarvestViewModel(
            harvest = harvest,
            fields = produceDataFields(harvest),
            canEdit = harvest.canEdit,
        )
    }

    private fun produceDataFields(harvest: CommonHarvestData): List<DataField<CommonHarvestField>> {
        val fieldsToBeDisplayed = commonHarvestFields.getFieldsToBeDisplayed(
            harvest = harvest,
            mode = CommonHarvestFields.Context.Mode.VIEW,
            ownHarvest = harvest.actorInfo == GroupHuntingPerson.Unknown
        )

        return fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
            dataFieldProducer.createField(
                fieldSpecification = fieldSpecification,
                harvest = harvest
            )
        }
    }

    companion object {
        private val logger by getLogger(ViewHarvestController::class)
    }
}

