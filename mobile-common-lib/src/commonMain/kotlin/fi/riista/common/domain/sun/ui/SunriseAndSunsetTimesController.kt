package fi.riista.common.domain.sun.ui

import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.sun.SunriseAndSunset
import fi.riista.common.domain.sun.SunriseAndSunsetCalculator
import fi.riista.common.logging.getLogger
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.ETRSCoordinate
import fi.riista.common.model.distanceTo
import fi.riista.common.model.isInsideFinland
import fi.riista.common.model.toHoursAndMinutesString
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlin.math.round

class SunriseAndSunsetTimesController internal constructor(
    private val currentTimeProvider: LocalDateTimeProvider,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<SunriseAndSunsetTimesViewModel>(),
    IntentHandler<ChangeCalculationParamsIntent>,
    HasUnreproducibleState<SunriseAndSunsetTimesController.SavedState> {

    constructor(stringProvider: StringProvider): this(
        currentTimeProvider = SystemDateTimeProvider(),
        stringProvider = stringProvider,
    )

    val eventDispatchers: ChangeCalculationParamsEventDispatcher by lazy {
        ChangeCalculationParamsEventToIntentMapper(intentHandler = this)
    }

    /**
     * Can the location be moved automatically?
     *
     * Automatic location updates should be prevented if user has manually specified
     * the location for the sunrise/sunset calculation.
     */
    var locationCanBeUpdatedAutomatically: Boolean = true
        private set

    private val pendingIntents = mutableListOf<ChangeCalculationParamsIntent>()

    private var restoredCalculationParams: SunriseAndSunsetCalculationParams? = null

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<SunriseAndSunsetTimesViewModel>> = flow {

        // prefer existing viewmodel if any
        getLoadedViewModelOrNull()?.let {
            emit(ViewModelLoadStatus.Loaded(it))
            return@flow
        }


        val viewModel = createViewModel(
            parameters = restoredCalculationParams ?: SunriseAndSunsetCalculationParams(
                localDate = currentTimeProvider.now().date,
                location = CommonLocation.Unknown,
                lastResult = null,
            )
        )

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    override fun handleIntent(intent: ChangeCalculationParamsIntent) {
        // It is possible that intent is sent already before we have Loaded viewmodel.
        // This is the case e.g. when location is updated in external activity (on android)
        // and the activity/fragment utilizing this controller was destroyed. In that case
        // the call cycle could be:
        // - finish map activity with result
        // - create fragment / activity (that will utilize this controller)
        // - restore controller state
        // - handle activity result (e.g. dispatch location updated event)
        // - resume -> loadViewModel
        //
        // tackle the above situation by collecting intents to pendingIntents and restored
        // viewmodel with those when viewModel has been loaded
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            updateViewModel(ViewModelLoadStatus.Loaded(
                viewModel = handleIntent(intent, viewModel)
            ))
        } else {
            pendingIntents.add(intent)
        }
    }

    private fun handleIntent(
        intent: ChangeCalculationParamsIntent,
        viewModel: SunriseAndSunsetTimesViewModel,
    ): SunriseAndSunsetTimesViewModel {
        val newParameters = when (intent) {
            is ChangeCalculationParamsIntent.ChangeDate ->
                viewModel.parameters.copy(
                    localDate = intent.localDate
                )
            is ChangeCalculationParamsIntent.ChangeLocation -> {
                if (intent.locationChangedAfterUserInteraction) {
                    locationCanBeUpdatedAutomatically = false
                }

                viewModel.parameters.copy(
                    location = intent.location.asKnownLocation()
                )
            }
        }

        return createViewModel(parameters = newParameters)
    }

    private fun createViewModel(parameters: SunriseAndSunsetCalculationParams): SunriseAndSunsetTimesViewModel {
        val sunriseAndSunset = calculateSunriseAndSunset(parameters)

        return parameters.copy(lastResult = sunriseAndSunset).let { params ->
            SunriseAndSunsetTimesViewModel(
                parameters = params,
                fields = createFields(params)
            )
        }
    }

    private fun createFields(parameters: SunriseAndSunsetCalculationParams): List<DataField<SunriseAndSunsetField>> {
        val sunriseAndSunset = parameters.lastResult

        return listOfNotNull(
            LocationField(
                id = SunriseAndSunsetField.LOCATION,
                location = parameters.location,
            ) {
                readOnly = false
            },
            DateField(
                id = SunriseAndSunsetField.DATE,
                date = parameters.localDate,
            ) {
                label = stringProvider.getString(RR.string.sun_day_selection_label)
                readOnly = false
            },
            StringField(
                id = SunriseAndSunsetField.TEXT_SUNRISE,
                value = sunriseAndSunset?.sunrise?.toHoursAndMinutesString() ?: "-"
            ) {
                label = stringProvider.getString(RR.string.sun_sunrise_label)
                singleLine = true
                readOnly = true
                paddingTop = Padding.LARGE
                paddingBottom = Padding.SMALL
            }.takeIf { sunriseAndSunset != null },
            StringField(
                id = SunriseAndSunsetField.TEXT_SUNSET,
                value = sunriseAndSunset?.sunset?.toHoursAndMinutesString() ?: "-"
            ) {
                label = stringProvider.getString(RR.string.sun_sunset_label)
                singleLine = true
                readOnly = true
            }.takeIf { sunriseAndSunset != null },
            LabelField(
                id = SunriseAndSunsetField.INSTRUCTIONS,
                text = stringProvider.getString(RR.string.sun_instructions),
                type = LabelField.Type.INFO
            ) {
                textAlignment = LabelField.TextAlignment.CENTER
                paddingTop = Padding.LARGE
            }.takeIf { sunriseAndSunset == null },
            LabelField(
                id = SunriseAndSunsetField.DISCLAIMER,
                text = stringProvider.getString(RR.string.sun_disclaimer),
                type = LabelField.Type.INFO
            ) {
                textAlignment = LabelField.TextAlignment.JUSTIFIED
                paddingTop = Padding.LARGE
            }.takeIf { sunriseAndSunset != null }
        )
    }

    private fun calculateSunriseAndSunset(parameters: SunriseAndSunsetCalculationParams): SunriseAndSunset? {
        val knownLocation = parameters.location.etrsLocationOrNull ?: kotlin.run {
            return null
        }
        val etrsCoordinate = ETRSCoordinate(knownLocation.latitude.toLong(), knownLocation.longitude.toLong())

        val preventRecalculation = parameters.lastResult?.location?.let { lastCalculationLocation ->
            val distanceFromLastCalculation = etrsCoordinate.distanceTo(lastCalculationLocation)

            // always recalculate if user has changed the location!
            if (locationCanBeUpdatedAutomatically && distanceFromLastCalculation < 250) {
                logger.v {
                    "Location changed ${round(distanceFromLastCalculation)} meters, " +
                            "not calculating sunrise/sunset again"
                }
                true
            } else {
                false
            }
        } ?: false

        if (preventRecalculation) {
            return parameters.lastResult
        }

        return SunriseAndSunsetCalculator.calculateSunriseAndSunset(
            date = parameters.localDate,
            location = etrsCoordinate
        )
    }

    /**
     * Tries to use the current user location as place for calculating sunrise and sunset.
     *
     * Controller will only update the calculation parameters if user has not explicitly
     * set the location previously and if the given [location] is inside Finland.
     *
     * The return value indicates whether it might be possible to update location parameter to
     * current user location in the future.
     *
     * @return  True if location can be changed in the future, false otherwise.
     */
    fun trySelectCurrentUserLocation(location: ETRMSGeoLocation): Boolean {
        if (!location.isInsideFinland()) {
            // It is possible that the exact GPS location is not yet known and the attempted
            // location is outside of Finland. Don't prevent future updates because of this.
            return true
        }

        if (!locationCanBeUpdatedAutomatically) {
            return false
        }

        // don't allow automatic location updates unless the viewmodel is loaded
        // - otherwise we might get pending location updates before loading of viewmodel
        if (getLoadedViewModelOrNull() == null) {
            return true
        }

        handleIntent(
            ChangeCalculationParamsIntent.ChangeLocation(
                location = location,
                locationChangedAfterUserInteraction = false
            )
        )
        return true
    }

    override fun getUnreproducibleState(): SavedState? {
        return getLoadedViewModelOrNull()?.parameters?.let { parameters ->
            SavedState(
                calculationParams = parameters,
                locationCanBeUpdatedAutomatically = locationCanBeUpdatedAutomatically,
            )
        }
    }

    override fun restoreUnreproducibleState(state: SavedState) {
        restoredCalculationParams = state.calculationParams
        locationCanBeUpdatedAutomatically = state.locationCanBeUpdatedAutomatically
    }

    @Serializable
    data class SavedState internal constructor(
        internal val calculationParams: SunriseAndSunsetCalculationParams,
        internal val locationCanBeUpdatedAutomatically: Boolean,
    )

    companion object {
        private val logger by getLogger(SunriseAndSunsetTimesController::class)
    }
}
