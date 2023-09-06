package fi.riista.common.ui.helpers

import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.isDeer
import fi.riista.common.domain.model.isMoose
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.StringProvider

internal class WeightFormatter(
    stringProvider: StringProvider
) {
    private val doubleFormatter = DoubleFormatter(stringProvider)

    internal fun formatWeight(weight: Double, species: Species): String {
        return when (val decimals = getDecimalCount(species)) {
            0 -> weight.formatWithZeroDecimals(doubleFormatter)
            1 -> weight.formatWithOneDecimal(doubleFormatter)
            else -> {
                logger.w { "Requested unsupported decimals ($decimals) for weight of species $species" }
                weight.formatWithOneDecimal(doubleFormatter)
            }
        }
    }

    companion object {
        internal fun getDecimalCount(species: Species): Int {
            return when {
                species.isDeer() || species.isMoose() -> 0
                else -> 1
            }
        }

        private val logger by getLogger(WeightFormatter::class)
    }
}

internal fun Double.formatWeight(formatter: WeightFormatter, species: Species): String {
    return formatter.formatWeight(this, species)
}