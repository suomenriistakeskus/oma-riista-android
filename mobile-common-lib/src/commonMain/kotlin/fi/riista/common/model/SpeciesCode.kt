package fi.riista.common.model

typealias SpeciesCode = Int

@Suppress("SpellCheckingInspection")
object SpeciesCodes {
    // Mooselike animals
    const val FALLOW_DEER_ID: SpeciesCode = 47484               // Kuusipeura
    const val MOOSE_ID: SpeciesCode = 47503                     // Hirvi
    const val ROE_DEER_ID: SpeciesCode = 47507                  // Metsäkauris
    const val WHITE_TAILED_DEER_ID: SpeciesCode = 47629         // Valkohäntäpeura
    const val WILD_FOREST_DEER_ID: SpeciesCode = 200556         // Metsäpeura

    // Large carnivores
    const val BEAR_ID: SpeciesCode = 47348                      // Karhu
    const val LYNX_ID: SpeciesCode = 46615                      // Ilves
    const val WOLF_ID: SpeciesCode = 46549                      // Susi
    const val WOLVERINE_ID: SpeciesCode = 47212                 // Ahma

    // Birds
    const val BEAN_GOOSE_ID: SpeciesCode = 26287                // Metsähanhi
    const val COMMON_EIDER_ID: SpeciesCode = 26419              // Haahka
    const val COOT_ID: SpeciesCode = 27381                      // Nokikana
    const val GARGANEY_ID: SpeciesCode = 26388                  // Heinätavi
    const val GOOSANDER_ID: SpeciesCode = 26442                 // Isokoskelo
    const val GREYLAG_GOOSE_ID: SpeciesCode = 26291             // Merihanhi
    const val LONG_TAILED_DUCK_ID: SpeciesCode = 26427          // Alli
    const val PINTAIL_ID: SpeciesCode = 26382                   // Jouhisorsa
    const val POCHARD_ID: SpeciesCode = 26407                   // Punasotka
    const val RED_BREASTED_MERGANSER_ID: SpeciesCode = 26440    // Tukkakoskelo
    const val SHOVELER_ID: SpeciesCode = 26394                  // Lapasorsa
    const val TUFTED_DUCK_ID: SpeciesCode = 26415               // Tukkasotka
    const val WIGEON_ID: SpeciesCode = 26360                    // Haapana

    // Other mammals
    const val EUROPEAN_BEAVER_ID: SpeciesCode = 48251           // Euroopanmajava
    const val GREY_SEAL_ID: SpeciesCode = 47282                 // Halli eli harmaahylje
    const val MOUNTAIN_HARE_ID: SpeciesCode = 50106             // Metsäjänis
    const val OTTER_ID: SpeciesCode = 47169                     // Saukko
    const val POLECAT_ID: SpeciesCode = 47240                   // Hilleri
    const val RINGED_SEAL_ID: SpeciesCode = 200555              // Itämerennorppa
    const val WILD_BOAR_ID: SpeciesCode = 47926                 // Villisika
}

private val DEER_ANIMALS = listOf(
        SpeciesCodes.FALLOW_DEER_ID,
        SpeciesCodes.ROE_DEER_ID,
        SpeciesCodes.WHITE_TAILED_DEER_ID,
        SpeciesCodes.WILD_FOREST_DEER_ID
)

fun SpeciesCode.isMoose(): Boolean {
    return this == SpeciesCodes.MOOSE_ID
}

fun SpeciesCode.isDeer(): Boolean {
    return this in DEER_ANIMALS
}

fun SpeciesCode.isDeerOrMoose(): Boolean {
    return isDeer() || isMoose()
}
