package fi.riista.common.poi

object MockPoiData {
    const val PoiLocationGroups =
        """
        [
            {
                "id": 1,
                "rev": 4,
                "visibleId": 1,
                "clubId": null,
                "description": "Passi1",
                "type": "SIGHTING_PLACE",
                "lastModifiedDate": null,
                "lastModifierName": null,
                "lastModifierRiistakeskus": false,
                "locations": [
                    {
                        "id": 2,
                        "poiId": 1,
                        "description": "Toka passipaikka",
                        "visibleId": 2,
                        "geoLocation": {
                            "latitude": 6825040,
                            "longitude": 313212,
                            "source": "MANUAL",
                            "accuracy": 0.0,
                            "altitude": null,
                            "altitudeAccuracy": null
                        }
                    },
                    {
                        "id": 1,
                        "poiId": 1,
                        "description": "Eka passipaikka",
                        "visibleId": 1,
                        "geoLocation": {
                            "latitude": 6826107,
                            "longitude": 312227,
                            "source": "MANUAL",
                            "accuracy": 0.0,
                            "altitude": null,
                            "altitudeAccuracy": null
                        }
                    }
                ]
            },
            {
                "id": 2,
                "rev": 1,
                "visibleId": 2,
                "clubId": null,
                "description": "Ruokintapaikat",
                "type": "FEEDING_PLACE",
                "lastModifiedDate": null,
                "lastModifierName": null,
                "lastModifierRiistakeskus": false,
                "locations": [
                    {
                        "id": 3,
                        "poiId": 2,
                        "description": "Eka ruokintapaikka",
                        "visibleId": 1,
                        "geoLocation": {
                            "latitude": 6826789,
                            "longitude": 313128,
                            "source": "MANUAL",
                            "accuracy": 0.0,
                            "altitude": null,
                            "altitudeAccuracy": null
                        }
                    }
                ]
            },
            {
                "id": 4,
                "rev": 1,
                "visibleId": 3,
                "clubId": null,
                "description": "Toinen passiketju",
                "type": "SIGHTING_PLACE",
                "lastModifiedDate": null,
                "lastModifierName": null,
                "lastModifierRiistakeskus": false,
                "locations": [
                    {
                        "id": 8,
                        "poiId": 4,
                        "description": "Ensimmäinen passipaikka",
                        "visibleId": 1,
                        "geoLocation": {
                            "latitude": 6826750,
                            "longitude": 311909,
                            "source": "MANUAL",
                            "accuracy": 0.0,
                            "altitude": null,
                            "altitudeAccuracy": null
                        }
                    },
                    {
                        "id": 9,
                        "poiId": 4,
                        "description": "Toinen passipaikka",
                        "visibleId": 2,
                        "geoLocation": {
                            "latitude": 6826980,
                            "longitude": 312645,
                            "source": "MANUAL",
                            "accuracy": 0.0,
                            "altitude": null,
                            "altitudeAccuracy": null
                        }
                    },
                    {
                        "id": 10,
                        "poiId": 4,
                        "description": "Kolmas passipaikka",
                        "visibleId": 3,
                        "geoLocation": {
                            "latitude": 6826868,
                            "longitude": 313370,
                            "source": "MANUAL",
                            "accuracy": 0.0,
                            "altitude": null,
                            "altitudeAccuracy": null
                        }
                    }
                ]
            }
        ]
        """

    const val EmptyPoiLocationGroups = "[]"

}
