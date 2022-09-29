package fi.riista.common.ui.dataField

import kotlinx.serialization.Serializable

@Serializable
data class FieldRequirement(
    /**
     * The field requirement type.
     */
    val type: Type,

    /**
     * Should the requirement status be displayed?
     *
     * This flag is useful, when field is technically required (e.g. for validation purposes) but
     * requirement should not be displayed to the user.
     */
    val indicateRequirement: Boolean,
) {
    enum class Type {
        REQUIRED,
        VOLUNTARY,

        /**
         * The requirement status for the field that don't have requirement status. These are
         * for example labels, errors, captions etc.
         */
        NONE,
        ;
    }

    /**
     * Is the field required and should the requirement status be indicated?
     */
    fun isVisiblyRequired(): Boolean {
        return isRequired() && indicateRequirement
    }

    /**
     * Is the field required?
     */
    fun isRequired(): Boolean {
        return type == Type.REQUIRED
    }

    companion object {
        fun required() = FieldRequirement(Type.REQUIRED, indicateRequirement = true)
        fun voluntary() = FieldRequirement(Type.VOLUNTARY, indicateRequirement = true)
        fun noRequirement() = FieldRequirement(Type.NONE, indicateRequirement = true)
    }
}


@Serializable
data class FieldSpecification<FieldId : DataFieldId>(
    val fieldId: FieldId,
    val requirementStatus: FieldRequirement,
)

fun <FieldId : DataFieldId> List<FieldSpecification<FieldId>>.contains(fieldId: FieldId): Boolean {
    return indexOfFirst { it.fieldId == fieldId } >= 0
}

fun <FieldId : DataFieldId> FieldId.noRequirement(): FieldSpecification<FieldId> {
    return FieldSpecification(this, FieldRequirement(FieldRequirement.Type.NONE, indicateRequirement = false))
}

fun <FieldId : DataFieldId> FieldId.required(indicateRequirementStatus: Boolean = true): FieldSpecification<FieldId> {
    return FieldSpecification(this, FieldRequirement(FieldRequirement.Type.REQUIRED, indicateRequirementStatus))
}

fun <FieldId : DataFieldId> FieldId.voluntary(indicateRequirementStatus: Boolean = true): FieldSpecification<FieldId> {
    return FieldSpecification(this, FieldRequirement(FieldRequirement.Type.VOLUNTARY, indicateRequirementStatus))
}

fun <FieldId : DataFieldId> FieldId.withRequirement(
    provideRequirement: () -> FieldRequirement,
): FieldSpecification<FieldId> {
    return FieldSpecification(this, provideRequirement())
}


class FieldSpecificationListBuilder<FieldId: DataFieldId> {
    private val fieldSpecifications = mutableListOf<FieldSpecification<FieldId>>()

    fun conditionally(
        condition: Boolean,
        block: FieldSpecificationListBuilder<FieldId>.() -> Unit
    ): FieldSpecificationListBuilder<FieldId> {
        if (condition) {
            this.block()
        }
        return this
    }

    /**
     * Adds given [fields].
     *
     * Only non-null fields are added i.e. nulls are filtered out. This allows using `.takeIf { }`
     * construction e.g.
     *   .add(Field.FIRST,
     *        Field.SECOND.takeIf { shouldAddSecond() },
     *        Field.THIRD)
     */
    fun add(vararg fields: FieldSpecification<FieldId>?): FieldSpecificationListBuilder<FieldId> {
        this.fieldSpecifications.addAll(fields.filterNotNull())
        return this
    }

    fun toList(): List<FieldSpecification<FieldId>> = fieldSpecifications
}
