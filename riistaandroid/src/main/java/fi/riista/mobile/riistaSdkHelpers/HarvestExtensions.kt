package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.common.util.withNumberOfElements
import fi.riista.mobile.database.HarvestDbHelper.UpdateType
import fi.riista.mobile.models.GameHarvest
import fi.riista.mobile.models.GameLogImage
import fi.riista.mobile.models.HarvestSpecimen

fun GameHarvest.toCommonHarvest(): CommonHarvest? {
    val localDateTime = mTime?.let {
        val jodaDateTime = org.joda.time.LocalDateTime.fromCalendarFields(it)
        LocalDateTime(
            year = jodaDateTime.year,
            monthNumber = jodaDateTime.monthOfYear,
            dayOfMonth = jodaDateTime.dayOfMonth,
            hour = jodaDateTime.hourOfDay,
            minute = jodaDateTime.minuteOfHour,
            second = jodaDateTime.secondOfMinute
        )
    } ?: kotlin.run {
        return null
    }

    val species = mSpeciesID?.let { Species.Known(it) } ?: Species.Unknown

    val commonSpecimens =
        (mSpecimen?.map { it.toCommonHarvestSpecimen() } ?: listOf())
            .withNumberOfElements(mAmount) {
                createDefaultHarvestSpecimen()
            }

    return CommonHarvest(
        localId = mLocalId.toLong(),
        localUrl = null,
        id = mId.toLong().takeIf { it != 0L },
        rev = mRev,
        species = species,
        geoLocation = mLocation.toETRMSGeoLocation(source = mLocationSource.toBackendEnum()),
        pointOfTime = localDateTime,
        description = mDescription,
        canEdit = mCanEdit,
        modified = !mSent,
        deleted = mPendingOperation == UpdateType.DELETE,
        images = EntityImages(
            remoteImageIds = mImages.mapNotNull { image ->
                when (image.type) {
                    GameLogImage.ImageType.UUID -> image.uuid
                    GameLogImage.ImageType.URI,
                    null -> null
                }
            },
            localImages = mImages.map { image ->
                EntityImage(
                    serverId = image.uuid,
                    localIdentifier = null,
                    localUrl = image.uri?.toString(),
                    status = when (image.type) {
                        GameLogImage.ImageType.URI, null ->
                            // todo: can image be local and to_be_removed
                            EntityImage.Status.LOCAL
                        GameLogImage.ImageType.UUID -> EntityImage.Status.UPLOADED
                    }
                )
            },
        ),
        specimens = commonSpecimens,
        amount = mAmount,
        harvestSpecVersion = mHarvestSpecVersion,
        harvestReportRequired = mHarvestReportRequired,
        harvestReportState = mHarvestReportState.toBackendEnum(),
        permitNumber = mPermitNumber,
        permitType = mPermitType,
        stateAcceptedToHarvestPermit = mStateAcceptedToHarvestPermit.toBackendEnum(),
        deerHuntingType = mDeerHuntingType?.name.toBackendEnum(),
        deerHuntingOtherTypeDescription = mDeerHuntingOtherTypeDescription,
        mobileClientRefId = mMobileClientRefId.takeIf { it != 0L },
        harvestReportDone = mHarvestReportDone,
        rejected = mHarvestReportState == GameHarvest.HARVEST_REJECTED,
        feedingPlace = mFeedingPlace,
        taigaBeanGoose = mTaigaBeanGoose,
        greySealHuntingMethod = mHuntingMethod?.name.toBackendEnum()
    )
}

fun HarvestSpecimen.toCommonHarvestSpecimen() =
    CommonHarvestSpecimen(
        id = id?.toLong(),
        rev = rev?.toInt(),
        gender = gender.toBackendEnum(),
        age = age.toBackendEnum(),
        weight = weight,
        weightEstimated = weightEstimated,
        weightMeasured = weightMeasured,
        fitnessClass = fitnessClass.toBackendEnum(),
        antlersLost = antlersLost,
        antlersType = antlersType.toBackendEnum(),
        antlersWidth = antlersWidth,
        antlerPointsLeft = antlerPointsLeft,
        antlerPointsRight = antlerPointsRight,
        antlersGirth = antlersGirth,
        antlersLength = antlersLength,
        antlersInnerWidth = antlersInnerWidth,
        antlerShaftWidth = antlerShaftWidth,
        notEdible = notEdible,
        alone = alone,
        additionalInfo = additionalInfo,
    )

private fun createDefaultHarvestSpecimen() =
    CommonHarvestSpecimen(
        id = null,
        rev = null,
        gender = BackendEnum.create(null),
        age = BackendEnum.create(null),
        weight = null,
        weightEstimated = null,
        weightMeasured = null,
        fitnessClass = BackendEnum.create(null),
        antlersLost = null,
        antlersType = BackendEnum.create(null),
        antlersWidth = null,
        antlerPointsLeft = null,
        antlerPointsRight = null,
        antlersGirth = null,
        antlersLength = null,
        antlersInnerWidth = null,
        antlerShaftWidth = null,
        notEdible = null,
        alone = null,
        additionalInfo = null,
    )
