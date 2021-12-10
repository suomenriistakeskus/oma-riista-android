package fi.riista.mobile.utils

interface CredentialsStore {

    fun get(): Credentials?

    /**
     * Checks if login credential are stored in shared preferences.
     */
    fun isCredentialsSaved(): Boolean = get() != null

    /**
     * Saves credentials.
     */
    fun save(username: String?, password: String?)

    /**
     * Clears stored login credentials.
     */
    fun clear()

}
