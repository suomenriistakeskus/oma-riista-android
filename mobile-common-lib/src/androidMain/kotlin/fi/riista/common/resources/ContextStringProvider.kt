package fi.riista.common.resources

import android.content.Context

/**
 * A [StringProvider] that gets the string from the specified [context].
 *
 * The common library [RStringId] is mapped to android string resource id by
 * the given [StringIdMapper]
 */
class ContextStringProvider(
    private val context: Context,
    private val stringIdMapper: StringIdMapper,
): StringProvider {
    override fun getString(stringId: RStringId): String {
        return context.getString(stringIdMapper.mapToResourceId(stringId))
    }

    override fun getFormattedString(stringId: RStringId, arg: String): String {
        return context.getString(stringIdMapper.mapToResourceId(stringId), arg)
    }
}