package fi.riista.common.groupHunting.model

/**
 * An interface for classes that are able to identify a harvest made within group hunting.
 *
 * In order to identify a [GroupHuntingHarvest], the [HuntingGroup] also needs to be identified.
 */
interface IdentifiesGroupHuntingHarvest: IdentifiesHuntingGroup {
    val harvestId: GroupHuntingHarvestId
}
