package fi.riista.common.ui

data class FunctionalityFlag(
    /**
     * Is the functionality available at all i.e. should the functionality UI be displayed?
     */
    val available: Boolean,

    /**
     * Is the functionality enabled i.e. should the functionality UI be enabled / disabled?
     */
    val enabled: Boolean
)