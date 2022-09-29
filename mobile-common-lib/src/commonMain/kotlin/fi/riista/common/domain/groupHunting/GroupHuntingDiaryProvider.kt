package fi.riista.common.domain.groupHunting

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.domain.groupHunting.dto.GroupHuntingDiaryDTO
import fi.riista.common.domain.groupHunting.dto.toGroupHuntingDiary
import fi.riista.common.domain.groupHunting.model.GroupHuntingDiary
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface GroupHuntingDiaryProvider: DataFetcher {
    val diary: GroupHuntingDiary
}

internal class GroupHuntingDiaryNetworkProvider(
    backendApiProvider: BackendApiProvider,
    private val huntingGroupId: HuntingGroupId,
) : GroupHuntingDiaryProvider,
    NetworkDataFetcher<GroupHuntingDiaryDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _gameDiary = AtomicReference(GroupHuntingDiary(emptyList(), emptyList(), emptyList(), emptyList()))
    override val diary: GroupHuntingDiary
        get() {
            return _gameDiary.value
        }

    override suspend fun fetchFromNetwork(): NetworkResponse<GroupHuntingDiaryDTO> =
            backendAPI.fetchGroupHuntingDiary(huntingGroupId)

    override fun handleSuccess(
            statusCode: Int,
            responseData: NetworkResponseData<out GroupHuntingDiaryDTO>
    ) {
        _gameDiary.set(responseData.typed.toGroupHuntingDiary())
    }

    override fun handleError401() {
        _gameDiary.set(GroupHuntingDiary(emptyList(), emptyList(), emptyList(), emptyList()))
    }

    override fun logger(): Logger = logger

    companion object {
        private val logger by getLogger(GroupHuntingDiaryNetworkProvider::class)
    }

}
