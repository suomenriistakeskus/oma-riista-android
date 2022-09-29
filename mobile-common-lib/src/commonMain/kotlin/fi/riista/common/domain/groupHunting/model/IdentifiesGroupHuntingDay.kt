package fi.riista.common.domain.groupHunting.model

/**
 * An interface for classes that are able to identify a hunting day made within group hunting.
 *
 * In order to identify a [GroupHuntingDay], the [HuntingGroup] also needs to be identified.
 */
interface IdentifiesGroupHuntingDay: IdentifiesHuntingGroup {
    val huntingDayId: GroupHuntingDayId
}
