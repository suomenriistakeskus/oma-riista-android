package fi.riista.mobile.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class AreaMap : Serializable {
    @JsonProperty("number")
    var number: String? = null

    @JsonProperty("name")
    var name: String? = null
}
