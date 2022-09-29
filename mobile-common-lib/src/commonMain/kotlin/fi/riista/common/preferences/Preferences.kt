package fi.riista.common.preferences

interface Preferences {
    fun contains(key: String): Boolean

    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String? = null): String?

    fun putInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int? = null): Int?

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean? = null): Boolean?

    fun remove(key: String)
}

expect class PlatformPreferences(): Preferences {
    override fun contains(key: String): Boolean
    override fun putString(key: String, value: String)
    override fun getString(key: String, defaultValue: String?): String?
    override fun putInt(key: String, value: Int)
    override fun getInt(key: String, defaultValue: Int?): Int?
    override fun putBoolean(key: String, value: Boolean)
    override fun getBoolean(key: String, defaultValue: Boolean?): Boolean?
    override fun remove(key: String)
}
