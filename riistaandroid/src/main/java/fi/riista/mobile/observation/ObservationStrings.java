package fi.riista.mobile.observation;

import android.content.Context;

import java.util.HashMap;

import fi.riista.mobile.R;
import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.observation.ObservationType;
import fi.riista.mobile.models.specimen.MooseAntlersType;
import fi.riista.mobile.models.specimen.MooseFitnessClass;

public class ObservationStrings {

    private static final HashMap<String, Integer> sMapping;

    static {
        sMapping = new HashMap<>();

        sMapping.put("UNKNOWN", R.string.observation_unknown);

        // Observation types
        sMapping.put(ObservationType.NAKO.name(), R.string.type_sight);
        sMapping.put(ObservationType.POIKUE.name(), R.string.type_poikue);
        sMapping.put(ObservationType.PARI.name(), R.string.type_pari);
        sMapping.put(ObservationType.JALKI.name(), R.string.type_track);
        sMapping.put(ObservationType.ULOSTE.name(), R.string.type_excrement);
        sMapping.put(ObservationType.AANI.name(), R.string.type_sound);
        sMapping.put(ObservationType.RIISTAKAMERA.name(), R.string.type_game_camera);
        sMapping.put(ObservationType.KOIRAN_RIISTATYO.name(), R.string.type_dog);
        sMapping.put(ObservationType.MAASTOLASKENTA.name(), R.string.type_ground_count);
        sMapping.put(ObservationType.KOLMIOLASKENTA.name(), R.string.type_triangulation_count);
        sMapping.put(ObservationType.LENTOLASKENTA.name(), R.string.type_air_count);
        sMapping.put(ObservationType.HAASKA.name(), R.string.type_carcass);
        sMapping.put(ObservationType.SYONNOS.name(), R.string.type_feeding);
        sMapping.put(ObservationType.KELOMISPUU.name(), R.string.type_kelomispuu);
        sMapping.put(ObservationType.KIIMAKUOPPA.name(), R.string.type_kiimakuoppa);
        sMapping.put(ObservationType.MAKUUPAIKKA.name(), R.string.type_laying_location);
        sMapping.put(ObservationType.PESA.name(), R.string.type_nest);
        sMapping.put(ObservationType.PESA_KEKO.name(), R.string.type_nest_mound);
        sMapping.put(ObservationType.PESA_PENKKA.name(), R.string.type_nest_bank);
        sMapping.put(ObservationType.PESA_SEKA.name(), R.string.type_nest_mixed);
        sMapping.put(ObservationType.PATO.name(), R.string.type_dam);
        sMapping.put(ObservationType.SOIDIN.name(), R.string.type_soidin);
        sMapping.put(ObservationType.LUOLASTO.name(), R.string.type_caves);
        sMapping.put(ObservationType.PESIMALUOTO.name(), R.string.type_nesting_islet);
        sMapping.put(ObservationType.LEPAILYLUOTO.name(), R.string.type_resting_islet);
        sMapping.put(ObservationType.PESIMASUO.name(), R.string.type_nesting_swamp);
        sMapping.put(ObservationType.MUUTON_AIKAINEN_LEPAILYALUE.name(), R.string.type_migration_resting_area);
        sMapping.put(ObservationType.RIISTANKULKUPAIKKA.name(), R.string.type_game_path);
        sMapping.put(ObservationType.POIKUEYMPARISTO.name(), R.string.type_poikueymparisto);
        sMapping.put(ObservationType.VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA.name(),
                R.string.type_vaihtelevarakenteinen_mustikkametsa);
        sMapping.put(ObservationType.VAIHTELEVARAKENTEINEN_MANTYSEKOTTEINEN_METSA.name(),
                R.string.type_vaihtelevarakenteinen_mantysekotteinen_metsa);
        sMapping.put(ObservationType.HAKOMAMANTY.name(), R.string.type_hakomamanty);
        sMapping.put(ObservationType.VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA.name(),
                R.string.type_vaihtelevarakenteinen_lehtipuusekotteinen_metsa);
        sMapping.put(ObservationType.RUOKAILUKOIVIKKO.name(), R.string.type_ruokailukoivikko);
        sMapping.put(ObservationType.LEPPAKUUSIMETSA_TAI_KOIVUKUUSIMETSA.name(),
                R.string.type_leppakuusimetsa_tai_koivukuusimetsa);
        sMapping.put(ObservationType.KUUSISEKOTTEINEN_METSA.name(), R.string.type_kuusisekotteinen_metsa);
        sMapping.put(ObservationType.SUON_REUNAMETSA.name(), R.string.type_suon_reunametsa);
        sMapping.put(ObservationType.RUOKAILUPAJUKKO_TAI_KOIVIKKO.name(), R.string.type_ruokailupajukko_tai_koivikko);
        sMapping.put(ObservationType.MUU.name(), R.string.type_other);

        // Deer hunting types
        sMapping.put(DeerHuntingType.STAND_HUNTING.name(), R.string.deer_hunting_type_standing);
        sMapping.put(DeerHuntingType.DOG_HUNTING.name(), R.string.deer_hunting_type_dog);
        sMapping.put(DeerHuntingType.OTHER.name(), R.string.deer_hunting_type_other);

        // Age
        sMapping.put("ADULT", R.string.age_adult);
        sMapping.put("LT1Y", R.string.age_less_than_year);
        sMapping.put("_1TO2Y", R.string.age_year_or_two);
        sMapping.put("ERAUS", R.string.age_eraus);

        // State
        sMapping.put("HEALTHY", R.string.state_healthy);
        sMapping.put("ILL", R.string.state_ill);
        sMapping.put("WOUNDED", R.string.state_wounded);
        sMapping.put("CARCASS", R.string.state_carcass);
        sMapping.put("DEAD", R.string.state_dead);

        // Marking
        sMapping.put("NOT_MARKED", R.string.marked_none);
        sMapping.put("COLLAR_OR_RADIO_TRANSMITTER", R.string.marked_collar);
        sMapping.put("LEG_RING_OR_WING_TAG", R.string.marked_ring);
        sMapping.put("EARMARK", R.string.marked_ear);

        // Moose fitness class
        sMapping.put(MooseFitnessClass.EXCELLENT.name(), R.string.fitness_class_excellent);
        sMapping.put(MooseFitnessClass.NORMAL.name(), R.string.fitness_class_normal);
        sMapping.put(MooseFitnessClass.THIN.name(), R.string.fitness_class_thin);
        sMapping.put(MooseFitnessClass.STARVED.name(), R.string.fitness_class_starved);

        // Moose antlers type
        sMapping.put(MooseAntlersType.HANKO.name(), R.string.antlers_type_hanko);
        sMapping.put(MooseAntlersType.LAPIO.name(), R.string.antlers_type_lapio);
        sMapping.put(MooseAntlersType.SEKA.name(), R.string.antlers_type_seka);

        // SRVA
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

        // Harvest
        sMapping.put(GreySealHuntingMethod.SHOT.name(), R.string.harvest_hunting_type_shot);
        sMapping.put(GreySealHuntingMethod.CAPTURED_ALIVE.name(), R.string.harvest_hunting_type_captured_alive);
        sMapping.put(GreySealHuntingMethod.SHOT_BUT_LOST.name(), R.string.harvest_hunting_type_shot_but_lost);
    }

    public static String get(Context context, String key) {
        Integer value = sMapping.get(key);
        if (value != null) {
            return context.getString(value);
        }
        return null;
    }
}
