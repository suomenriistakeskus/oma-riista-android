package fi.riista.common.model

@kotlinx.serialization.Serializable
data class EntitiesByYearMonth<EntityType>(
    val yearMonth: YearMonth,
    val entities: List<EntityType>
)

internal fun <EntityType> List<EntityType>.groupByYearMonth(
    yearMonthAccessor: (EntityType) -> YearMonth,
): List<EntitiesByYearMonth<EntityType>> {
    val entitiesByYearMonth: MutableMap<YearMonth, MutableList<EntityType>> = mutableMapOf()

    this.forEach { entity ->
        val yearMonth = yearMonthAccessor(entity)
        entitiesByYearMonth.getOrPut(yearMonth) { mutableListOf() }.add(entity)
    }

    return entitiesByYearMonth
        .map {
            EntitiesByYearMonth(yearMonth = it.key, entities = it.value)
        }.sortedByDescending {
            it.yearMonth
        }
}
