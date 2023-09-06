package fi.riista.common.domain.huntingControl.ui.hunterInfo

object MockHunterInfoData {

    const val HunterNumber = "22222222"
    const val HunterInfo = """
    {
      "name" : "Pasi Puurtinen",
      "dateOfBirth" : "1911-11-11",
      "homeMunicipality" : {
        "fi" : "Nokia",
        "sv" : "Nokia",
        "en" : null
      },
      "hunterNumber" : "$HunterNumber",
      "huntingLicenseActive" : true,
      "huntingLicenseDateOfPayment" : "2022-06-28",
      "shootingTests" : [ {
        "rhyCode" : "376",
        "rhyName" : "Tampereen riistanhoitoyhdistys",
        "type" : "BEAR",
        "typeName" : "Karhu",
        "begin" : "2019-09-30",
        "end" : "2022-09-30",
        "expired" : true
      }, {
        "rhyCode" : "376",
        "rhyName" : "Tampereen riistanhoitoyhdistys",
        "type" : "MOOSE",
        "typeName" : "Hirvi / Peura",
        "begin" : "2019-05-13",
        "end" : "2022-05-13",
        "expired" : true
      }, {
        "rhyCode" : "376",
        "rhyName" : "Tampereen riistanhoitoyhdistys",
        "type" : "ROE_DEER",
        "typeName" : "Metsäkauris",
        "begin" : "2018-01-10",
        "end" : "2021-01-10",
        "expired" : true
      } ]
    }
    """
}
