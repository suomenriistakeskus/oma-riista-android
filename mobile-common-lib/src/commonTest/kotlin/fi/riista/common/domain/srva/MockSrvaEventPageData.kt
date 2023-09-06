package fi.riista.common.domain.srva

object MockSrvaEventPageData {
    const val srvaPageWithOneEvent =
        """
    {
      "content" : [ {
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
        "mobileClientRefId" : null,
        "srvaEventSpecVersion" : 2
      } ],
      "latestEntry" : "2016-02-18T11:00:00.000",
      "hasMore" : false
    }
        """

    const val srvaPageWithUpdatedEvent =
        """
    {
      "content" : [ {
        "id" : 19,
        "rev" : 2,
        "type" : "SRVA",
        "geoLocation" : {
          "latitude" : 7034882,
          "longitude" : 530722,
          "source" : "MANUAL",
          "accuracy" : 0.9,
          "altitude" : 21.5,
          "altitudeAccuracy" : 0.7
        },
        "pointOfTime" : "2016-02-18T12:01:20.000",
        "description" : "Some SRVA event2",
        "canEdit" : false,
        "imageIds" : [ "3d959f28-1bee-4c52-8fb1-ae24383859d0", "3d959f28-1bee-4c52-8fb1-ae24383859d2"],
        "eventName" : "ACCIDENT",
        "eventType" : "TRAFFIC_ACCIDENT",
        "totalSpecimenAmount" : 1,
        "otherMethodDescription" : null,
        "otherTypeDescription" : null,
        "methods" : [ {
          "name" : "TRACED_WITH_DOG",
          "isChecked" : false
        }, {
          "name" : "TRACED_WITHOUT_DOG",
          "isChecked" : true
        }, {
          "name" : "OTHER",
          "isChecked" : false
        } ],
        "personCount" : 98,
        "timeSpent" : 87,
        "eventResult" : "ANIMAL_FOUND_AND_NOT_TERMINATED",
        "authorInfo" : {
          "id" : 4,
          "rev" : 16,
          "byName" : "Pentikäinen",
          "lastName" : "Mujunen",
          "hunterNumber" : "88888888",
          "extendedName" : null
        },
        "specimens" : [ {
          "gender" : "MALE",
          "age" : "ADULT"
        } ],
        "rhyId" : 20,
        "state" : "ACCEPTED",
        "otherSpeciesDescription" : null,
        "gameSpeciesCode" : 47507,
        "approverInfo" : {
          "firstName" : "Rauno",
          "lastName" : "Koskinen"
        },
        "mobileClientRefId" : 123890,
        "srvaEventSpecVersion" : 2
      } ],
      "latestEntry" : "2016-02-18T11:00:00.000",
      "hasMore" : false
    }
        """

    const val emptySrvaPage =
        """
    {
      "content" : [],
      "latestEntry" : null,
      "hasMore" : false
    }
        """

    const val deletedSrvaEvents =
        """
    {
        "latestEntry" : "2016-02-18T11:00:00.000",
        "entryIds" : [ 19 ]
    }
        """

    const val emptyDeletedSrvaEvents =
        """
    {
        "latestEntry" : null,
        "entryIds" : []
    }
        """
}


