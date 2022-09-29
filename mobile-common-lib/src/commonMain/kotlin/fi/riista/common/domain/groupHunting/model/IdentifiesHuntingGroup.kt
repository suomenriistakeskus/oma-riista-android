package fi.riista.common.domain.groupHunting.model

/**
 * An interface for classes that are able to identify group hunting hunting group.
 *
 * In order to identify a [HuntingGroup], the club also needs to be identified
 */
interface IdentifiesHuntingGroup: IdentifiesGroupHuntingClub {
    val huntingGroupId: HuntingGroupId
}
