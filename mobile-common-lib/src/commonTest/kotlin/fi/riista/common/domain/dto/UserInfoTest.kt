package fi.riista.common.domain.dto

import fi.riista.common.domain.model.OccupationType
import kotlin.test.*

@Suppress("SpellCheckingInspection")
class UserInfoTest {

    @Test
    fun testBasicUserDetails() {
        val userInfo = parseUserInfo(MockUserInfo.Pentti)
        assertEquals("user", userInfo.username)
        assertEquals("Pentti", userInfo.firstName)
        assertEquals("Mujunen", userInfo.lastName)
        // TODO: use proper LocalDate (or similar) when available
        assertEquals("1911-11-11", userInfo.birthDate)

        // sv, en tested elsewhere for other LocalizedStrings
        assertEquals("Nokia", userInfo.homeMunicipality.fi)
    }

    @Test
    fun testOtherUserDetails() {
        val userInfo = parseUserInfo(MockUserInfo.Pentti)
        assertNotNull(userInfo.qrCode)
        assertNotNull(userInfo.timestamp)
        assertTrue(userInfo.enableSrva)
        assertTrue(userInfo.enableShootingTests)
        assertTrue(userInfo.deerPilotUser)
    }

    @Test
    fun testAddress() {
        val address = parseUserInfo(MockUserInfo.Pentti).address!!
        assertEquals(134, address.id)
        assertEquals(0, address.rev)
        assertFalse(address.editable)
        assertEquals("Mujunenkatu", address.streetAddress)
        assertEquals("00004", address.postalCode)
        assertEquals("Mujunenkaupunki", address.city)
        assertEquals("suomi", address.country)
    }

    @Test
    fun testRhy() {
        val rhy: OrganizationDTO = parseUserInfo(MockUserInfo.Pentti).rhy!!
        assertEquals(180, rhy.id)
        assertEquals("Nokian seudun riistanhoitoyhdistys", rhy.name.fi)
        assertEquals("Nokianejdens jaktvårdsförening", rhy.name.sv)
        //assertNull(rhy.name.en)
        assertEquals("368", rhy.officialCode)
    }

    @Test
    fun testHunterInformation() {
        val userInfo = parseUserInfo(MockUserInfo.Pentti)
        assertEquals("88888888", userInfo.hunterNumber)
        assertEquals("1990-01-01", userInfo.hunterExamDate)
        assertEquals("2019-08-01", userInfo.huntingCardStart)
        assertEquals("2021-07-31", userInfo.huntingCardEnd)
        assertEquals("2021-06-30", userInfo.huntingBanStart)
        assertEquals("2021-06-31", userInfo.huntingBanEnd)
        assertTrue(userInfo.huntingCardValidNow)
    }

    @Test
    fun testShootingTests() {
        // TODO: 11.2.2021 get data for shooting tests!
        val userInfo = parseUserInfo(MockUserInfo.Pentti)
        assertEquals(0, userInfo.shootingTests.size)
    }

    @Test
    fun testOccupations() {
        val occupations = parseUserInfo(MockUserInfo.Pentti).occupations
        assertEquals(3, occupations.size)
        val occupation = occupations[0]
        assertEquals(69, occupation.id)
        assertEquals(OccupationType.SHOOTING_TEST_OFFICIAL.rawBackendEnumValue, occupation.occupationType)
        assertEquals("Ampumakokeen vastaanottaja", occupation.name.fi)
        assertNull(occupation.beginDate)
        assertNull(occupation.endDate)
        assertNotNull(occupation.organisation)

        assertEquals("2020-01-01", occupations[1].beginDate)
        assertEquals("2020-02-01", occupations[1].endDate)
    }

    @Test
    fun testMissingDataParsing() {
        parseUserInfo(MockUserInfo.PenttiWithMissingData)
    }

    private fun parseUserInfo(userInfo: String, ignoreUnknownKeys: Boolean = true): UserInfoDTO =
        MockUserInfo.parse(userInfo, ignoreUnknownKeys)
}
