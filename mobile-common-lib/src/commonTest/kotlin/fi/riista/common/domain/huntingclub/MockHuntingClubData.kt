package fi.riista.common.domain.huntingclub

object MockHuntingClubData {
    const val HuntingClubMemberships =
        """
        [ {
            "id" : 46,
            "occupationType" : "SEURAN_YHDYSHENKILO",
            "name" : {
                "fi" : "Yhteyshenkilö",
                "sv" : "Kontaktperson"
            },
            "beginDate" : null,
            "endDate" : null,
            "organisation" : {
                "id" : 329,
                "name" : {
                    "fi" : "Nokian metsästysseura",
                    "sv" : "Nokian metsästysseura SV"
                },
                "officialCode" : "123456"
            }
        } ]
        """

    const val HuntingClubMemberInvitations =
        """
        [ {
          "id" : 12,
          "rev" : 0,
          "clubId" : 333,
          "personId" : 4,
          "club" : {
            "id" : 333,
            "name" : {
              "fi" : "Porrassalmen eräveikot ry",
              "sv" : "Porrassalmen eräveikot ry"
            },
            "officialCode" : "1007921"
          },
          "person" : {
            "byName" : "Pena",
            "lastName" : "Mujunen",
            "hunterNumber" : "88888888"
          },
          "occupationType" : null,
          "userRejectedTime" : null
        } ]
        """

    const val EmptyHuntingClubMemberships = "[]"
    const val EmptyHuntingClubMemberInvitations = "[]"

    const val HuntingClubSearchResult =
        """
        {
            "id": 399,
            "nameFI": "Nokian metsästysseura",
            "nameSV": "Nokian metsästysseura",
            "officialCode": "1531219"
        }
        """
}
