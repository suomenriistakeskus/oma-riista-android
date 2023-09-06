package fi.riista.common.domain.srva

object MockSrvaEventData {
    const val srvaEvent =
        """
            {
        "id" : 19,
        "rev" : 1,
        "type" : "SRVA",
        "geoLocation" : {
          "latitude" : 7034880,
          "longitude" : 530720,
          "source" : "MANUAL",
          "accuracy" : 0.8,
          "altitude" : 21.4,
          "altitudeAccuracy" : 0.6
        },
        "pointOfTime" : "2016-02-18T11:00:00.000",
        "description" : "Some SRVA event",
        "canEdit" : true,
        "imageIds" : [ "3d959f28-1bee-4c52-8fb1-ae24383859d0"],
        "eventName" : "ACCIDENT",
        "eventType" : "TRAFFIC_ACCIDENT",
        "totalSpecimenAmount" : 1,
        "otherMethodDescription" : null,
        "otherTypeDescription" : null,
        "methods" : [ {
          "name" : "TRACED_WITH_DOG",
          "isChecked" : true
        }, {
          "name" : "TRACED_WITHOUT_DOG",
          "isChecked" : false
        }, {
          "name" : "OTHER",
          "isChecked" : false
        } ],
        "personCount" : 99,
        "timeSpent" : 88,
        "eventResult" : "ANIMAL_FOUND_AND_NOT_TERMINATED",
        "authorInfo" : {
          "id" : 4,
          "rev" : 15,
          "byName" : "Pena",
          "lastName" : "Mujunen",
          "hunterNumber" : "88888888",
          "extendedName" : null
        },
        "specimens" : [ {
          "gender" : "FEMALE",
          "age" : "YOUNG"
        } ],
        "rhyId" : 209,
        "state" : "REJECTED",
        "otherSpeciesDescription" : null,
        "gameSpeciesCode" : 47507,
        "approverInfo" : {
          "firstName" : "Pentti",
          "lastName" : "Mujunen"
        },
        "mobileClientRefId" : 123456,
        "srvaEventSpecVersion" : 2
      }
        """
}
