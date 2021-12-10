package fi.riista.common.groupHunting

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.groupHunting.dto.GroupHuntingDayDTO
import fi.riista.common.groupHunting.dto.GroupHuntingDaysDTO
import fi.riista.common.groupHunting.dto.toHuntingDay
import fi.riista.common.groupHunting.model.GroupHuntingDay
import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface GroupHuntingDayProvider: DataFetcher {
    val huntingDays: List<GroupHuntingDay>?
}

internal class GroupHuntingDayFromNetworkProvider(
    backendApiProvider: BackendApiProvider,
    private val huntingGroupId: HuntingGroupId,
) : GroupHuntingDayProvider,
    NetworkDataFetcher<GroupHuntingDaysDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _huntingDays = AtomicReference<List<GroupHuntingDay>?>(null)
    override val huntingDays: List<GroupHuntingDay>?
        get() = _huntingDays.value

    override suspend fun fetchFromNetwork(): NetworkResponse<GroupHuntingDaysDTO> =
        backendAPI.fetchHuntingGroupHuntingDays(huntingGroupId)

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out List<GroupHuntingDayDTO>>
    ) {
        val createdHuntingDays = responseData.typed.mapNotNull { it.toHuntingDay() }
        if (createdHuntingDays.size != responseData.typed.size) {
            logger.w { "Failed to map each GroupHuntingDayDTO to a GroupHuntingDay" }
        }
        _huntingDays.set(createdHuntingDays)
    }

    override fun handleError401() {
        _huntingDays.set(null)
    }

    override fun logger(): Logger = logger

    companion object {
        private val logger by getLogger(GroupHuntingDayFromNetworkProvider::class)
    }
}