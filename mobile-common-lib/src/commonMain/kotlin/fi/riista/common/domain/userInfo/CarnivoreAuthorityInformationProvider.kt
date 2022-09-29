package fi.riista.common.domain.userInfo

interface CarnivoreAuthorityInformationProvider {
    /**
     * Is the user carnivore authority?
     */
    val userIsCarnivoreAuthority: Boolean
}