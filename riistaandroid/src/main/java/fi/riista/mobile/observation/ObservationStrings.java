package fi.riista.mobile.observation;

import java.util.HashMap;

import android.content.Context;
import fi.riista.mobile.R;
import fi.riista.mobile.models.Specimen.MooseAntlersType;
import fi.riista.mobile.models.Specimen.MooseFitnessClass;

public class ObservationStrings {

    private static HashMap<String, Integer> sMapping;

    static {
        sMapping = new HashMap<String, Integer>();

        sMapping.put("UNKNOWN", R.string.observation_unknown);

        //Observation types
        sMapping.put("NAKO", R.string.type_sight);
        sMapping.put("JALKI", R.string.type_track);
        sMapping.put("ULOSTE", R.string.type_excrement);
        sMapping.put("AANI", R.string.type_sound);
        sMapping.put("RIISTAKAMERA", R.string.type_game_camera);
        sMapping.put("KOIRAN_RIISTATYO", R.string.type_dog);
        sMapping.put("MAASTOLASKENTA", R.string.type_ground_count);
        sMapping.put("KOLMIOLASKENTA", R.string.type_triangulation_count);
        sMapping.put("LENTOLASKENTA", R.string.type_air_count);
        sMapping.put("HAASKA", R.string.type_carcass);
        sMapping.put("SYONNOS", R.string.type_feeding);
        sMapping.put("KELOMISPUU", R.string.type_kelomispuu);
        sMapping.put("KIIMAKUOPPA", R.string.type_kiimakuoppa);
        sMapping.put("MAKUUPAIKKA", R.string.type_laying_location);
        sMapping.put("PESA", R.string.type_nest);
        sMapping.put("PESA_KEKO", R.string.type_nest_mound);
        sMapping.put("PESA_PENKKA", R.string.type_nest_bank);
        sMapping.put("PESA_SEKA", R.string.type_nest_mixed);
        sMapping.put("PATO", R.string.type_dam);
        sMapping.put("SOIDIN", R.string.type_soidin);
        sMapping.put("LUOLASTO", R.string.type_caves);
        sMapping.put("PESIMALUOTO", R.string.type_nesting_islet);
        sMapping.put("LEPAILYLUOTO", R.string.type_resting_islet);
        sMapping.put("PESIMASUO", R.string.type_nesting_swamp);
        sMapping.put("MUUTON_AIKAINEN_LEPAILYALUE", R.string.type_migration_resting_area);
        sMapping.put("RIISTANKULKUPAIKKA", R.string.type_game_path);
        sMapping.put("MUU", R.string.type_other);

        //Age
        sMapping.put("ADULT", R.string.age_adult);
        sMapping.put("LT1Y", R.string.age_less_than_year);
        sMapping.put("_1TO2Y", R.string.age_year_or_two);
        sMapping.put("ERAUS", R.string.age_eraus);

        //State
        sMapping.put("HEALTHY", R.string.state_healthy);
        sMapping.put("ILL", R.string.state_ill);
        sMapping.put("WOUNDED", R.string.state_wounded);
        sMapping.put("CARCASS", R.string.state_carcass);
        sMapping.put("DEAD", R.string.state_dead);

        //Marking
        sMapping.put("NOT_MARKED", R.string.marked_none);
        sMapping.put("COLLAR_OR_RADIO_TRANSMITTER", R.string.marked_collar);
        sMapping.put("LEG_RING_OR_WING_TAG", R.string.marked_ring);
        sMapping.put("EARMARK", R.string.marked_ear);

        //Moose fitness class
        sMapping.put(MooseFitnessClass.EXCELLENT.toString(), R.string.moose_fitness_class_excellent);
        sMapping.put(MooseFitnessClass.NORMAL.toString(), R.string.moose_fitness_class_normal);
        sMapping.put(MooseFitnessClass.THIN.toString(), R.string.moose_fitness_class_thin);
        sMapping.put(MooseFitnessClass.STARVED.toString(), R.string.moose_fitness_class_starved);

        //Moose antlers type
        sMapping.put(MooseAntlersType.HANKO.toString(), R.string.moose_antlers_type_hanko);
        sMapping.put(MooseAntlersType.LAPIO.toString(), R.string.moose_antlers_type_lapio);
        sMapping.put(MooseAntlersType.SEKA.toString(), R.string.moose_antlers_type_seka);

        //SRVA
        sMapping.put("ACCIDENT", R.string.srva_accident);
        sMapping.put("DEPORTATION", R.string.srva_deportation);
        sMapping.put("INJURED_ANIMAL", R.string.srva_sick_animal);

        sMapping.put("TRAFFIC_ACCIDENT", R.string.srva_traffic_accident);
        sMapping.put("RAILWAY_ACCIDENT", R.string.srva_railway_accident);
        sMapping.put("OTHER", R.string.srva_other);

        sMapping.put("ANIMAL_NEAR_HOUSES_AREA", R.string.srva_animal_near_houses);
        sMapping.put("ANIMAL_AT_FOOD_DESTINATION", R.string.srva_animal_at_food_destination);

        sMapping.put("INJURED_ANIMAL", R.string.srva_injured_animal);
        sMapping.put("ANIMAL_ON_ICE", R.string.srva_animal_on_ice);

        sMapping.put("ANIMAL_FOUND_DEAD", R.string.srva_animal_found_dead);
        sMapping.put("ANIMAL_FOUND_AND_TERMINATED", R.string.srva_animal_found_and_terminated);
        sMapping.put("ANIMAL_FOUND_AND_NOT_TERMINATED", R.string.srva_animal_found_and_not_terminated);
        sMapping.put("ACCIDENT_SITE_NOT_FOUND", R.string.srva_accident_site_not_found);
        sMapping.put("ANIMAL_NOT_FOUND", R.string.srva_animal_not_found);
        sMapping.put("UNDUE_ALARM", R.string.srva_undue_alarm);

        sMapping.put("ANIMAL_TERMINATED", R.string.srva_animal_terminated);
        sMapping.put("ANIMAL_DEPORTED", R.string.srva_animal_deported);

        sMapping.put("DOG", R.string.srva_method_dog);
        sMapping.put("TRACED_WITH_DOG", R.string.srva_method_traced_with_dog);
        sMapping.put("TRACED_WITHOUT_DOG", R.string.srva_method_traced_without_dog);
        sMapping.put("PAIN_EQUIPMENT", R.string.srva_method_pain_equipment);
        sMapping.put("SOUND_EQUIPMENT", R.string.srva_method_sound_equipment);
    }

    public static String get(Context context, String key) {
        Integer value = sMapping.get(key);
        if (value != null) {
            return context.getString(value);
        }
        return null;
    }
}
