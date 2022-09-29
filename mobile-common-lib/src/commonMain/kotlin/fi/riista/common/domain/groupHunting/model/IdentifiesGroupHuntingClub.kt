package fi.riista.common.domain.groupHunting.model

import fi.riista.common.domain.model.OrganizationId

/**
 * An interface for classes that are able to identify group hunting club.
 */
interface IdentifiesGroupHuntingClub {
    val clubId: OrganizationId
}