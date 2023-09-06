package fi.riista.common.domain.permit.metsahallitusPermit.sync

object MockMetsahallitusPermitsData {

    const val permits =
    """
    [
        {
            "permitIdentifier" : "4949110101",
            "permitType" : {
               "fi" : "Pienpetokausilupa 1.8.2019-30.4.2020"
            },
            "permitName" : {
                "fi" : "Kausi",
                "sv" : "Säsongtillstånd",
                "en" : "Season license"
            },
            "areaNumber" : "1601",
            "areaName" : {
                "fi" : "Karigasniemi",
                "sv" : "Karigasniemi",
                "en" : "Karigasniemi"
            },
            "beginDate" : "2019-08-01",
            "endDate" : "2020-04-30",
            "harvestFeedbackUrl": {
                "fi": "<some url>"
            }
        }
    ]
    """

}
