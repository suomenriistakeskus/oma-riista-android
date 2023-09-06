package fi.riista.common.domain.observation.metadata

import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.observation.metadata.dto.toObservationMetadata
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.util.deserializeFromJson

internal object HardcodedObservationMetadataProvider {
    val metadata: ObservationMetadata by lazy {
        metadataJson
            .deserializeFromJson<ObservationMetadataDTO>()!!
            .toObservationMetadata()
    }

    internal val metadataJson =
        """
        {
          "lastModified": "2023-03-15T17:55:22.948+0200",
          "speciesList": [
            {
              "gameSpeciesCode": 26287,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESIMASUO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26291,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26298,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26360,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26366,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26373,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26382,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26388,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26394,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26407,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26415,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26419,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26427,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26435,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SOIDIN",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26440,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26442,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUUTON_AIKAINEN_LEPAILYALUE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26921,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SOIDIN",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUEYMPARISTO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KUUSISEKOTTEINEN_METSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SUON_REUNAMETSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RUOKAILUPAJUKKO_TAI_KOIVIKKO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26922,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SOIDIN",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26926,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SOIDIN",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUEYMPARISTO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RUOKAILUKOIVIKKO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26928,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SOIDIN",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUEYMPARISTO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "VAIHTELEVARAKENTEINEN_MANTYSEKOTTEINEN_METSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "HAKOMAMANTY",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 26931,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SOIDIN",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUEYMPARISTO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LEPPAKUUSIMETSA_TAI_KOIVUKUUSIMETSA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 27048,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 27152,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 27381,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 27649,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SOIDIN",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 27750,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 27759,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 27911,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 33117,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 37122,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 37142,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 37166,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 37178,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 46542,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 46549,
              "baseFields": {
                "withinMooseHunting": "YES"
              },
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "widthOfPaw": "VOLUNTARY",
                    "lengthOfPaw": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "widthOfPaw": "VOLUNTARY",
                    "lengthOfPaw": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "HAASKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LUOLASTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ],
              "minWidthOfPaw": 4.0,
              "maxWidthOfPaw": 15.0,
              "minLengthOfPaw": 4.0,
              "maxLengthOfPaw": 15.0
            },
            {
              "gameSpeciesCode": 46564,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LUOLASTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 46587,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LUOLASTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 46615,
              "baseFields": {
                "withinMooseHunting": "YES"
              },
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "widthOfPaw": "VOLUNTARY",
                    "lengthOfPaw": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "widthOfPaw": "VOLUNTARY",
                    "lengthOfPaw": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "HAASKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ],
              "minWidthOfPaw": 4.0,
              "maxWidthOfPaw": 15.0,
              "minLengthOfPaw": 4.0,
              "maxLengthOfPaw": 15.0
            },
            {
              "gameSpeciesCode": 47169,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47180,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LUOLASTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47212,
              "baseFields": {
                "withinMooseHunting": "YES"
              },
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "widthOfPaw": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "widthOfPaw": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "HAASKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LUOLASTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ],
              "minWidthOfPaw": 4.0,
              "maxWidthOfPaw": 15.0
            },
            {
              "gameSpeciesCode": 47223,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47230,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47240,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47243,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47282,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESIMALUOTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LEPAILYLUOTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47305,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESIMALUOTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LEPAILYLUOTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47329,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47348,
              "baseFields": {
                "withinMooseHunting": "YES"
              },
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "widthOfPaw": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "widthOfPaw": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "_1TO2Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "CARCASS",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "HAASKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ],
              "minWidthOfPaw": 4.0,
              "maxWidthOfPaw": 20.0
            },
            {
              "gameSpeciesCode": 47476,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KELOMISPUU",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KIIMAKUOPPA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47479,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KELOMISPUU",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KIIMAKUOPPA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47484,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KELOMISPUU",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KIIMAKUOPPA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47503,
              "baseFields": {
                "withinMooseHunting": "YES"
              },
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "MOOSE_HUNTING",
                  "type": "NAKO",
                  "baseFields": {
                    "mooselikeMaleAmount": "YES",
                    "mooselikeFemaleAmount": "YES",
                    "mooselikeCalfAmount": "YES",
                    "mooselikeFemale1CalfAmount": "YES",
                    "mooselikeFemale2CalfsAmount": "YES",
                    "mooselikeFemale3CalfsAmount": "YES",
                    "mooselikeUnknownSpecimenAmount": "YES"
                  },
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KELOMISPUU",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KIIMAKUOPPA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47507,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KELOMISPUU",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KIIMAKUOPPA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47629,
              "baseFields": {
                "withinDeerHunting": "YES"
              },
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "DEER_HUNTING",
                  "type": "NAKO",
                  "baseFields": {
                    "mooselikeMaleAmount": "YES",
                    "mooselikeFemaleAmount": "YES",
                    "mooselikeCalfAmount": "YES",
                    "mooselikeFemale1CalfAmount": "YES",
                    "mooselikeFemale2CalfsAmount": "YES",
                    "mooselikeFemale3CalfsAmount": "YES",
                    "mooselikeFemale4CalfsAmount": "YES",
                    "mooselikeUnknownSpecimenAmount": "YES",
                    "deerHuntingType": "YES",
                    "deerHuntingTypeDescription": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KELOMISPUU",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KIIMAKUOPPA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47774,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 47926,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 48089,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 48250,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PATO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA_KEKO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA_PENKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA_SEKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 48251,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PATO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA_KEKO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA_PENKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA_SEKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 48537,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 50106,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 50114,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LUOLASTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 50336,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 50386,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 53004,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 200535,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "POIKUE",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PARI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "YES",
                    "gender": "YES"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "LEG_RING_OR_WING_TAG"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {}
                }
              ]
            },
            {
              "gameSpeciesCode": 200555,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "PESIMALUOTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "LEPAILYLUOTO",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            },
            {
              "gameSpeciesCode": 200556,
              "baseFields": {},
              "specimenFields": {},
              "contextSensitiveFieldSets": [
                {
                  "category": "NORMAL",
                  "type": "NAKO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "JALKI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "ULOSTE",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "AANI",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTAKAMERA",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "KOIRAN_RIISTATYO",
                  "baseFields": {
                    "amount": "YES"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY",
                    "state": "VOLUNTARY",
                    "marking": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ],
                  "allowedStates": [
                    "HEALTHY",
                    "ILL",
                    "WOUNDED",
                    "DEAD"
                  ],
                  "allowedMarkings": [
                    "NOT_MARKED",
                    "COLLAR_OR_RADIO_TRANSMITTER",
                    "EARMARK"
                  ]
                },
                {
                  "category": "NORMAL",
                  "type": "SYONNOS",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KELOMISPUU",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "KIIMAKUOPPA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MAKUUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "RIISTANKULKUPAIKKA",
                  "baseFields": {},
                  "specimenFields": {}
                },
                {
                  "category": "NORMAL",
                  "type": "MUU",
                  "baseFields": {
                    "amount": "VOLUNTARY"
                  },
                  "specimenFields": {
                    "age": "VOLUNTARY",
                    "gender": "VOLUNTARY"
                  },
                  "allowedAges": [
                    "ADULT",
                    "LT1Y",
                    "UNKNOWN"
                  ]
                }
              ]
            }
          ],
          "observationSpecVersion": 5
        }
        """.trimIndent()
}
