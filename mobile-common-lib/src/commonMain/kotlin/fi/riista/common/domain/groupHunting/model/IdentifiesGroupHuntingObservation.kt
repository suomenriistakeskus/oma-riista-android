package fi.riista.common.domain.groupHunting.model


/**
 * An interface for classes that are able to identify an observation made within group hunting.
 *
 * In order to identify a [GroupHuntingObservation], the [HuntingGroup] also needs to be identified.
 */
interface IdentifiesGroupHuntingObservation: IdentifiesHuntingGroup {
    val observationId: GroupHuntingObservationId
}
