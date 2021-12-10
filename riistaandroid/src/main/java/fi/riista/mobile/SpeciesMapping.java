package fi.riista.mobile;

import fi.riista.mobile.utils.UnmodifiableSparseIntArray;

/**
 * This mapping speeds up image lookup - getIdentifier calls are too heavy to be done constantly
 * Generated with bash command:
 * for f in species_*.jpg; do set -- $f; a=$(echo $f | sed -e s/[^0-9]//g); echo species.put\($a, R.drawable.species_$a\)\;; done
 */
public class SpeciesMapping {
    public static final UnmodifiableSparseIntArray species;

    static {
        species = new UnmodifiableSparseIntArray();
        species.put(200535, R.drawable.species_200535);
        species.put(200555, R.drawable.species_200555);
        species.put(200556, R.drawable.species_200556);
        species.put(26287, R.drawable.species_26287);
        species.put(26291, R.drawable.species_26291);
        species.put(26298, R.drawable.species_26298);
        species.put(26360, R.drawable.species_26360);
        species.put(26366, R.drawable.species_26366);
        species.put(26373, R.drawable.species_26373);
        species.put(26382, R.drawable.species_26382);
        species.put(26388, R.drawable.species_26388);
        species.put(26394, R.drawable.species_26394);
        species.put(26407, R.drawable.species_26407);
        species.put(26415, R.drawable.species_26415);
        species.put(26419, R.drawable.species_26419);
        species.put(26427, R.drawable.species_26427);
        species.put(26435, R.drawable.species_26435);
        species.put(26440, R.drawable.species_26440);
        species.put(26442, R.drawable.species_26442);
        species.put(26921, R.drawable.species_26921);
        species.put(26922, R.drawable.species_26922);
        species.put(26926, R.drawable.species_26926);
        species.put(26928, R.drawable.species_26928);
        species.put(26931, R.drawable.species_26931);
        species.put(27048, R.drawable.species_27048);
        species.put(27152, R.drawable.species_27152);
        species.put(27381, R.drawable.species_27381);
        species.put(27649, R.drawable.species_27649);
        species.put(27750, R.drawable.species_27750);
        species.put(27759, R.drawable.species_27759);
        species.put(27911, R.drawable.species_27911);
        species.put(33117, R.drawable.species_33117);
        species.put(37122, R.drawable.species_37122);
        species.put(37142, R.drawable.species_37142);
        species.put(37166, R.drawable.species_37166);
        species.put(37178, R.drawable.species_37178);
        species.put(46542, R.drawable.species_46542);
        species.put(46549, R.drawable.species_46549);
        species.put(46564, R.drawable.species_46564);
        species.put(46587, R.drawable.species_46587);
        species.put(46615, R.drawable.species_46615);
        species.put(47169, R.drawable.species_47169);
        species.put(47180, R.drawable.species_47180);
        species.put(47212, R.drawable.species_47212);
        species.put(47223, R.drawable.species_47223);
        species.put(47230, R.drawable.species_47230);
        species.put(47240, R.drawable.species_47240);
        species.put(47243, R.drawable.species_47243);
        species.put(47282, R.drawable.species_47282);
        species.put(47305, R.drawable.species_47305);
        species.put(47329, R.drawable.species_47329);
        species.put(47348, R.drawable.species_47348);
        species.put(47476, R.drawable.species_47476);
        species.put(47479, R.drawable.species_47479);
        species.put(47484, R.drawable.species_47484);
        species.put(47503, R.drawable.species_47503);
        species.put(47507, R.drawable.species_47507);
        species.put(47629, R.drawable.species_47629);
        species.put(47774, R.drawable.species_47774);
        species.put(47926, R.drawable.species_47926);
        species.put(48089, R.drawable.species_48089);
        species.put(48250, R.drawable.species_48250);
        species.put(48251, R.drawable.species_48251);
        species.put(48537, R.drawable.species_48537);
        species.put(50106, R.drawable.species_50106);
        species.put(50114, R.drawable.species_50114);
        species.put(50336, R.drawable.species_50336);
        species.put(50386, R.drawable.species_50386);
        species.put(53004, R.drawable.species_53004);
        species.lock();
    }
}
