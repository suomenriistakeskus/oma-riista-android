package fi.riista.common.domain.harvest

object MockHarvestPageData {

    const val harvestPageWithOneHarvest =
        """
    {
      "content" : [ {
        "id" : 1940,
        "rev" : 5,
        "type" : "HARVEST",
        "geoLocation" : {
          "latitude" : 6900707,
          "longitude" : 440517,
          "source" : "GPS_DEVICE",
          "accuracy" : 12.302000045776367,
          "altitude" : 21.4,
          "altitudeAccuracy" : 0.6
        },
        "pointOfTime" : "2023-01-18T15:47:00.000",
        "description" : "Small animal",
        "canEdit" : true,
        "imageIds" : [ "86f733ec-8105-48ab-80ce-727dbd1e7a96" ],
        "gameSpeciesCode" : 47230,
        "harvestSpecVersion" : 9,
        "harvestReportRequired" : true,
        "harvestReportState" : "SENT_FOR_APPROVAL",
        "permitNumber" : "2022-1-000-10005-7",
        "permitType" : "Poikkeuslupa riistanisäkkäille",
        "stateAcceptedToHarvestPermit" : "ACCEPTED",
        "specimens" : [ {
          "id" : 1078,
          "rev" : 3,
          "gender" : "FEMALE",
          "age" : "YOUNG",
          "weight" : 3.45
        } ],
        "deerHuntingType" : null,
        "deerHuntingOtherTypeDescription" : null,
        "apiVersion" : 2,
        "mobileClientRefId" : 7189988460482656588,
        "amount" : 1,
        "harvestReportDone" : true
      } ],
      "latestEntry" : "2023-01-20T12:52:45.967",
      "hasMore" : false
    }
        """

    const val harvestPageWithUpdatedHarvest =
        """
    {
      "content" : [ {
        "id" : 1940,
        "rev" : 6,
        "type" : "HARVEST",
        "geoLocation" : {
          "latitude" : 6900708,
          "longitude" : 440518,
          "source" : "MANUAL",
          "accuracy" : null,
          "altitude" : null,
          "altitudeAccuracy" : null
        },
        "pointOfTime" : "2023-01-18T15:47:22.000",
        "description" : "Really small animal",
        "canEdit" : true,
        "imageIds" : [ "86f733ec-8105-48ab-80ce-727dbd1e7a96", "a6997f26-dd4f-4687-8145-372365664798" ],
        "gameSpeciesCode" : 47230,
        "harvestSpecVersion" : 9,
        "harvestReportRequired" : true,
        "harvestReportState" : "SENT_FOR_APPROVAL",
        "permitNumber" : "2022-1-000-10005-7",
        "permitType" : "Poikkeuslupa riistanisäkkäille",
        "stateAcceptedToHarvestPermit" : "ACCEPTED",
        "specimens" : [ {
          "id" : 1078,
          "rev" : 3,
          "gender" : "FEMALE",
          "age" : "YOUNG",
          "weight" : 3.45
        } ],
        "deerHuntingType" : null,
        "deerHuntingOtherTypeDescription" : null,
        "apiVersion" : 2,
        "mobileClientRefId" : 7189988460482656588,
        "amount" : 2,
        "harvestReportDone" : true
      } ],
      "latestEntry" : "2023-01-20T12:52:45.967",
      "hasMore" : false
    }
        """

    const val deletedHarvests =
        """
    {
        "latestEntry" : "2016-02-18T11:00:00.000",
        "entryIds" : [ 1940 ]
    }
        """

    const val emptyDeletedHarvests =
        """
    {
        "latestEntry" : null,
        "entryIds" : [ ]
    }
        """

}
