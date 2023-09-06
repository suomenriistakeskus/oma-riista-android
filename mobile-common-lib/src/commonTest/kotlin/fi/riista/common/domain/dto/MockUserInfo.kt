package fi.riista.common.domain.dto

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Suppress("SpellCheckingInspection")
object MockUserInfo {

    internal fun parse(userInfo: String, ignoreUnknownKeys: Boolean = true): UserInfoDTO =
        Json {
            this.ignoreUnknownKeys = ignoreUnknownKeys
        }.decodeFromString(userInfo)

    const val PenttiHunterNumber: HunterNumberDTO = "88888888"
    const val PenttiUsername = "user"
    const val Pentti =
        "{\n" +
        "  \"username\" : \"$PenttiUsername\",\n" +
        "  \"personId\" : 123,\n" +
        "  \"firstName\" : \"Pentti\",\n" +
        "  \"lastName\" : \"Mujunen\",\n" +
        "  \"birthDate\" : \"1911-11-11\",\n" +
        "  \"address\" : {\n" +
        "    \"id\" : 134,\n" +
        "    \"rev\" : 0,\n" +
        "    \"editable\" : false,\n" +
        "    \"streetAddress\" : \"Mujunenkatu\",\n" +
        "    \"postalCode\" : \"00004\",\n" +
        "    \"city\" : \"Mujunenkaupunki\",\n" +
        "    \"country\" : \"suomi\"\n" +
        "  },\n" +
        "  \"homeMunicipality\" : {\n" +
        "    \"fi\" : \"Nokia\",\n" +
        "    \"sv\" : \"Nokia\"\n" +
        "  },\n" +
        "  \"rhy\" : {\n" +
        "    \"id\" : 180,\n" +
        "    \"name\" : {\n" +
        "      \"fi\" : \"Nokian seudun riistanhoitoyhdistys\",\n" +
        "      \"sv\" : \"Nokianejdens jaktvårdsförening\"\n" +
        "    },\n" +
        "    \"officialCode\" : \"368\"\n" +
        "  },\n" +
        "  \"hunterNumber\" : \"$PenttiHunterNumber\",\n" +
        "  \"hunterExamDate\" : \"1990-01-01\",\n" +
        "  \"huntingCardStart\" : \"2019-08-01\",\n" +
        "  \"huntingCardEnd\" : \"2021-07-31\",\n" +
        "  \"huntingBanStart\" : \"2021-06-30\",\n" +
        "  \"huntingBanEnd\" : \"2021-06-31\",\n" +
        "  \"huntingCardValidNow\" : true,\n" +
        "  \"qrCode\" : \"Mujunen;Pentti;Nokia;11111911;88888888;31072021;368;MCECDwCpo49F5W1St20XOgJsWgIOPhpvIB9zKvHzdBKYsXQ=\",\n" +
        "  \"timestamp\" : \"2021-02-11T06:31:50.990Z\",\n" +
        "  \"gameDiaryYears\" : [ 2000, 2005, 2008, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 ],\n" +
        "  \"harvestYears\" : [ 2000, 2008, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 ],\n" +
        "  \"observationYears\" : [ 2005, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020 ],\n" +
        "  \"shootingTests\" : [ ],\n" +
        "  \"occupations\" : [ {\n" +
        "    \"id\" : 69,\n" +
        "    \"occupationType\" : \"AMPUMAKOKEEN_VASTAANOTTAJA\",\n" +
        "    \"name\" : {\n" +
        "      \"fi\" : \"Ampumakokeen vastaanottaja\",\n" +
        "      \"sv\" : \"Examinator för skjutprov\"\n" +
        "    },\n" +
        "    \"beginDate\" : null,\n" +
        "    \"endDate\" : null,\n" +
        "    \"organisation\" : {\n" +
        "      \"id\" : 184,\n" +
        "      \"name\" : {\n" +
        "        \"fi\" : \"Tampereen riistanhoitoyhdistys\",\n" +
        "        \"sv\" : \"Tammerfors jaktvårdsförening\"\n" +
        "      },\n" +
        "      \"officialCode\" : \"376\"\n" +
        "    }\n" +
        "  }, {\n" +
        "    \"id\" : 75,\n" +
        "    \"occupationType\" : \"AMPUMAKOKEEN_VASTAANOTTAJA\",\n" +
        "    \"name\" : {\n" +
        "      \"fi\" : \"Ampumakokeen vastaanottaja\",\n" +
        "      \"sv\" : \"Examinator för skjutprov\"\n" +
        "    },\n" +
        "    \"beginDate\" : \"2020-01-01\",\n" +
        "    \"endDate\" : \"2020-02-01\",\n" +
        "    \"organisation\" : {\n" +
        "      \"id\" : 255,\n" +
        "      \"name\" : {\n" +
        "        \"fi\" : \"Helsingin riistanhoitoyhdistys\",\n" +
        "        \"sv\" : \"Helsingfors jaktvårdsförening\"\n" +
        "      },\n" +
        "      \"officialCode\" : \"602\"\n" +
        "    }\n" +
        "  }, {\n" +
        "    \"id\" : 139,\n" +
        "    \"occupationType\" : \"AMPUMAKOKEEN_VASTAANOTTAJA\",\n" +
        "    \"name\" : {\n" +
        "      \"fi\" : \"Ampumakokeen vastaanottaja\",\n" +
        "      \"sv\" : \"Examinator för skjutprov\"\n" +
        "    },\n" +
        "    \"beginDate\" : null,\n" +
        "    \"endDate\" : null,\n" +
        "    \"organisation\" : {\n" +
        "      \"id\" : 177,\n" +
        "      \"name\" : {\n" +
        "        \"fi\" : \"Lempäälän seudun riistanhoitoyhdistys\",\n" +
        "        \"sv\" : \"Lempäälänejdens jaktvårdsförening\"\n" +
        "      },\n" +
        "      \"officialCode\" : \"362\"\n" +
        "    }\n" +
        "  } ],\n" +
        "  \"enableSrva\" : true,\n" +
        "  \"enableShootingTests\" : true,\n" +
        "  \"deerPilotUser\" : true\n" +
        "}"

    // Data where nullable fields are missing completely
    const val PenttiWithMissingData =
        "{\n" +
        "  \"username\" : \"user\",\n" +
        "  \"personId\" : 123,\n" +
        "  \"firstName\" : \"Pentti\",\n" +
        "  \"lastName\" : \"Mujunen\",\n" +
        "  \"homeMunicipality\" : {\n" +
        "    \"sv\" : \"Nokia\"\n" +
        "  },\n" +
        "  \"rhy\" : {\n" +
        "    \"id\" : 180,\n" +
        "    \"name\" : {\n" +
        "      \"fi\" : \"Nokian seudun riistanhoitoyhdistys\"\n" +
        "    },\n" +
        "    \"officialCode\" : \"368\"\n" +
        "  },\n" +
        "  \"huntingCardValidNow\" : true,\n" +
        "  \"timestamp\" : \"2021-02-11T06:31:50.990Z\",\n" +
        "  \"gameDiaryYears\" : [ 2000, 2005, 2008, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 ],\n" +
        "  \"harvestYears\" : [ 2000, 2008, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 ],\n" +
        "  \"observationYears\" : [ 2005, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020 ],\n" +
        "  \"shootingTests\" : [ ],\n" +
        "  \"occupations\" : [ ],\n" +
        "  \"enableSrva\" : true,\n" +
        "  \"enableShootingTests\" : true,\n" +
        "  \"deerPilotUser\" : true\n" +
        "}"
}
