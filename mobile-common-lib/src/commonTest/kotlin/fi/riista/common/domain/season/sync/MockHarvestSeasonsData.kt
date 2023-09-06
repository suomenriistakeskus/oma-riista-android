package fi.riista.common.domain.season.sync

object MockHarvestSeasonsData {

    // mock seasons are for hunting year 2021. Those need to be changed in tests!
    const val harvestSeasons =
    """
    [
        {
            "name": null,
            "gameSpeciesCode": 47503,
            "beginDate": "2022-06-01",
            "endDate": "2022-07-31"
        },
        {
            "name": null,
            "gameSpeciesCode": 27381,
            "beginDate": "2022-04-01",
            "endDate": "2022-04-21"
        }
    ]
    """

}
