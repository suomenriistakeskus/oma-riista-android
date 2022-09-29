@file:Suppress("SpellCheckingInspection")

package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.model.LocalDate
import fi.riista.common.domain.model.OccupationType
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.model.toBackendEnum

object MockGroupHuntingData {
    const val FirstHuntingGroupId: HuntingGroupId = 344
    const val SecondHuntingGroupId: HuntingGroupId = 345


    const val FirstClubId: OrganizationId = 329
    const val OneClub: String =
        "{\n" +
        "    \"clubs\": [\n" +
        "        {\n" +
        "            \"id\": $FirstClubId,\n" +
        "            \"officialCode\": \"1\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"Nokian metsästysseura\",\n" +
        "                \"sv\": \"Nokian metsästysseura SV\"\n" +
        "            }\n" +
        "        }\n" +
        "    ],\n" +
        "    \"groups\": [\n" +
        "        {\n" +
        "            \"id\": $FirstHuntingGroupId,\n" +
        "            \"clubId\": $FirstClubId,\n" +
        "            \"speciesCode\": 47503,\n" +
        "            \"huntingYear\": 2019,\n" +
        "            \"beginDate\": \"2015-06-02\",\n" +
        "            \"endDate\": \"2015-10-31\",\n" +
        "            \"beginDate2\": null,\n" +
        "            \"endDate2\": null,\n"+
        "            \"permitNumber\": \"2019-1-000-10000-6\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"Hirvi 2019 fi\",\n" +
        "                \"sv\": \"Hirvi 2019 sv\"\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": $SecondHuntingGroupId,\n" +
        "            \"clubId\": $FirstClubId,\n" +
        "            \"speciesCode\": 47629,\n" +
        "            \"huntingYear\": 2019,\n" +
        "            \"beginDate\": \"2015-06-01\",\n" +
        "            \"endDate\": \"2015-10-30\",\n" +
        "            \"beginDate2\": null,\n" +
        "            \"endDate2\": null,\n"+
        "            \"permitNumber\": \"2019-1-000-10000-7\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"VHP 2019\",\n" +
        "                \"sv\": \"VHP 2019\"\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": 346,\n" +
        "            \"clubId\": $FirstClubId,\n" +
        "            \"speciesCode\": 47503,\n" +
        "            \"huntingYear\": 2021,\n" +
        "            \"beginDate\": \"2021-06-01\",\n" +
        "            \"endDate\": \"2021-10-30\",\n" +
        "            \"beginDate2\": null,\n" +
        "            \"endDate2\": null,\n"+
        "            \"permitNumber\": \"2021-1-000-10002-5\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"Hirviryhmä 21\",\n" +
        "                \"sv\": \"Hirviryhmä 21\"\n" +
        "            }\n" +
        "        }\n" +
        "    ]\n" +
        "}"

    const val SecondClubId: OrganizationId = 339
    const val TwoClubs: String =
        "{\n" +
        "    \"clubs\": [\n" +
        "        {\n" +
        "            \"id\": $FirstClubId,\n" +
        "            \"officialCode\": \"1\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"Nokian metsästysseura\",\n" +
        "                \"sv\": \"Nokian metsästysseura SV\"\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": $SecondClubId,\n" +
        "            \"officialCode\": \"2\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"Pirkkalan porukka\",\n" +
        "                \"sv\": \"Pirkkalan porukka SV\"\n" +
        "            }\n" +
        "        }\n" +
        "    ],\n" +
        "    \"groups\": [\n" +
        "        {\n" +
        "            \"id\": $FirstHuntingGroupId,\n" +
        "            \"clubId\": $FirstClubId,\n" +
        "            \"speciesCode\": 47629,\n" +
        "            \"huntingYear\": 2019,\n" +
        "            \"beginDate\": \"2015-06-02\",\n" +
        "            \"endDate\": \"2015-10-31\",\n" +
        "            \"beginDate2\": null,\n" +
        "            \"endDate2\": null,\n"+
        "            \"permitNumber\": \"2019-1-000-10000-6\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"VHP 2019\",\n" +
        "                \"sv\": \"VHP 2019\"\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": 345,\n" +
        "            \"clubId\": $FirstClubId,\n" +
        "            \"speciesCode\": 47503,\n" +
        "            \"huntingYear\": 2019,\n" +
        "            \"beginDate\": \"2015-06-01\",\n" +
        "            \"endDate\": \"2015-10-30\",\n" +
        "            \"beginDate2\": null,\n" +
        "            \"endDate2\": null,\n"+
        "            \"permitNumber\": \"2019-1-000-10000-6\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"Hirvi 2019\",\n" +
        "                \"sv\": \"Hirvi 2019\"\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": 346,\n" +
        "            \"clubId\": $SecondClubId,\n" +
        "            \"speciesCode\": 47503,\n" +
        "            \"huntingYear\": 2021,\n" +
        "            \"beginDate\": \"2021-06-01\",\n" +
        "            \"endDate\": \"2021-10-30\",\n" +
        "            \"beginDate2\": null,\n" +
        "            \"endDate2\": null,\n"+
        "            \"permitNumber\": \"2021-1-000-10002-5\",\n" +
        "            \"name\": {\n" +
        "                \"fi\": \"Hirviryhmä 21\",\n" +
        "                \"sv\": \"Hirviryhmä 21\"\n" +
        "            }\n" +
        "        }\n" +
        "    ]\n" +
        "}"

    const val Members =
        "[\n" +
        "    {\n" +
        "        \"id\": 141,\n" +
        "        \"occupationType\": \"RYHMAN_JASEN\",\n" +
        "        \"personId\": 8,\n" +
        "        \"firstName\": \"Veikko\",\n" +
        "        \"lastName\": \"Vartio\",\n" +
        "        \"hunterNumber\": \"55555555\",\n" +
        "        \"beginDate\": null,\n" +
        "        \"endDate\": null\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": 144,\n" +
        "        \"occupationType\": \"RYHMAN_METSASTYKSENJOHTAJA\",\n" +
        "        \"personId\": 3,\n" +
        "        \"firstName\": \"Asko\",\n" +
        "        \"lastName\": \"Partanen\",\n" +
        "        \"hunterNumber\": \"11111111\",\n" +
        "        \"beginDate\": null,\n" +
        "        \"endDate\": null\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": 142,\n" +
        "        \"occupationType\": \"RYHMAN_JASEN\",\n" +
        "        \"personId\": 10,\n" +
        "        \"firstName\": \"Reijo\",\n" +
        "        \"lastName\": \"Kuivariihi\",\n" +
        "        \"hunterNumber\": \"77777777\",\n" +
        "        \"beginDate\": null,\n" +
        "        \"endDate\": null\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": 143,\n" +
        "        \"occupationType\": \"RYHMAN_JASEN\",\n" +
        "        \"personId\": 4,\n" +
        "        \"firstName\": \"Pentti\",\n" +
        "        \"lastName\": \"Mujunen\",\n" +
        "        \"hunterNumber\": \"88888888\",\n" +
        "        \"beginDate\": null,\n" +
        "        \"endDate\": null\n" +
        "    }\n" +
        "]"

    const val HuntingArea =
        "{\n" +
        "  \"areaId\" : 28,\n" +
        "  \"externalId\": \"3EM48F3PXA\",\n" +
        "  \"bounds\" : {\n" +
        "    \"minLng\" : 23.44006,\n" +
        "    \"minLat\" : 61.506074,\n" +
        "    \"maxLng\" : 23.554391,\n" +
        "    \"maxLat\" : 61.54017\n" +
        "  }\n" +
        "}"

    const val GroupStatus =
        "{\n" +
        "    \"canCreateHuntingDay\": true,\n" +
        "    \"canCreateHarvest\": true,\n" +
        "    \"canCreateObservation\": true,\n" +
        "    \"canEditDiaryEntry\": true,\n" +
        "    \"canEditHuntingDay\": true\n" +
        "}"

    const val GroupStatusHuntingEnded =
        "{\n" +
                "    \"canCreateHuntingDay\": true,\n" +
                "    \"canCreateHarvest\": true,\n" +
                "    \"canCreateObservation\": true,\n" +
                "    \"canEditDiaryEntry\": true,\n" +
                "    \"canEditHuntingDay\": true,\n" +
                "    \"huntingFinished\": true\n" +
                "}"

    const val GroupStatusCantEditOrCreate =
        """
        {
            "canCreateHuntingDay": false,
            "canCreateHarvest": false,
            "canCreateObservation": false,
            "canEditDiaryEntry": false,
            "canEditHuntingDay": false
        }
        """

    val FirstHuntingDayId = GroupHuntingDayId.remote(5L)
    private val SecondHuntingDayId = GroupHuntingDayId.remote(6L)
    private val CreatedHuntingDayId = GroupHuntingDayId.remote(7L)
    val DeerHuntingDayId = GroupHuntingDayId.remote(8L)
    private val AddedAfterHuntingDayId = GroupHuntingDayId.remote(9L)
    private val AddedBeforeHuntingDayId = GroupHuntingDayId.remote(10L)

    const val FirstHuntingDayDate = "2015-09-01"
    val FirstHuntingDay =
        """
        {
            "id": ${FirstHuntingDayId.remoteId},
            "rev": 0,
            "huntingGroupId": $FirstHuntingGroupId,
            "startDate": "$FirstHuntingDayDate",
            "endDate": "$FirstHuntingDayDate",
            "startTime": "06:00",
            "endTime": "21:00",
            "durationInMinutes": 900,
            "breakDurationInMinutes": 0,
            "snowDepth": 123,
            "huntingMethod": "PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA",
            "numberOfHunters": 23,
            "numberOfHounds": 1,
            "createdBySystem": false
        }
        """
    val SecondHuntingDay =
        """
        {
            "id": ${SecondHuntingDayId.remoteId},
            "rev": 0,
            "huntingGroupId": $FirstHuntingGroupId,
            "startDate": "2015-09-03",
            "endDate": "2015-09-03",
            "startTime": "06:00",
            "endTime": "21:00",
            "durationInMinutes": 750,
            "breakDurationInMinutes": null,
            "snowDepth": null,
            "huntingMethod": null,
            "numberOfHunters": null,
            "numberOfHounds": null,
            "createdBySystem": false
        }
        """
    const val AddedAfterHuntingDayDate = "2015-09-12"
    val AddedAfterHuntingDay =
        """
        {
            "id": ${AddedAfterHuntingDayId.remoteId},
            "rev": 0,
            "huntingGroupId": $FirstHuntingGroupId,
            "startDate": "$AddedAfterHuntingDayDate",
            "endDate": "$AddedAfterHuntingDayDate",
            "startTime": "06:00",
            "endTime": "21:00",
            "durationInMinutes": 750,
            "breakDurationInMinutes": null,
            "snowDepth": null,
            "huntingMethod": null,
            "numberOfHunters": null,
            "numberOfHounds": null,
            "createdBySystem": false
        }
        """
    const val AddedBeforeHuntingDayDate = "2015-08-28"
    val AddedBeforeHuntingDay =
        """
        {
            "id": ${AddedBeforeHuntingDayId.remoteId},
            "rev": 0,
            "huntingGroupId": $FirstHuntingGroupId,
            "startDate": "$AddedBeforeHuntingDayDate",
            "endDate": "$AddedBeforeHuntingDayDate",
            "startTime": "06:00",
            "endTime": "21:00",
            "durationInMinutes": 750,
            "breakDurationInMinutes": null,
            "snowDepth": null,
            "huntingMethod": null,
            "numberOfHunters": null,
            "numberOfHounds": null,
            "createdBySystem": false
        }
        """

    val DeerHuntingDay =
        """
        {
            "id": ${DeerHuntingDayId.remoteId},
            "rev": 0,
            "huntingGroupId": $SecondHuntingGroupId,
            "startDate": "2015-09-03",
            "endDate": "2015-09-03",
            "startTime": "06:00",
            "endTime": "21:00",
            "durationInMinutes": 750,
            "breakDurationInMinutes": null,
            "snowDepth": null,
            "huntingMethod": null,
            "numberOfHunters": null,
            "numberOfHounds": null,
            "createdBySystem": true
        }
        """

    val GroupHuntingDays =
        """
        [
          $FirstHuntingDay,
          $SecondHuntingDay
        ]
        """
    val GroupHuntingDaysOneDayAddedAfter =
        """
        [
          $FirstHuntingDay,
          $SecondHuntingDay,
          $AddedAfterHuntingDay
        ]
        """
    val GroupHuntingDaysOneDayAddedBefore =
        """
        [
          $FirstHuntingDay,
          $SecondHuntingDay,
          $AddedBeforeHuntingDay
        ]
        """
    val GroupHuntingDaysOneDayAddedBeforeAndAfter =
        """
        [
          $FirstHuntingDay,
          $SecondHuntingDay,
          $AddedBeforeHuntingDay,
          $AddedAfterHuntingDay
        ]
        """
    val UpdatedFirstHuntingDay =
        """
        {
          "id": ${FirstHuntingDayId.remoteId},
          "rev": 0,
          "huntingGroupId": $FirstHuntingGroupId,
          "startDate": "2015-09-01",
          "endDate": "2015-09-01",
          "startTime": "07:00",
          "endTime": "20:00",
          "durationInMinutes": 750,
          "breakDurationInMinutes": 30,
          "snowDepth": 120,
          "huntingMethod": "HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE",
          "numberOfHunters": 20,
          "numberOfHounds": 2,
          "createdBySystem": false
        }
        """

    val GroupHuntingDaysAfterUpdate =
        """
        [
          $UpdatedFirstHuntingDay,
          $SecondHuntingDay
        ]
        """

    val CreatedGroupHuntingDay =
        """
        {
          "id": ${CreatedHuntingDayId.remoteId},
          "rev": 0,
          "huntingGroupId": $FirstHuntingGroupId,
          "startDate": "2015-09-04",
          "endDate": "2015-09-04",
          "startTime": "08:30",
          "endTime": "19:30",
          "durationInMinutes": 570,
          "breakDurationInMinutes": 90,
          "snowDepth": 90,
          "huntingMethod": "HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE",
          "numberOfHunters": 20,
          "numberOfHounds": 2,
          "createdBySystem": false
        }
        """

    val GroupHuntingDaysAfterCreate =
        """
        [
          $FirstHuntingDay,
          $SecondHuntingDay,
          $CreatedGroupHuntingDay
        ]
        """

    const val FirstHarvestId = 949L // Approved harvest
    const val SecondHarvestId = 946L // Not approved harvest
    const val ThirdHarvestId = 951L // Not approved harvest with white tailed deer
    const val FirstObservationId = 29L
    const val SecondObservationId = 30L
    const val ObservationDate = "2015-09-10"

    val GroupHuntingDiary =
        """
        {
          "harvests" : [ {
            "id" : $FirstHarvestId,
            "rev" : 2,
            "type" : "HARVEST",
            "geoLocation" : {
              "latitude" : 6820960,
              "longitude" : 318112,
              "source" : "MANUAL",
              "accuracy" : 0.0,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-01T14:00:00.000",
            "description" : "1234",
            "canEdit" : true,
            "imageIds" : [ ],
            "gameSpeciesCode" : 47503,
            "harvestSpecVersion" : 8,
            "harvestReportRequired" : false,
            "harvestReportState" : "SENT_FOR_APPROVAL",
            "permitNumber" : "12323-33323",
            "stateAcceptedToHarvestPermit" : "PROPOSED",
            "specimens" : [ {
              "id" : 413,
              "rev" : 1,
              "gender" : "MALE",
              "age" : "ADULT",
              "weight" : null,
              "weightEstimated" : 34.0,
              "weightMeasured" : 4.0,
              "fitnessClass" : "NAANTYNYT",
              "antlersLost" : false,
              "antlersType" : "HANKO",
              "antlersWidth" : 24,
              "antlerPointsLeft" : 4,
              "antlerPointsRight" : 1,
              "notEdible" : false,
              "additionalInfo" : "additional_info"
            } ],
            "deerHuntingType" : "DOG_HUNTING",
            "deerHuntingOtherTypeDescription" : null,
            "apiVersion" : 2,
            "mobileClientRefId" : null,
            "amount" : 1,
            "harvestReportDone" : false,
            "huntingDayId" : ${FirstHuntingDayId.remoteId},
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : "Mr."
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : null
            }
          },

          {
            "id" : $SecondHarvestId,
            "rev" : 4,
            "type" : "HARVEST",
            "geoLocation" : {
              "latitude" : 6827036,
              "longitude" : 314265,
              "source" : "MANUAL",
              "accuracy" : 0.0,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-01T11:11:00.000",
            "description" : null,
            "canEdit" : false,
            "imageIds" : [ ],
            "gameSpeciesCode" : 47503,
            "harvestSpecVersion" : 8,
            "harvestReportRequired" : false,
            "harvestReportState" : null,
            "permitNumber" : null,
            "stateAcceptedToHarvestPermit" : null,
            "specimens" : [ {
              "id" : 408,
              "rev" : 1,
              "gender" : "MALE",
              "age" : "ADULT",
              "weight" : null
            } ],
            "deerHuntingType" : null,
            "deerHuntingOtherTypeDescription" : null,
            "apiVersion" : 2,
            "mobileClientRefId" : null,
            "amount" : 1,
            "harvestReportDone" : false,
            "huntingDayId" : null,
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : null
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : null
            }
          },

          {
            "id" : $ThirdHarvestId,
            "rev" : 4,
            "type" : "HARVEST",
            "geoLocation" : {
              "latitude" : 6827036,
              "longitude" : 314265,
              "source" : "MANUAL",
              "accuracy" : 0.0,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-01T11:11:00.000",
            "description" : null,
            "canEdit" : false,
            "imageIds" : [ ],
            "gameSpeciesCode" : 47629,
            "harvestSpecVersion" : 8,
            "harvestReportRequired" : false,
            "harvestReportState" : null,
            "permitNumber" : null,
            "stateAcceptedToHarvestPermit" : null,
            "specimens" : [ {
              "id" : 408,
              "rev" : 1,
              "gender" : "MALE",
              "age" : "ADULT",
              "weight" : null,
              "notEdible" : false,
              "antlersLost" : false
            } ],
            "deerHuntingType" : null,
            "deerHuntingOtherTypeDescription" : null,
            "apiVersion" : 2,
            "mobileClientRefId" : null,
            "amount" : 1,
            "harvestReportDone" : false,
            "huntingDayId" : null,
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : null
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : null
            }
          }],


          "observations" : [ {
            "id" : $FirstObservationId,
            "rev" : 1,
            "type" : "OBSERVATION",
            "geoLocation" : {
              "latitude" : 6789568,
              "longitude" : 330224,
              "source" : "MANUAL",
              "accuracy" : 1.2,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "${ObservationDate}T13:00:00.000",
            "description" : "It was an animal",
            "canEdit" : false,
            "imageIds" : [ "9a632219-d58c-4882-b91d-439229df1644" ],
            "gameSpeciesCode" : 47503,
            "observationType" : "NAKO",
            "observationCategory" : "MOOSE_HUNTING",
            "inYardDistanceToResidence" : null,
            "verifiedByCarnivoreAuthority" : null,
            "observerName" : null,
            "observerPhoneNumber" : null,
            "officialAdditionalInfo" : null,
            "specimens" : [
                {
                      "id" : 20,
                      "rev" : 0,
                      "gender" : "FEMALE",
                      "age" : "UNKNOWN",
                      "state" : "WOUNDED",
                      "marking" : "EARMARK",
                      "widthOfPaw" : null,
                      "lengthOfPaw" : null
                }
            ],
            "pack" : true,
            "litter" : true,
            "mobileClientRefId" : null,
            "observationSpecVersion" : 3,
            "withinMooseHunting" : null,
            "linkedToGroupHuntingDay" : false,
            "huntingDayId" : null,
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : null
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : "Mr."
            },
            "totalSpecimenAmount" : 1,
            "mooselikeMaleAmount" : 0,
            "mooselikeFemaleAmount" : 1,
            "mooselikeCalfAmount" : 2,
            "mooselikeFemale1CalfAmount" : 3,
            "mooselikeFemale2CalfsAmount" : 4,
            "mooselikeFemale3CalfsAmount" : 5,
            "mooselikeUnknownSpecimenAmount" : 6,
            "inYardDistanceToResidence" : null,
            "verifiedByCarnivoreAuthority" : null
          },
          {
            "id" : $SecondObservationId,
            "rev" : 1,
            "type" : "OBSERVATION",
            "geoLocation" : {
              "latitude" : 6789568,
              "longitude" : 330224,
              "source" : "MANUAL",
              "accuracy" : 1.2,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-03T13:00:00.000",
            "description" : "It was an animal",
            "canEdit" : false,
            "imageIds" : [ "9a632219-d58c-4882-b91d-439229df1644" ],
            "gameSpeciesCode" : 47629,
            "observationType" : "NAKO",
            "observationCategory" : "MOOSE_HUNTING",
            "inYardDistanceToResidence" : null,
            "verifiedByCarnivoreAuthority" : null,
            "observerName" : null,
            "observerPhoneNumber" : null,
            "officialAdditionalInfo" : null,
            "specimens" : [
                {
                      "id" : 20,
                      "rev" : 0,
                      "gender" : "FEMALE",
                      "age" : "UNKNOWN",
                      "state" : "WOUNDED",
                      "marking" : "EARMARK",
                      "widthOfPaw" : null,
                      "lengthOfPaw" : null
                }
            ],
            "pack" : true,
            "litter" : true,
            "mobileClientRefId" : null,
            "observationSpecVersion" : 4,
            "withinMooseHunting" : null,
            "linkedToGroupHuntingDay" : false,
            "huntingDayId" : null,
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : null
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : "Mr."
            },
            "totalSpecimenAmount" : 1,
            "mooselikeMaleAmount" : 0,
            "mooselikeFemaleAmount" : 1,
            "mooselikeCalfAmount" : 2,
            "mooselikeFemale1CalfAmount" : 3,
            "mooselikeFemale2CalfsAmount" : 4,
            "mooselikeFemale3CalfsAmount" : 5,
            "mooselikeFemale3CalfsAmount" : 6,
            "mooselikeFemale4CalfsAmount" : 7,
            "mooselikeUnknownSpecimenAmount" : 8,
            "inYardDistanceToResidence" : null,
            "verifiedByCarnivoreAuthority" : null
          } ],

          "rejectedHarvests": [],

          "rejectedObservations": []
        }
        """

    const val EmptyGroupHuntingDiary =
        """
        {
            "harvests" : [],
            "observations" : [],
            "rejectedHarvests": [],
            "rejectedObservations": []
        }
        """

    val CreatedHarvest =
        """
            {
            "id" : 966,
            "rev" : 0,
            "type" : "HARVEST",
            "geoLocation" : {
              "latitude" : 6820960,
              "longitude" : 318112,
              "source" : "MANUAL",
              "accuracy" : 0.0,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-01T14:00:00.000",
            "description" : "1234",
            "canEdit" : false,
            "imageIds" : [ ],
            "gameSpeciesCode" : 47503,
            "harvestSpecVersion" : 8,
            "harvestReportRequired" : false,
            "harvestReportState" : "SENT_FOR_APPROVAL",
            "permitNumber" : "12323-33323",
            "stateAcceptedToHarvestPermit" : "PROPOSED",
            "specimens" : [ {
              "id" : 413,
              "rev" : 1,
              "gender" : "MALE",
              "age" : "ADULT",
              "weight" : null,
              "weightEstimated" : 34.0,
              "weightMeasured" : 4.0,
              "fitnessClass" : "NAANTYNYT",
              "antlersType" : "HANKO",
              "antlersWidth" : 24,
              "antlerPointsLeft" : 4,
              "antlerPointsRight" : 1,
              "notEdible" : false,
              "additionalInfo" : "additional_info"
            } ],
            "deerHuntingType" : "DOG_HUNTING",
            "deerHuntingOtherTypeDescription" : null,
            "apiVersion" : 2,
            "mobileClientRefId" : null,
            "amount" : 1,
            "harvestReportDone" : false,
            "huntingDayId" : ${FirstHuntingDayId.remoteId},
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : "Mr."
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : null
            }
          }
        """
    val AcceptedHarvest =
        """
        {
            "id" : 949,
            "rev" : 3,
            "type" : "HARVEST",
            "geoLocation" : {
              "latitude" : 6820960,
              "longitude" : 318112,
              "source" : "MANUAL",
              "accuracy" : 0.0,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-01T14:00:00.000",
            "description" : "1234",
            "canEdit" : false,
            "imageIds" : [ ],
            "gameSpeciesCode" : 47503,
            "harvestSpecVersion" : 8,
            "harvestReportRequired" : false,
            "harvestReportState" : "SENT_FOR_APPROVAL",
            "permitNumber" : "12323-33323",
            "stateAcceptedToHarvestPermit" : "PROPOSED",
            "specimens" : [ {
              "id" : 413,
              "rev" : 1,
              "gender" : "MALE",
              "age" : "ADULT",
              "weight" : null,
              "weightEstimated" : 34.0,
              "weightMeasured" : 4.0,
              "fitnessClass" : "NAANTYNYT",
              "antlersType" : "HANKO",
              "antlersWidth" : 24,
              "antlerPointsLeft" : 4,
              "antlerPointsRight" : 1,
              "notEdible" : false,
              "additionalInfo" : "additional_info"
            } ],
            "deerHuntingType" : "DOG_HUNTING",
            "deerHuntingOtherTypeDescription" : null,
            "apiVersion" : 2,
            "mobileClientRefId" : null,
            "amount" : 1,
            "harvestReportDone" : false,
            "huntingDayId" : ${FirstHuntingDayId.remoteId},
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : "Mr."
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : null
            }
          }
        """
    const val ProposedHarvest =
        """
          {
            "id" : 980,
            "rev" : 0,
            "type" : "HARVEST",
            "geoLocation" : {
              "latitude" : 6820960,
              "longitude" : 318112,
              "source" : "MANUAL",
              "accuracy" : 0.0,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-04T14:00:00.000",
            "description" : "1234",
            "canEdit" : false,
            "imageIds" : [ ],
            "gameSpeciesCode" : 47503,
            "harvestSpecVersion" : 8,
            "harvestReportRequired" : false,
            "harvestReportState" : "SENT_FOR_APPROVAL",
            "permitNumber" : "12323-33323",
            "stateAcceptedToHarvestPermit" : "PROPOSED",
            "specimens" : [ {
              "id" : 413,
              "rev" : 1,
              "gender" : "MALE",
              "age" : "ADULT",
              "weight" : null,
              "weightEstimated" : 34.0,
              "weightMeasured" : 4.0,
              "fitnessClass" : "NAANTYNYT",
              "antlersType" : "HANKO",
              "antlersWidth" : 24,
              "antlerPointsLeft" : 4,
              "antlerPointsRight" : 1,
              "notEdible" : false,
              "additionalInfo" : "additional_info"
            } ],
            "deerHuntingType" : "DOG_HUNTING",
            "deerHuntingOtherTypeDescription" : null,
            "apiVersion" : 2,
            "mobileClientRefId" : null,
            "amount" : 1,
            "harvestReportDone" : false,
            "huntingDayId" : null,
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : "Mr."
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : null
            }
          }
        """

    val AcceptedObservation =
        """
          {
            "id" : $SecondObservationId,
            "rev" : 1,
            "type" : "OBSERVATION",
            "geoLocation" : {
              "latitude" : 6789568,
              "longitude" : 330224,
              "source" : "MANUAL",
              "accuracy" : 1.2,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-02T13:00:00.000",
            "description" : "It was an animal",
            "canEdit" : false,
            "imageIds" : [ "9a632219-d58c-4882-b91d-439229df1644" ],
            "gameSpeciesCode" : 47629,
            "observationType" : "NAKO",
            "observationCategory" : "MOOSE_HUNTING",
            "inYardDistanceToResidence" : null,
            "verifiedByCarnivoreAuthority" : null,
            "observerName" : null,
            "observerPhoneNumber" : null,
            "officialAdditionalInfo" : null,
            "specimens" : [
                {
                      "id" : 20,
                      "rev" : 0,
                      "gender" : "FEMALE",
                      "age" : "UNKNOWN",
                      "state" : "WOUNDED",
                      "marking" : "EARMARK",
                      "widthOfPaw" : null,
                      "lengthOfPaw" : null
                }
            ],
            "pack" : true,
            "litter" : true,
            "mobileClientRefId" : null,
            "observationSpecVersion" : 4,
            "withinMooseHunting" : null,
            "linkedToGroupHuntingDay" : false,
            "huntingDayId" : ${FirstHuntingDayId.remoteId},
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : null
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : "Mr."
            },
            "totalSpecimenAmount" : 1,
            "mooselikeMaleAmount" : 0,
            "mooselikeFemaleAmount" : 1,
            "mooselikeCalfAmount" : 2,
            "mooselikeFemale1CalfAmount" : 3,
            "mooselikeFemale2CalfsAmount" : 4,
            "mooselikeFemale3CalfsAmount" : 5,
            "mooselikeFemale3CalfsAmount" : 6,
            "mooselikeFemale4CalfsAmount" : 7,
            "mooselikeUnknownSpecimenAmount" : 8,
            "inYardDistanceToResidence" : null,
            "verifiedByCarnivoreAuthority" : null
          }
        """

    const val ProposedObservation =
        """
          {
            "id" : 40,
            "rev" : 1,
            "type" : "OBSERVATION",
            "geoLocation" : {
              "latitude" : 6789568,
              "longitude" : 330224,
              "source" : "MANUAL",
              "accuracy" : 1.2,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "pointOfTime" : "2015-09-05T13:00:00.000",
            "description" : "It was an animal",
            "canEdit" : false,
            "imageIds" : [ "9a632219-d58c-4882-b91d-439229df1644" ],
            "gameSpeciesCode" : 47503,
            "observationType" : "NAKO",
            "observationCategory" : "MOOSE_HUNTING",
            "inYardDistanceToResidence" : null,
            "verifiedByCarnivoreAuthority" : null,
            "observerName" : null,
            "observerPhoneNumber" : null,
            "officialAdditionalInfo" : null,
            "specimens" : [
                {
                      "id" : 20,
                      "rev" : 0,
                      "gender" : "FEMALE",
                      "age" : "UNKNOWN",
                      "state" : "WOUNDED",
                      "marking" : "EARMARK",
                      "widthOfPaw" : null,
                      "lengthOfPaw" : null
                }
            ],
            "pack" : true,
            "litter" : true,
            "mobileClientRefId" : null,
            "observationSpecVersion" : 4,
            "withinMooseHunting" : null,
            "linkedToGroupHuntingDay" : false,
            "huntingDayId" : null,
            "authorInfo" : {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : null
            },
            "actorInfo" : {
              "id" : 5,
              "rev" : 14,
              "byName" : "Pentti",
              "lastName" : "Makunen",
              "hunterNumber" : "99999999",
              "extendedName" : "Mr."
            },
            "totalSpecimenAmount" : 1,
            "mooselikeMaleAmount" : 0,
            "mooselikeFemaleAmount" : 1,
            "mooselikeCalfAmount" : 2,
            "mooselikeFemale1CalfAmount" : 3,
            "mooselikeFemale2CalfsAmount" : 4,
            "mooselikeFemale3CalfsAmount" : 5,
            "mooselikeFemale3CalfsAmount" : 6,
            "mooselikeFemale4CalfsAmount" : 7,
            "mooselikeUnknownSpecimenAmount" : 8,
            "inYardDistanceToResidence" : null,
            "verifiedByCarnivoreAuthority" : null
          }
        """

    const val PersonWithHunterNumber88888888 =
        """
            {
              "id" : 4,
              "rev" : 13,
              "byName" : "Pena",
              "lastName" : "Mujunen",
              "hunterNumber" : "88888888",
              "extendedName" : "Mr."
            }
        """

    val HuntingGroupMember88888888 = HuntingGroupMember(
        id = 1,
        occupationType = OccupationType.CLUB_MEMBER.toBackendEnum(),
        personId = 4,
        firstName = "Pentti",
        lastName = "Mujunen",
        hunterNumber = "88888888",
        beginDate = LocalDate(2020, 1, 1),
        endDate = LocalDate(2030, 1, 1),
    )
}

