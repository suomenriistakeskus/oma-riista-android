package fi.riista.common.domain.srva.metadata

object MockSrvaMetadata {
    /**
     * The SRVA metadata json for spec version 2
     */
    const val METADATA_SPEC_VERSION_2: String =
        """
        {
          "ages" : [ "ADULT", "YOUNG", "UNKNOWN" ],
          "genders" : [ "FEMALE", "MALE", "UNKNOWN" ],
          "species" : [ {
            "code" : 47503,
            "name" : {
              "fi" : "hirvi",
              "sv" : "älg",
              "en" : "moose"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 47629,
            "name" : {
              "fi" : "valkohäntäpeura",
              "sv" : "vitsvanshjort",
              "en" : "white-tailed deer"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 47507,
            "name" : {
              "fi" : "metsäkauris",
              "sv" : "rådjur",
              "en" : "roe deer"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 200556,
            "name" : {
              "fi" : "metsäpeura",
              "sv" : "skogsren",
              "en" : "wild forest reindeer"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 47484,
            "name" : {
              "fi" : "kuusipeura",
              "sv" : "dovhjort",
              "en" : "fallow deer"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 47926,
            "name" : {
              "fi" : "villisika",
              "sv" : "vildsvin",
              "en" : "wild boar"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 46615,
            "name" : {
              "fi" : "ilves",
              "sv" : "lodjur",
              "en" : "lynx"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 47348,
            "name" : {
              "fi" : "karhu",
              "sv" : "björn",
              "en" : "brown bear"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 46549,
            "name" : {
              "fi" : "susi",
              "sv" : "varg",
              "en" : "wolf"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          }, {
            "code" : 47212,
            "name" : {
              "fi" : "ahma",
              "sv" : "järv",
              "en" : "wolverine"
            },
            "categoryId" : 2,
            "multipleSpecimenAllowedOnHarvests" : false
          } ],
          "events" : [ {
            "name" : "ACCIDENT",
            "types" : [ "TRAFFIC_ACCIDENT", "RAILWAY_ACCIDENT", "OTHER" ],
            "results" : [ "ANIMAL_FOUND_DEAD", "ANIMAL_FOUND_AND_TERMINATED", "ANIMAL_FOUND_AND_NOT_TERMINATED", "ACCIDENT_SITE_NOT_FOUND", "ANIMAL_NOT_FOUND", "UNDUE_ALARM" ],
            "methods" : [ {
              "name" : "TRACED_WITH_DOG",
              "isChecked" : false
            }, {
              "name" : "TRACED_WITHOUT_DOG",
              "isChecked" : false
            }, {
              "name" : "OTHER",
              "isChecked" : false
            } ]
          }, {
            "name" : "DEPORTATION",
            "types" : [ "ANIMAL_NEAR_HOUSES_AREA", "ANIMAL_AT_FOOD_DESTINATION", "OTHER" ],
            "typeDetails" : {
              "ANIMAL_NEAR_HOUSES_AREA" : [ {
                "detailType" : "CARED_HOUSE_AREA",
                "speciesCodes" : null
              }, {
                "detailType" : "FARM_ANIMAL_BUILDING",
                "speciesCodes" : null
              }, {
                "detailType" : "URBAN_AREA",
                "speciesCodes" : null
              }, {
                "detailType" : "OTHER",
                "speciesCodes" : null
              } ],
              "ANIMAL_AT_FOOD_DESTINATION" : [ {
                "detailType" : "CARCASS_AT_FOREST",
                "speciesCodes" : null
              }, {
                "detailType" : "CARCASS_NEAR_HOUSES_AREA",
                "speciesCodes" : null
              }, {
                "detailType" : "GARBAGE_CAN",
                "speciesCodes" : null
              }, {
                "detailType" : "BEEHIVE",
                "speciesCodes" : [ 47348 ]
              }, {
                "detailType" : "OTHER",
                "speciesCodes" : null
              } ]
            },
            "results" : [ "ANIMAL_TERMINATED", "ANIMAL_DEPORTED", "ANIMAL_NOT_FOUND", "UNDUE_ALARM" ],
            "resultDetails" : {
              "ANIMAL_DEPORTED" : [ "ANIMAL_CONTACTED_AND_DEPORTED", "ANIMAL_CONTACTED", "UNCERTAIN_RESULT" ]
            },
            "methods" : [ {
              "name" : "DOG",
              "isChecked" : false
            }, {
              "name" : "PAIN_EQUIPMENT",
              "isChecked" : false
            }, {
              "name" : "SOUND_EQUIPMENT",
              "isChecked" : false
            }, {
              "name" : "VEHICLE",
              "isChecked" : false
            }, {
              "name" : "CHASING_WITH_PEOPLE",
              "isChecked" : false
            }, {
              "name" : "OTHER",
              "isChecked" : false
            } ]
          }, {
            "name" : "INJURED_ANIMAL",
            "types" : [ "INJURED_ANIMAL", "ANIMAL_ON_ICE", "OTHER" ],
            "results" : [ "ANIMAL_FOUND_DEAD", "ANIMAL_FOUND_AND_TERMINATED", "ANIMAL_FOUND_AND_NOT_TERMINATED", "ANIMAL_NOT_FOUND", "UNDUE_ALARM" ],
            "methods" : [ {
              "name" : "TRACED_WITH_DOG",
              "isChecked" : false
            }, {
              "name" : "TRACED_WITHOUT_DOG",
              "isChecked" : false
            }, {
              "name" : "OTHER",
              "isChecked" : false
            } ]
          }, {
            "name" : "SOME_FUTURE_CATEGORY",
            "types" : [ "TYPE_1", "TYPE_2" ],
            "results" : [ "RESULT_1", "RESULT_2" ],
            "methods" : [ {
              "name" : "METHOD_1",
              "isChecked" : false
            }, {
              "name" : "METHOD_2",
              "isChecked" : false
            } ]
          }]
        }
        """

}