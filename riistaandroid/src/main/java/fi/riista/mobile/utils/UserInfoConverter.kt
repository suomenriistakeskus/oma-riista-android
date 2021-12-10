package fi.riista.mobile.utils

import com.fasterxml.jackson.databind.ObjectMapper
import fi.riista.mobile.models.user.UserInfo
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInfoConverter @Inject constructor(private val objectMapper: ObjectMapper) {

    fun fromJson(json: String): UserInfo? {
        return try {
            objectMapper.readValue(json, objectMapper.typeFactory.constructType(UserInfo::class.java))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
   }
}
