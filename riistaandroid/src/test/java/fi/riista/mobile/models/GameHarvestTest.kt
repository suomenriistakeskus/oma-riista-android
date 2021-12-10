package fi.riista.mobile.models

import android.location.Location
import android.util.Pair
import fi.riista.mobile.AppConfig
import fi.riista.mobile.test.MockUtils.mockLocation
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.abstraction.MockUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations
import java.util.EnumSet

class GameHarvestTest {

    private lateinit var mock: MockNeat
    private lateinit var specimenGenerator: MockUnit<HarvestSpecimen>

    private lateinit var locationMock: Location
    private lateinit var locationMock2: Location

    @Mock
    private var coordinateMock: Pair<Long, Long>? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        mock = MockNeat.threadLocal()

        // Harvest specimen fields are auto-filled using reflection.
        specimenGenerator = mock.reflect(HarvestSpecimen::class.java).useDefaults(true)

        locationMock = mockLocation(62.0, 24.0)
        locationMock2 = mockLocation(62.0, 24.0)
    }

    @Test
    fun testDeepClone() {
        val original = newHarvest()
        val copy = original.deepClone()

        // Copy must not be same instance.
        assertNotSame(original, copy)

        assertEquals(original.mHarvestSpecVersion, copy.mHarvestSpecVersion)

        assertEquals(original.mId, copy.mId)
        assertEquals(original.mLocalId, copy.mLocalId)
        assertEquals(original.mMobileClientRefId, copy.mMobileClientRefId)

        assertEquals(original.mSent, copy.mSent)
        assertEquals(original.mRemote, copy.mRemote)
        assertEquals(original.mRev, copy.mRev)
        assertEquals(original.mPendingOperation, copy.mPendingOperation)
        assertEquals(original.mCanEdit, copy.mCanEdit)

        assertEquals(original.mSpeciesID, copy.mSpeciesID)

        // Location is a mutable class => object identities must differ
        assertNotSame(original.mLocation, copy.mLocation)
        assertEquals(original.mLocation.latitude, copy.mLocation.latitude, 0.00001)
        assertEquals(original.mLocation.longitude, copy.mLocation.longitude, 0.00001)
        assertEquals(original.mLocation.altitude, copy.mLocation.altitude, 0.00001)

        assertEquals(original.mCoordinates, copy.mCoordinates)
        assertEquals(original.mAccuracy, copy.mAccuracy, 0.00001f)
        assertEquals(original.mHasAltitude, copy.mHasAltitude)
        assertEquals(original.mAltitude, copy.mAltitude, 0.00001)
        assertEquals(original.mAltitudeAccuracy, copy.mAltitudeAccuracy, 0.00001)
        assertEquals(original.mLocationSource, copy.mLocationSource)

        assertEquals(original.mTime, copy.mTime)
        assertEquals(original.mAmount, copy.mAmount)
        assertEquals(original.mDescription, copy.mDescription)

        assertEquals(original.mHarvestReportDone, copy.mHarvestReportDone)
        assertEquals(original.mHarvestReportRequired, copy.mHarvestReportRequired)
        assertEquals(original.mHarvestReportState, copy.mHarvestReportState)

        assertEquals(original.mPermitNumber, copy.mPermitNumber)
        assertEquals(original.mPermitType, copy.mPermitType)
        assertEquals(original.mStateAcceptedToHarvestPermit, copy.mStateAcceptedToHarvestPermit)

        assertEquals(original.mDeerHuntingType, copy.mDeerHuntingType)
        assertEquals(original.mDeerHuntingOtherTypeDescription, copy.mDeerHuntingOtherTypeDescription)
        assertEquals(original.mFeedingPlace, copy.mFeedingPlace)
        assertEquals(original.mHuntingMethod, copy.mHuntingMethod)
        assertEquals(original.mTaigaBeanGoose, copy.mTaigaBeanGoose)

        val numExpectedSpecimens = original.mSpecimen.size
        assertEquals(numExpectedSpecimens, copy.mSpecimen.size)

        for (i in 0 until numExpectedSpecimens) {
            val originalSpecimen: HarvestSpecimen = original.mSpecimen[i]
            val specimenClone: HarvestSpecimen = copy.mSpecimen[i]
            HarvestSpecimenTest.assertEqualButNotSame(originalSpecimen, specimenClone)
        }

        assertEquals(original.mImages, copy.mImages)
    }

    // Does not return sensible field values but it is not essential in these tests.
    private fun newHarvest(): GameHarvest {
        val harvest = spy(GameHarvest.createNew(AppConfig.HARVEST_SPEC_VERSION))
        harvest.mLocation = locationMock
        `when`(harvest.copyOfLocation).thenReturn(locationMock2)

        // Populate values not set in constructor.

        harvest.mId = newInt()
        harvest.mLocalId = newInt()

        harvest.mSent = newBoolean()
        harvest.mRemote = newBoolean()
        harvest.mCanEdit = newBoolean()

        harvest.mSpeciesID = newInt()

        harvest.mCoordinates = coordinateMock
        harvest.mAccuracy = newFloat()
        harvest.mHasAltitude = newBoolean()
        harvest.mAltitude = newDouble()
        harvest.mAltitudeAccuracy = newDouble()
        harvest.mLocationSource = newString()

        harvest.mAmount = mock.ints().range(1, 25).`val`()
        harvest.mDescription = newString()

        harvest.mHarvestReportDone = newBoolean()
        harvest.mHarvestReportRequired = newBoolean()
        harvest.mHarvestReportState = newString()

        harvest.mPermitNumber = newString()
        harvest.mPermitType = newString()
        harvest.mStateAcceptedToHarvestPermit = newString()

        harvest.mDeerHuntingType = newEnum(DeerHuntingType::class.java)
        harvest.mDeerHuntingOtherTypeDescription = newString()
        harvest.mFeedingPlace = newBoolean()
        harvest.mHuntingMethod = newEnum(GreySealHuntingMethod::class.java)
        harvest.mTaigaBeanGoose = newBoolean()

        harvest.mSpecimen = listOf(newSpecimen(), newSpecimen())

        return harvest
    }

    private fun newInt(): Int = mock.ints().`val`()

    private fun newFloat(): Float = mock.floats().`val`()

    private fun newDouble(): Double = mock.doubles().`val`()

    private fun newBoolean(): Boolean = mock.bools().`val`()

    private fun newString(): String = mock.strings().`val`()

    private fun <T : Enum<T>> newEnum(clazz: Class<T>): T = mock.seq(EnumSet.allOf(clazz)).`val`()

    private fun newSpecimen(): HarvestSpecimen = specimenGenerator.`val`()

}
