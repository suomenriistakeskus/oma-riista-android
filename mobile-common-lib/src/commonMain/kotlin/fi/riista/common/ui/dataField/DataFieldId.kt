package fi.riista.common.ui.dataField

/**
 * An id for typical use case with data fields. Probably an Enum that implements this interface.
 */
interface DataFieldId {
    /**
     * [DataField] ids must be representable as [Int]. This allows using stable ids in
     * RecyclerView.
     */
    fun toInt(): Int
}

/**
 * An id for data fields when there may be multiple items for same [type] e.g. headers, multiple
 * checkboxes which values are unified into one field. Useful also when querying
 * same values for multiple entries (e.g. specimens data input).
 */
abstract class IndexedDataFieldId<E : Enum<E>> : DataFieldId {
    abstract val type: E
    abstract val index: Int

    fun validateIndex() {
        require(index in 0 until MAX_INDEX) {
            "index not between 0..${MAX_INDEX} for type $type"
        }
    }

    override fun toInt(): Int {
        validateIndex()
        return MAX_INDEX * (type.ordinal + 1) + index
    }

    companion object {
        private const val MAX_INDEX = 1000

        internal inline fun <reified E : Enum<E>, reified FieldType : IndexedDataFieldId<E>> toIndexedField(
            value: Int,
            fieldFactory: (E, Int) -> FieldType,
        ): FieldType? {
            val ordinal = (value / MAX_INDEX) - 1
            val index = value.mod(MAX_INDEX)
            return enumValues<E>().getOrNull(ordinal)?.let { type ->
                fieldFactory(type, index)
            }
        }
    }
}
