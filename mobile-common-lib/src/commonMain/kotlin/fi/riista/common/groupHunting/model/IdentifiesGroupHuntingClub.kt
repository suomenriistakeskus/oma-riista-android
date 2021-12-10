package fi.riista.common.groupHunting.model

import fi.riista.common.model.OrganizationId

/**
 * An interface for classes that are able to identify group hunting club.
 */
interface IdentifiesGroupHuntingClub {
    val clubId: OrganizationId
}