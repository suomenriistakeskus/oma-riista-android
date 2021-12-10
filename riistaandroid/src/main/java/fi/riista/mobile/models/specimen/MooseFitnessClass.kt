package fi.riista.mobile.models.specimen

enum class MooseFitnessClass(private val value: String) {

    EXCELLENT("ERINOMAINEN"),
    NORMAL("NORMAALI"),
    THIN("LAIHA"),
    STARVED("NAANTYNYT");

    override fun toString(): String {
        return value
    }
}
