package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.domain.huntingclub.clubs.storage.MockHuntingClubMemoryStorage
import fi.riista.common.domain.huntingclub.memberships.storage.MockHuntingClubOccupationsMemoryStorage
import fi.riista.common.domain.model.Occupation
import fi.riista.common.domain.model.OccupationType
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.userInfo.MockUsernameProvider
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalizedString
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockBackendAPIProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HuntingClubsSelectableForEntriesTest {

    @Test
    fun `selectable clubs include organizations from user occupations`() = runBlockingTest {
        val occupationsStorage = MockHuntingClubOccupationsMemoryStorage()
        val organisation = organization()
        occupationsStorage.replaceOccupations(
            username = username,
            occupations = listOf(
                Occupation(
                    id = 1,
                    occupationType = OccupationType.CLUB_MEMBER.toBackendEnum(),
                    name = LocalizedString("fi", null, null),
                    beginDate = null,
                    endDate = null,
                    organisation = organisation,
                )
            )
        )

        val selectableClubs = selectableClubs(occupationsStorage = occupationsStorage)
        with (selectableClubs.getClubsSelectableForEntries()) {
            assertEquals(1, size)
            assertEquals(organisation, get(0))
        }
    }

    @Test
    fun `searched club should be included to selectable clubs`() = runBlockingTest {
        val selectableClubs = selectableClubs()
        val result = selectableClubs.searchClubByOfficialCode("1531219")
        assertNotNull(result)
        assertEquals(399, result.id)

        with (selectableClubs.getClubsSelectableForEntries()) {
            assertEquals(1, size)
            assertEquals(result, get(0))
        }
    }

    @Test
    fun `search from backend should not be performed if club exists in club storage`() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val clubStorage = MockHuntingClubMemoryStorage()
        val organization = organization()
        clubStorage.addOrganizationsIfNotExists(listOf(organization))

        val selectableClubs = selectableClubs(backendAPIMock, clubStorage)

        // club should not appear in selectable clubs before search
        assertTrue(selectableClubs.getClubsSelectableForEntries().isEmpty())

        val result = selectableClubs.searchClubByOfficialCode("12345678")
        assertEquals(organization, result)
        assertEquals(0, backendAPIMock.callCount(BackendAPI::searchHuntingClubByOfficialCode))

        // result should also exist in selectable clubs now that search has been performed
        with (selectableClubs.getClubsSelectableForEntries()) {
            assertEquals(1, size)
            assertEquals(organization, get(0))
        }
    }

    @Test
    fun `search from backend should not be performed if club has previously been searched`() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val selectableClubs = selectableClubs(backendAPIMock)

        assertEquals(0, backendAPIMock.callCount(BackendAPI::searchHuntingClubByOfficialCode))
        assertTrue(selectableClubs.getClubsSelectableForEntries().isEmpty())

        val result1 = selectableClubs.searchClubByOfficialCode("1531219")
        assertEquals(1, backendAPIMock.callCount(BackendAPI::searchHuntingClubByOfficialCode))
        with (selectableClubs.getClubsSelectableForEntries()) {
            assertEquals(1, size)
            assertEquals(result1, get(0))
        }

        val result2 = selectableClubs.searchClubByOfficialCode("1531219")
        assertEquals(result1, result2)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::searchHuntingClubByOfficialCode))
        with (selectableClubs.getClubsSelectableForEntries()) {
            assertEquals(1, size)
            assertEquals(result2, get(0))
        }
    }

    private fun organization() =
        Organization(
            id = 1,
            name = LocalizedString(fi = "fi", sv = "sv", en = "en"),
            officialCode = "12345678",
        )

    private fun selectableClubs(
        backendAPIMock: BackendAPIMock = BackendAPIMock(),
        clubStorage: MockHuntingClubMemoryStorage = MockHuntingClubMemoryStorage(),
        occupationsStorage: MockHuntingClubOccupationsMemoryStorage = MockHuntingClubOccupationsMemoryStorage(),
        selectedClubsStorage: UserHuntingClubsProvider = UserHuntingClubsMemoryStorage(),
    ): HuntingClubsSelectableForEntries {
        return HuntingClubsSelectableForEntriesImpl(
            usernameProvider = MockUsernameProvider(username = this@HuntingClubsSelectableForEntriesTest.username),
            clubStorage = clubStorage,
            clubOccupationsProvider = occupationsStorage,
            selectedClubsForEntriesStorage = selectedClubsStorage,
            backendApiProvider = MockBackendAPIProvider(backendAPIMock)
        )
    }

    private val username: String = "Pena"
}
