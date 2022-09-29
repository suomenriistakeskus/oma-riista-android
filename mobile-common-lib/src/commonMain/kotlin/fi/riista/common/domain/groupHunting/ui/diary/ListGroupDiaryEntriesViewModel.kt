package fi.riista.common.domain.groupHunting.ui.diary

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.DiaryEntryType
import fi.riista.common.model.BackendId
import fi.riista.common.model.LocalDateTime


data class ListGroupDiaryEntriesViewModel(
    val entries: List<GroupDiaryEntryViewModel>
)

data class GroupDiaryEntryViewModel(
    val type: DiaryEntryType,
    val remoteId: BackendId,
    val speciesCode: SpeciesCode,
    val acceptStatus: AcceptStatus,
    val pointOfTime: LocalDateTime,

    /**
     * The name of the shooter / observer. Optional.
     */
    val actorName: String?
)



