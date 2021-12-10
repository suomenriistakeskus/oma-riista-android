package fi.riista.common.model


/**
 * An interface for containing common entity data.
 */
interface Entity {
    enum class Type {
        LOCAL,
        REMOTE,
        ;

        fun isLocal() = this == LOCAL
        fun isRemote() = this == REMOTE
    }

    val type: Type
}