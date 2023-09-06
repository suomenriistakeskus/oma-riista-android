package fi.riista.common.domain.observation

object MockObservationData {
    const val observation =
        """
    {
        "id" : 12,
        "rev" : 1,
        "type" : "OBSERVATION",
        "geoLocation" : {
          "latitude" : 7035076,
          "longitude" : 483517,
          "source" : "GPS_DEVICE",
          "accuracy" : 12.302000045776367,
          "altitude" : null,
          "altitudeAccuracy" : null
        },
        "pointOfTime" : "2020-01-30T15:17:34.851",
        "description" : "",
        "canEdit" : true,
        "imageIds" : [ "3d959f28-1bee-4c52-8fb1-ae24383859d0"],
        "gameSpeciesCode" : 26440,
        "observationType" : "ULOSTE",
        "observationCategory" : "NORMAL",
        "inYardDistanceToResidence" : null,
        "verifiedByCarnivoreAuthority" : null,
        "observerName" : null,
        "observerPhoneNumber" : null,
        "officialAdditionalInfo" : null,
        "specimens" : null,
        "pack" : null,
        "litter" : null,
        "mobileClientRefId" : 6787724453918204169,
        "observationSpecVersion" : 4,
        "withinMooseHunting" : null,
        "linkedToGroupHuntingDay" : false,
        "totalSpecimenAmount" : null
      }
        """
}
