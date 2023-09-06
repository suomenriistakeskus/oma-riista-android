package fi.riista.common.model

import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals

class ETRSCoordinateTest {
    @Test
    fun `distance between two coordinates`() {
        assertEquals(5.0, ETRSCoordinate(0,0).distanceTo(ETRSCoordinate(3, 4)))

        // example from https://www.einouikkanen.fi/geodocs/OppijaksoKoordinaatistoista.html
        // Vaasa ETRS-TM35FIN coordinates: 7015316, 231624
        // Kotka ETRS-TM35FIN coordinates: 6707100, 495422
        assertEquals(
            expected = 405693.0,
            actual = round(ETRSCoordinate(7015316, 231624).distanceTo(ETRSCoordinate(6707100, 495422)))
        )
    }
}
