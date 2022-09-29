package fi.riista.common.domain.huntingControl

object MockHuntingControlData {

    const val RhyId = 184L
    const val FirstEventId = 1L
    const val FirstAttachmentId = 123L

    const val HuntingControlRhys =
        """
        [ {
          "specVersion" : 1,
          "rhy" : {
            "id" : $RhyId,
            "name" : {
              "fi" : "Tampereen riistanhoitoyhdistys",
              "sv" : "Tammerfors jaktvårdsförening"
            },
            "officialCode" : "376"
          },
          "gameWardens" : [ {
            "inspector" : {
              "id" : 3,
              "firstName" : "Asko",
              "lastName" : "Partanen"
            },
            "beginDate" : "2022-01-13",
            "endDate" : "2026-07-31"
          }, {
            "inspector" : {
              "id" : 4,
              "firstName" : "Pentti",
              "lastName" : "Mujunen"
            },
            "beginDate" : "2022-01-13",
            "endDate" : "2026-07-31"
          } ],
          "events" : [ {
            "specVersion" : 1,
            "id" : $FirstEventId,
            "rev" : 2,
            "mobileClientRefId" : null,
            "eventType" : "DOG_DISCIPLINE_CONTROL",
            "status" : "PROPOSED",
            "inspectors" : [ {
              "id" : 4,
              "firstName" : "Pentti",
              "lastName" : "Mujunen"
            }, {
              "id" : 3,
              "firstName" : "Asko",
              "lastName" : "Partanen"
            } ],
            "cooperationTypes" : [ "POLIISI", "OMA" ],
            "wolfTerritory" : false,
            "otherParticipants" : "Poliisipartio",
            "geoLocation" : {
              "latitude" : 6822000,
              "longitude" : 326316,
              "source" : "MANUAL",
              "accuracy" : null,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "locationDescription" : "Pyynikin uimaranta",
            "date" : "2022-01-13",
            "beginTime" : "11:00",
            "endTime" : "12:00",
            "customers" : 1,
            "proofOrders" : 1,
            "description" : "Kuulemma uimarannalla pidettiin koiria vapaana. Käytiin katsomassa ettei vesilintuja häritty. Yksi masentunut ankka löytyi. Ks. liite.",
            "attachments" : [ {
              "id" : $FirstAttachmentId,
              "fileName" : "IMG_1387.jpg",
              "isImage" : true,
              "thumbnail" : null
            }, {
              "id" : 2,
              "fileName" : "__file.txt",
              "isImage" : false,
              "thumbnail" : null
            } ],
            "changeHistory" : [ {
              "id" : 1,
              "modificationTime" : "2022-01-13T14:03:48.692Z",
              "author" : {
                "id" : 5,
                "firstName" : "Pentti",
                "lastName" : "Mujunen"
              },
              "changeType" : "CREATE"
            }, {
              "id" : 5,
              "modificationTime" : "2022-02-04T07:07:10.332Z",
              "author" : {
                "id" : 5,
                "firstName" : "Pentti",
                "lastName" : "Mujunen"
              },
              "changeType" : "MODIFY"
            }, {
              "id" : 7,
              "modificationTime" : "2022-02-04T07:08:48.610Z",
              "author" : {
                "id" : 4,
                "firstName" : "Asko",
                "lastName" : "Partanen"
              },
              "changeType" : "MODIFY"
            } ],
            "canEdit" : true
          }, {
            "specVersion" : 1,
            "id" : 2,
            "rev" : 6,
            "mobileClientRefId" : 8200227646380466667,
            "eventType" : "MOOSELIKE_HUNTING_CONTROL",
            "status" : "PROPOSED",
            "inspectors" : [ {
              "id" : 4,
              "firstName" : "Pentti",
              "lastName" : "Mujunen"
            }, {
              "id" : 3,
              "firstName" : "Asko",
              "lastName" : "Partanen"
            } ],
            "cooperationTypes" : [ "OMA" ],
            "wolfTerritory" : false,
            "otherParticipants" : null,
            "geoLocation" : {
              "latitude" : 6826864,
              "longitude" : 311756,
              "source" : "MANUAL",
              "accuracy" : null,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "locationDescription" : "Lähellä lampea",
            "date" : "2022-02-01",
            "beginTime" : "12:00",
            "endTime" : "13:00",
            "customers" : 2,
            "proofOrders" : 1,
            "description" : null,
            "attachments" : [ ],
            "canEdit" : true
          } ]
        } ]
        """

    val AttachmentThumbnail = "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"

    val CreatedHuntingControlEvent =
        """
        {
            "specVersion" : 1,
            "id" : 1,
            "rev" : 2,
            "mobileClientRefId" : null,
            "eventType" : "DOG_DISCIPLINE_CONTROL",
            "status" : "PROPOSED",
            "inspectors" : [ {
              "id" : 4,
              "firstName" : "Pentti",
              "lastName" : "Mujunen"
            }, {
              "id" : 3,
              "firstName" : "Asko",
              "lastName" : "Partanen"
            } ],
            "cooperationTypes" : [ "POLIISI", "OMA" ],
            "wolfTerritory" : false,
            "otherParticipants" : "Poliisipartio",
            "geoLocation" : {
              "latitude" : 6822000,
              "longitude" : 326316,
              "source" : "MANUAL",
              "accuracy" : null,
              "altitude" : null,
              "altitudeAccuracy" : null
            },
            "locationDescription" : "Pyynikin uimaranta",
            "date" : "2022-01-13",
            "beginTime" : "11:00",
            "endTime" : "12:00",
            "customers" : 1,
            "proofOrders" : 1,
            "description" : "Kuulemma uimarannalla pidettiin koiria vapaana. Käytiin katsomassa ettei vesilintuja häritty. Yksi masentunut ankka löytyi. Ks. liite.",
            "attachments" : [ {
              "id" : 1,
              "fileName" : "IMG_1387.jpg",
              "isImage" : true,
              "thumbnail" : "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"
            }, {
              "id" : 2,
              "fileName" : "__file.txt",
              "isImage" : false,
              "thumbnail" : null
            } ],
            "canEdit" : true
          }    
        """

    val UpdatedHuntingControlEvent = CreatedHuntingControlEvent

    const val UploadedAttachmentRemoteId = 99L
}
