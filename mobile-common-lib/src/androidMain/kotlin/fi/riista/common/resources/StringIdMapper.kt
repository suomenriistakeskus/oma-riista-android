package fi.riista.common.resources

import androidx.annotation.StringRes

/**
 * Maps the common library [RStringId] to android string resource id.
 *
 * The common library doesn't know application resource ids and thus this must
 * be implemented on the application side.
 *
 * Also see [ContextStringProvider] for usage.
 */
interface StringIdMapper {
    @StringRes
    fun mapToResourceId(stringId: RStringId): Int
}
