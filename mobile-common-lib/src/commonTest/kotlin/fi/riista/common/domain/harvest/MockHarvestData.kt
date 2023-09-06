package fi.riista.common.domain.harvest

object MockHarvestData {
    const val harvest =
        """
    {
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
      }
        """
}
