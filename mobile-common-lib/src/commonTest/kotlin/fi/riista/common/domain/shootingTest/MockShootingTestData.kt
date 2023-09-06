package fi.riista.common.domain.shootingTest

object MockShootingTestData {
    const val events = """
    [ {
      "rhyId" : 180,
      "calendarEventId" : 130,
      "shootingTestEventId" : 89,
      "calendarEventType" : "AMPUMAKOE",
      "name" : "koe 1",
      "description" : "Eka koe",
      "date" : "2023-02-21",
      "beginTime" : "12:05",
      "endTime" : "14:15",
      "lockedTime" : null,
      "venue" : {
        "id" : 104,
        "rev" : 1,
        "name" : "test",
        "address" : {
          "id" : 141,
          "rev" : 1,
          "streetAddress" : "Kisapaikankatu 1",
          "postalCode" : "12345",
          "city" : "Pellonpieli"
        },
        "info" : "Infoa"
      },
      "officials" : [ {
        "id" : 450,
        "shootingTestEventId" : 89,
        "occupationId" : 247,
        "personId" : 4,
        "firstName" : "Pena",
        "lastName" : "Mujunen",
        "shootingTestResponsible" : true
      }, {
        "id" : 451,
        "shootingTestEventId" : 89,
        "occupationId" : 246,
        "personId" : 8,
        "firstName" : "Veikko",
        "lastName" : "Vartio",
        "shootingTestResponsible" : null
      } ],
      "numberOfAllParticipants" : 3,
      "numberOfParticipantsWithNoAttempts" : 2,
      "numberOfCompletedParticipants" : 1,
      "totalPaidAmount" : 3.21,
      "lastModifier" : {
        "firstName" : "Pentti",
        "lastName" : "Mujunen",
        "adminOrModerator" : false,
        "timestamp" : "2023-02-22T10:14:35.926"
      }
    } ]
        """

    const val firstEvent = """
    {
      "rhyId" : 184,
      "calendarEventId" : 90,
      "shootingTestEventId" : 61,
      "calendarEventType" : "AMPUMAKOE",
      "name" : "Nimi",
      "description" : "Kuvaus",
      "date" : "2020-05-21",
      "beginTime" : "18:00",
      "endTime" : "19:20",
      "lockedTime" : null,
      "venue" : {
        "id" : 2,
        "rev" : 0,
        "name" : "Takamestän rata0",
        "address" : {
          "id" : 33,
          "rev" : 0,
          "streetAddress" : "Takamettätie 222",
          "postalCode" : "33270",
          "city" : "Takametsä"
        },
        "info" : null
      },
      "officials" : [ {
        "id" : 360,
        "shootingTestEventId" : 61,
        "occupationId" : 79,
        "personId" : 11,
        "firstName" : "Kimmo",
        "lastName" : "Alakoski",
        "shootingTestResponsible" : true
      }, {
        "id" : 362,
        "shootingTestEventId" : 61,
        "occupationId" : 122,
        "personId" : 92,
        "firstName" : "Jenna",
        "lastName" : "Aromaa",
        "shootingTestResponsible" : null
      } ],
      "numberOfAllParticipants" : 5,
      "numberOfParticipantsWithNoAttempts" : 3,
      "numberOfCompletedParticipants" : 2,
      "totalPaidAmount" : 15.50,
      "lastModifier" : {
        "firstName" : "Pentti",
        "lastName" : "Mujunen",
        "adminOrModerator" : false,
        "timestamp" : "2020-05-25T12:16:22.922"
      }
    }
    """

    const val officials = """
       [ {
          "occupationId" : 79,
          "personId" : 11,
          "lastName" : "Alakoski",
          "firstName" : "Kimmo",
          "shootingTestResponsible" : true
        }, {
          "occupationId" : 122,
          "personId" : 92,
          "lastName" : "Aromaa",
          "firstName" : "Jenna",
          "shootingTestResponsible" : null
        }, {
          "occupationId" : 115,
          "personId" : 85,
          "lastName" : "Haanpää",
          "firstName" : "Annukka",
          "shootingTestResponsible" : null
        } 
       ]
    """

    const val participant = """
        {
          "id" : 124,
          "rev" : 1,
          "firstName" : "Asko",
          "lastName" : "Partanen",
          "hunterNumber" : "11111111",
          "mooseTestIntended" : false,
          "bearTestIntended" : true,
          "deerTestIntended" : true,
          "bowTestIntended" : false,
          "attempts" : [ {
            "type" : "BEAR",
            "attemptCount" : 1,
            "qualified" : true
          } ],
          "totalDueAmount" : 20.00,
          "paidAmount" : 0,
          "remainingAmount" : 20.00,
          "registrationTime" : "2023-03-06T10:13:50.480Z",
          "completed" : false
        }
    """

    const val participantDetailed = """
       {
          "id" : 124,
          "rev" : 1,
          "firstName" : "Asko",
          "lastName" : "Partanen",
          "hunterNumber" : "11111111",
          "dateOfBirth" : "1911-11-11",
          "mooseTestIntended" : false,
          "bearTestIntended" : false,
          "deerTestIntended" : false,
          "bowTestIntended" : true,
          "registrationTime" : "2023-03-06T10:13:50.480Z",
          "completed" : false,
          "attempts" : [ {
            "id" : 179,
            "rev" : 1,
            "type" : "BEAR",
            "result" : "QUALIFIED",
            "hits" : 4,
            "note" : "Hyvin osui",
            "author" : null
          } ]
        } 
    """

    const val person = """
        {
          "id" : 3,
          "firstName" : "Asko",
          "lastName" : "Partanen",
          "hunterNumber" : "11111111",
          "dateOfBirth" : "1911-11-11",
          "foreignPerson" : false,
          "registrationStatus" : "IN_PROGRESS",
          "selectedShootingTestTypes" : {
            "mooseTestIntended" : false,
            "bearTestIntended" : true,
            "roeDeerTestIntended" : true,
            "bowTestIntended" : false
          }
        }
    """

    const val participants = """
        [ {
          "id" : 124,
          "rev" : 3,
          "firstName" : "Asko",
          "lastName" : "Partanen",
          "hunterNumber" : "11111111",
          "mooseTestIntended" : false,
          "bearTestIntended" : true,
          "deerTestIntended" : true,
          "bowTestIntended" : false,
          "attempts" : [ {
            "type" : "BEAR",
            "attemptCount" : 2,
            "qualified" : true
          } ],
          "totalDueAmount" : 20.00,
          "paidAmount" : 20.00,
          "remainingAmount" : 0.00,
          "registrationTime" : "2023-03-06T10:13:50.480Z",
          "completed" : false
        } ]
    """

    const val attempt = """
        {
          "id" : 179,
          "rev" : 1,
          "participantId" : 124,
          "participantRev" : 3,
          "type" : "BEAR",
          "result" : "QUALIFIED",
          "hits" : 4,
          "note" : "Osumia tuli"
        }
    """
}
