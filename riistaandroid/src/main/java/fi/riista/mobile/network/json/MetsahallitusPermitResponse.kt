package fi.riista.mobile.network.json

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

class MetsahallitusPermitResponse {

    @JsonProperty
    lateinit var permitIdentifier: String

    @JsonProperty
    lateinit var permitType: Map<String, String>

    @JsonProperty
    lateinit var permitName: Map<String, String>

    @JsonProperty
    lateinit var areaNumber: String

    @JsonProperty
    lateinit var areaName: Map<String, String>

    @JsonProperty
    var beginDate: Date? = null

    @JsonProperty
    var endDate: Date? = null

    @JsonProperty
    var harvestFeedbackUrl: Map<String, String>? = null

}
