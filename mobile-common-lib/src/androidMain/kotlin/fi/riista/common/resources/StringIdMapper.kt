package fi.riista.common.resources

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * Maps the common library [RR.string] to android string resource id.
 *
 * The common library doesn't know application resource ids and thus this must
 * be implemented on the application side.
 *
 * Also see [ContextStringProvider] for usage.
 */
interface StringIdMapper {
    @StringRes
    fun mapToStringResourceId(stringId: RR.string): Int

    @StringRes
    fun mapToStringFormatResourceId(stringFormatId: RR.stringFormat): Int

    @PluralsRes
    fun mapToPluralsResourceId(pluralsId: RR.plurals): Int
}
