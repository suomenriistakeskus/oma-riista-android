package fi.riista.mobile.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_EN
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_SV
import fi.riista.mobile.utils.DateTimeUtils.formatLocalDateUsingShortFinnishFormat
import org.joda.time.LocalDate

@Entity(tableName = "mh_permit")
data class MetsahallitusPermit(

        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "permit_identifier")
        val permitIdentifier: String,

        @ColumnInfo(name = "permit_type")
        val permitType: String?,

        @ColumnInfo(name = "permit_type_sv")
        val permitTypeSwedish: String?,

        @ColumnInfo(name = "permit_type_en")
        val permitTypeEnglish: String?,

        @ColumnInfo(name = "permit_name")
        val permitName: String?,

        @ColumnInfo(name = "permit_name_sv")
        val permitNameSwedish: String?,

        @ColumnInfo(name = "permit_name_en")
        val permitNameEnglish: String?,

        @ColumnInfo(name = "area_number")
        val areaNumber: String,

        @ColumnInfo(name = "area_name")
        val areaName: String?,

        @ColumnInfo(name = "area_name_sv")
        val areaNameSwedish: String?,

        @ColumnInfo(name = "area_name_en")
        val areaNameEnglish: String?,

        @ColumnInfo(name = "begin_date")
        val beginDate: LocalDate?,

        @ColumnInfo(name = "end_date")
        val endDate: LocalDate?,

        @ColumnInfo(name = "harvest_feedback_url")
        val harvestFeedbackUrl: String?,

        @ColumnInfo(name = "harvest_feedback_url_sv")
        val harvestFeedbackUrlSwedish: String?,

        @ColumnInfo(name = "harvest_feedback_url_en")
        val harvestFeedbackUrlEnglish: String?,

        @NonNull
        @ColumnInfo(name = "user_name")
        val username: String

) {
    val period: String?
        get() {
            if (beginDate == null) {
                return endDate?.let {
                    val endDateAsString = formatLocalDateUsingShortFinnishFormat(it) ?: ""
                    "- $endDateAsString"
                }
            }

            val beginDateAsString = formatLocalDateUsingShortFinnishFormat(beginDate) ?: ""

            return when (endDate) {
                null -> beginDateAsString
                beginDate -> beginDateAsString
                else -> {
                    val endDateAsString = formatLocalDateUsingShortFinnishFormat(endDate) ?: ""
                    "$beginDateAsString - $endDateAsString"
                }
            }
        }

    fun getPermitName(languageCode: String?): String? =
            when (languageCode) {
                LANGUAGE_CODE_SV -> permitNameSwedish ?: permitName
                LANGUAGE_CODE_EN -> permitNameEnglish ?: permitName
                else -> permitName
            }

    fun getPermitType(languageCode: String?): String? =
            when (languageCode) {
                LANGUAGE_CODE_SV -> permitTypeSwedish ?: permitType
                LANGUAGE_CODE_EN -> permitTypeEnglish ?: permitType
                else -> permitType
            }

    fun getAreaName(languageCode: String?): String? =
            when (languageCode) {
                LANGUAGE_CODE_SV -> areaNameSwedish ?: areaName
                LANGUAGE_CODE_EN -> areaNameEnglish ?: areaName
                else -> areaName
            }

    fun getAreaNumberAndName(languageCode: String?): String =
            getAreaName(languageCode)?.let { "$areaNumber $it" } ?: areaNumber

    fun getHarvestFeedbackUrl(languageCode: String?): String? =
            when (languageCode) {
                LANGUAGE_CODE_SV -> harvestFeedbackUrlSwedish ?: harvestFeedbackUrl
                LANGUAGE_CODE_EN -> harvestFeedbackUrlEnglish ?: harvestFeedbackUrl
                else -> harvestFeedbackUrl
            }
}
