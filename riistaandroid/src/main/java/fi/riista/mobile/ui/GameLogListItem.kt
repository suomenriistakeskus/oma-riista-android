package fi.riista.mobile.ui

import android.location.Location
import android.net.Uri
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.util.toLocation
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.models.GameLogImage
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import java.util.*

class GameLogListItem {
    interface OnClickListItemListener {
        fun onItemClick(item: GameLogListItem)
    }

    var isHeader = false
    var isStats = false
    var year = 0
    var month = 0
    var mHarvest: CommonHarvest? = null
    var mObservation: CommonObservation? = null
    var mSrva: CommonSrvaEvent? = null
    var speciesCode: Int? = null
    var totalSpecimenAmount: Int? = null
    var dateTime: Calendar? = null
    var type: String? = null
    var location: Location? = null
    var images: List<GameLogImage>? = null
    var sent = false
    var isTimelineTopVisible = false
    var isTimelineBottomVisible = false

    companion object {
        @JvmStatic
        fun fromHarvest(harvest: CommonHarvest): GameLogListItem {
            val item = GameLogListItem()
            item.speciesCode = harvest.species.knownSpeciesCodeOrNull()
            item.totalSpecimenAmount = harvest.amount
            item.dateTime = harvest.pointOfTime.toJodaDateTime().toCalendar(null)
            item.type = GameLog.TYPE_HARVEST
            item.location = harvest.geoLocation.toLocation()
            item.sent = !harvest.modified
            item.images = harvest.images.primaryImage?.toGameLogImage()?.let {
                listOf(it)
            } ?: emptyList()
            item.mHarvest = harvest
            val dateTime = item.dateTime
            item.month = dateTime?.get(Calendar.MONTH) ?: 0
            item.year = dateTime?.get(Calendar.YEAR) ?: 0
            return item
        }

        @JvmStatic
        fun fromObservation(observation: CommonObservation): GameLogListItem {
            val item = GameLogListItem()
            val amount = if (observation.mooseLikeSpecimenCount() == 0 && observation.totalSpecimenAmount != null) {
                observation.totalSpecimenAmount
            } else {
                observation.mooseLikeSpecimenCount()
            }
            item.speciesCode = observation.species.knownSpeciesCodeOrNull()
            item.totalSpecimenAmount = amount
            item.dateTime = observation.pointOfTime.toJodaDateTime().toCalendar(null)
            item.type = GameLog.TYPE_OBSERVATION
            item.location = observation.location.toLocation()
            item.sent = !observation.modified
            item.images = observation.images.primaryImage?.toGameLogImage()?.let {
                listOf(it)
            } ?: emptyList()
            item.mObservation = observation
            val dateTime = item.dateTime
            item.month = dateTime?.get(Calendar.MONTH) ?: 0
            item.year = dateTime?.get(Calendar.YEAR) ?: 0
            return item
        }

        @JvmStatic
        fun fromSrva(srva: CommonSrvaEvent): GameLogListItem {
            val item = GameLogListItem()
            item.speciesCode = srva.species.knownSpeciesCodeOrNull()
            item.totalSpecimenAmount = srva.totalSpecimenAmount
            item.dateTime = srva.pointOfTime.toJodaDateTime().toCalendar(null)
            item.type = GameLog.TYPE_SRVA
            item.location = srva.location.toLocation()
            item.sent = !srva.modified
            item.images = srva.images.primaryImage?.toGameLogImage()?.let {
                listOf(it)
            } ?: emptyList()
            item.mSrva = srva
            val dateTime = item.dateTime
            item.month = dateTime?.get(Calendar.MONTH) ?: 0
            item.year = dateTime?.get(Calendar.YEAR) ?: 0
            return item
        }
    }
}

fun EntityImage.toGameLogImage(): GameLogImage? {
    return if (localUrl != null && status != EntityImage.Status.LOCAL_TO_BE_REMOVED) {
        val image = GameLogImage(Uri.parse(localUrl))
        image.uuid = serverId
        image
    } else if (serverId != null) {
        GameLogImage(serverId)
    } else {
        null
    }
}

fun CommonObservation.mooseLikeSpecimenCount(): Int {
    var count = 0
    count += mooselikeMaleAmount ?: 0
    count += mooselikeFemaleAmount ?: 0
    count += (mooselikeFemale1CalfAmount ?: 0) * (1 + 1)
    count += (mooselikeFemale2CalfsAmount ?: 0) * (1 + 2)
    count += (mooselikeFemale3CalfsAmount ?: 0) * (1 + 3)
    count += (mooselikeFemale4CalfsAmount ?: 0) * (1 + 4)
    count += mooselikeCalfAmount ?: 0
    count += mooselikeUnknownSpecimenAmount ?: 0
    return count
}
