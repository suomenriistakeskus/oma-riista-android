package fi.riista.common.domain.huntingControl.ui.hunterInfo

sealed class HunterInfoIntent {
    class ChangeHunterNumber(val hunterNumber: Int?): HunterInfoIntent()

    class SearchByHunterNumber(val hunterNumber: String): HunterInfoIntent()
    class SearchBySsn(val ssn: String): HunterInfoIntent()

    object Reset : HunterInfoIntent()
    object Retry : HunterInfoIntent()
}
