package fi.riista.common.domain.huntingControl.ui.hunterInfo

import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.model.HuntingControlHunterInfo
import fi.riista.common.domain.huntingControl.ui.HuntingControlHunterInfoResponse
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.FieldSpecificationListBuilder
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class HunterInfoController(
    val huntingControlContext: HuntingControlContext,
    languageProvider: LanguageProvider,
    stringProvider: StringProvider,
) : ControllerWithLoadableModel<HunterInfoViewModel>(),
    IntentHandler<HunterInfoIntent>,
    HasUnreproducibleState<HunterInfoController.State> {

    val eventDispatcher: HunterInfoDispatcher = HunterInfoEventToIntentMapper(intentHandler = this)
    val intEventDispatcher: HunterInfoIntEventDispatcher = HunterInfoIntEventToIntentMapper(intentHandler = this)
    val actionEventDispatcher: HunterInfoActionEventDispatcher = HunterInfoActionEventToIntentMapper(intentHandler = this)

    private var stateToRestore: State? = null
    private val fieldProducer = HunterInfoFieldProducer(
        languageProvider = languageProvider,
        stringProvider = stringProvider,
    )

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<HunterInfoViewModel>> = flow {

        // update the state which we'll try to restore. This needs to be done before entering
        // loading state as we may want to restore the current state which is only available
        // if already in loaded state
        updateStateToRestore()

        val hunter = stateToRestore?.hunter
        val hunterSearch = stateToRestore?.hunterSearch
            ?: HunterSearch(
                searchTerm = SearchTerm.SearchableHunterNumber(""),
                status = HunterSearch.Status.ENTERING_HUNTER_NUMBER,
            )

        // state restoration was done -> Clear the stateToRestore in order to NOT use
        // same values again when restoring.
        clearStateToRestore()

        emit(
            ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    hunter = hunter,
                    hunterSearch = hunterSearch,
                )
            )
        )

        // If we were searching when state was stored then restart the search
        if (hunterSearch.status in listOf(HunterSearch.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER,
                                          HunterSearch.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER)
        ) {
            updateViewModelSuspended {
                search()
            }
        }
    }

    override fun handleIntent(intent: HunterInfoIntent) {
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            val updateViewModel = when (intent) {
                is HunterInfoIntent.ChangeHunterNumber -> {
                    val newHunterNumber = intent.hunterNumber?.toString() ?: ""
                    val updatedSearch = viewModel.hunterSearch.withUpdatedHunterNumber(newHunterNumber)
                    if (updatedSearch.status == HunterSearch.Status.VALID_SEARCH_TERM_ENTERED) {
                        updateViewModelSuspended {
                            search()
                        }
                    }
                    createViewModel(viewModel.hunterInfo, updatedSearch)
                }
                is HunterInfoIntent.SearchByHunterNumber -> {
                    val newHunterNumber = intent.hunterNumber
                    val updatedSearch = viewModel.hunterSearch.withUpdatedHunterNumber(newHunterNumber)
                    if (updatedSearch.status == HunterSearch.Status.VALID_SEARCH_TERM_ENTERED) {
                        updateViewModelSuspended {
                            search()
                        }
                    }
                    createViewModel(viewModel.hunterInfo, updatedSearch)
                }
                is HunterInfoIntent.SearchBySsn -> {
                    val updatedSearch = HunterSearch(
                        searchTerm = SearchTerm.SearchableSsn(ssn = intent.ssn),
                        status = HunterSearch.Status.VALID_SEARCH_TERM_ENTERED,
                    )
                    updateViewModelSuspended {
                        search()
                    }
                    createViewModel(viewModel.hunterInfo, updatedSearch)
                }
                is HunterInfoIntent.Reset -> {
                    createViewModel(
                        null,
                        HunterSearch(
                            searchTerm = SearchTerm.SearchableHunterNumber(""),
                            status = HunterSearch.Status.ENTERING_HUNTER_NUMBER,
                        )
                    )
                }
                is HunterInfoIntent.Retry -> {
                    updateViewModelSuspended {
                        search()
                    }
                    createViewModel(viewModel.hunterInfo, viewModel.hunterSearch)
                }
            }
            updateViewModel(
                ViewModelLoadStatus.Loaded(
                    viewModel = updateViewModel
                )
            )
        }
    }

    private suspend fun search() {
        val viewModel = getLoadedViewModelOrNull() ?: kotlin.run {
            logger.d { "Cannot search hunter by hunter number. No loaded viewmodel!" }
            return
        }

        when (viewModel.hunterSearch.searchTerm) {
            is SearchTerm.SearchableHunterNumber -> searchByHunterNumber(viewModel, viewModel.hunterSearch.searchTerm)
            is SearchTerm.SearchableSsn -> searchBySsn(viewModel, viewModel.hunterSearch.searchTerm)
        }
    }

    private suspend fun searchByHunterNumber(viewModel: HunterInfoViewModel, searchTerm: SearchTerm.SearchableHunterNumber) {
        val searchingHunterNumber = viewModel.hunterSearch.copy(
            status = HunterSearch.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER
        )
        updateViewModel(
            ViewModelLoadStatus.Loaded(
                createViewModel(
                    hunter = null,
                    hunterSearch = searchingHunterNumber
                )
            )
        )

        val hunterNumber = searchTerm.hunterNumber
        val response = huntingControlContext.fetchHunterInfoByHunterNumber(hunterNumber)

        val status = when (response) {
            is HuntingControlHunterInfoResponse.Success -> {
                HunterSearch.Status.HUNTER_FOUND
            }
            is HuntingControlHunterInfoResponse.Error -> {
                when (response.reason) {
                    HuntingControlHunterInfoResponse.ErrorReason.NOT_FOUND -> {
                        HunterSearch.Status.SEARCH_FAILED_HUNTER_NOT_FOUND
                    }
                    HuntingControlHunterInfoResponse.ErrorReason.NETWORK_ERROR -> {
                        HunterSearch.Status.SEARCH_FAILED_NETWORK_ERROR
                    }
                }
            }
        }
        val hunter = when (response) {
            is HuntingControlHunterInfoResponse.Success -> response.hunter
            else -> null
        }
        updateViewModel(
            ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    hunter = hunter,
                    hunterSearch = HunterSearch(SearchTerm.SearchableHunterNumber(hunterNumber), status),
                )
            )
        )
    }

    private suspend fun searchBySsn(viewModel: HunterInfoViewModel, searchTerm: SearchTerm.SearchableSsn) {
        val searchingSsn = viewModel.hunterSearch.copy(
            status = HunterSearch.Status.SEARCHING_PERSON_BY_SSN
        )
        updateViewModel(
            ViewModelLoadStatus.Loaded(
                createViewModel(
                    hunter = null,
                    hunterSearch = searchingSsn
                )
            )
        )

        val ssn = searchTerm.ssn
        val response = huntingControlContext.fetchHunterInfoBySsn(ssn)

        val status = when (response) {
            is HuntingControlHunterInfoResponse.Success -> {
                HunterSearch.Status.HUNTER_FOUND
            }
            is HuntingControlHunterInfoResponse.Error -> {
                when (response.reason) {
                    HuntingControlHunterInfoResponse.ErrorReason.NOT_FOUND -> {
                        HunterSearch.Status.SSN_SEARCH_FAILED_HUNTER_NOT_FOUND
                    }
                    HuntingControlHunterInfoResponse.ErrorReason.NETWORK_ERROR -> {
                        HunterSearch.Status.SSN_SEARCH_FAILED_NETWORK_ERROR
                    }
                }
            }
        }
        val hunter = when (response) {
            is HuntingControlHunterInfoResponse.Success -> response.hunter
            else -> null
        }
        updateViewModel(
            ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    hunter = hunter,
                    hunterSearch = HunterSearch(SearchTerm.SearchableSsn(ssn), status),
                )
            )
        )
    }

    private fun createViewModel(
        hunter: HuntingControlHunterInfo?,
        hunterSearch: HunterSearch,
    ): HunterInfoViewModel {
        return HunterInfoViewModel(
            hunterInfo = hunter,
            hunterSearch = hunterSearch,
            fields = produceDataFields(hunter, hunterSearch)
        )
    }

    private fun produceDataFields(
        hunterInfo: HuntingControlHunterInfo?,
        hunterNumberSearch: HunterSearch,
    ): List<DataField<HunterInfoField>> {
        val fieldsToBeDisplayed = getFieldsToBeDisplayed(hunterInfo, hunterNumberSearch)

        return fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
            fieldProducer.createField(fieldSpecification, hunterInfo, hunterNumberSearch)
        }
    }

    private fun getFieldsToBeDisplayed(
        hunterInfo: HuntingControlHunterInfo?,
        hunterSearch: HunterSearch,
    ): List<FieldSpecification<HunterInfoField>> {
        val fields = FieldSpecificationListBuilder<HunterInfoField>()
            .add(
                HunterInfoField.Type.SCAN_QR_CODE.noRequirement(),
                HunterInfoField.Type.ENTER_HUNTER_NUMBER.noRequirement()
                    .takeIf {
                        hunterSearch.status !in listOf(
                            HunterSearch.Status.SEARCHING_PERSON_BY_SSN,
                            HunterSearch.Status.SSN_SEARCH_FAILED_NETWORK_ERROR,
                            HunterSearch.Status.SSN_SEARCH_FAILED_HUNTER_NOT_FOUND
                        )
                    },
                HunterInfoField.Type.ENTERED_SSN.noRequirement()
                    .takeIf {
                        hunterSearch.status in listOf(
                            HunterSearch.Status.SEARCHING_PERSON_BY_SSN,
                            HunterSearch.Status.SSN_SEARCH_FAILED_NETWORK_ERROR,
                            HunterSearch.Status.SSN_SEARCH_FAILED_HUNTER_NOT_FOUND
                        )
                    },
                HunterInfoField.Type.HUNTER_NUMBER_INFO_OR_ERROR.noRequirement()
                    .takeIf { hunterSearch.status != HunterSearch.Status.HUNTER_FOUND },
                HunterInfoField.Type.RETRY_BUTTON.noRequirement()
                    .takeIf {
                        hunterSearch.status in listOf(
                            HunterSearch.Status.SEARCH_FAILED_NETWORK_ERROR,
                            HunterSearch.Status.SSN_SEARCH_FAILED_NETWORK_ERROR
                        )
                    },

                HunterInfoField.Type.PERSONAL_DATA_HEADER.noRequirement().takeIf { hunterInfo != null },
                HunterInfoField.Type.NAME.noRequirement().takeIf { hunterInfo != null },
                HunterInfoField.Type.DATE_OF_BIRTH.noRequirement().takeIf { hunterInfo != null },
                HunterInfoField.Type.HOME_MUNICIPALITY.noRequirement().takeIf { hunterInfo != null },
                HunterInfoField.Type.HUNTER_NUMBER.noRequirement().takeIf { hunterInfo != null },

                HunterInfoField.Type.HUNTING_LICENSE_HEADER.noRequirement().takeIf { hunterInfo != null },
                HunterInfoField.Type.HUNTING_LICENSE_STATUS.noRequirement().takeIf { hunterInfo != null },
                HunterInfoField.Type.HUNTING_LICENSE_DAY_OF_PAYMENT.noRequirement().takeIf { hunterInfo != null },

                HunterInfoField.Type.SHOOTING_TEST_HEADER.noRequirement().takeIf { hunterInfo != null },
            )

        if (hunterInfo != null) {
            fields.add(
                *List(hunterInfo.shootingTests.size) { index ->
                    listOf(
                        HunterInfoField.Type.SPECIES_CAPTION.toField(index).noRequirement(),
                        HunterInfoField.Type.SHOOTING_TEST_INFO.toField(index).noRequirement()
                    )
                }.flatten().toTypedArray()
            )
        }
        fields.add(
            HunterInfoField.Type.RESET_BUTTON.noRequirement()
                .takeIf {
                    hunterInfo != null ||
                            hunterSearch.status == HunterSearch.Status.SSN_SEARCH_FAILED_HUNTER_NOT_FOUND
                }
        )
        return fields.toList()
    }

    @Serializable
    data class State(
        val hunter: HuntingControlHunterInfo?,
        val hunterSearch: HunterSearch,
    )

    /**
     * Call this function before updating viewmodel to loading state!
     */
    private fun updateStateToRestore() {
        // since thin function is called before entering the loading state we can try to
        // obtain the current state. This way we can restore filter dates if we're just
        // refreshing the diary (i.e. viewmodel is in loaded state).
        val currentState = if (viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            getUnreproducibleState()
        } else {
            null
        }

        if (currentState != null) {
            stateToRestore = currentState
        }
    }

    private fun clearStateToRestore() {
        stateToRestore = null
    }

    override fun getUnreproducibleState(): State? {
        val state = getLoadedViewModelOrNull()?.let { viewModel ->
            State(
                hunter = viewModel.hunterInfo,
                hunterSearch = viewModel.hunterSearch,
            )
        }
        return state
    }

    override fun restoreUnreproducibleState(state: State) {
        stateToRestore = state
    }

    companion object {
        private val logger by getLogger(HunterInfoController::class)
    }

}

internal fun HunterInfoField.Type.noRequirement(): FieldSpecification<HunterInfoField> {
    return this.toField().noRequirement()
}
