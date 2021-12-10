package fi.riista.mobile.test

import android.location.Location
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

object MockUtils {

    @JvmStatic
    fun mockLocation(lat: Double, lng: Double): Location {
        val location = mock(Location::class.java)
        `when`(location.latitude).thenReturn(lat)
        `when`(location.longitude).thenReturn(lng)
        return location
    }
}
