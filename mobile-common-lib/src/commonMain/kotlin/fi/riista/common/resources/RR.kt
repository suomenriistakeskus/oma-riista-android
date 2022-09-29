package fi.riista.common.resources

/**
 * Similar to R on android but not generated. Also don't use R as that might be confusing
 * to app developer since this object is not internal.
 *
 * Also as a sidenote RR should be quite easy to use.
 * - RR.string.<some value> can easily be mapped to real string resource by just removing one 'R'
 *   from the beginning (assuming <some value> is same on RiistaSdk and on app)
 * - RR could stand for "Riista Resources"
 */
@Suppress("SpellCheckingInspection")
object RR {
    /**
     * Intentionally don't follow normal naming conventions. By not following the string ids
     * can be used almost identically as on android:
     *
     *  getString(RR.string.group_hunting_harvest_field_not_edible)
     *  vs
     *  getString(R.string.group_hunting_harvest_field_not_edible)
     */
    @Suppress("ClassName", "EnumEntryName") // keep it similar to android resources
    enum class string {
        generic_yes,
        generic_no,

        error_date_not_allowed,

        group_hunting_label_club,
        group_hunting_label_season,
        group_hunting_label_species,
        group_hunting_label_hunting_group,
        group_hunting_error_hunting_has_finished,

        group_hunting_error_time_not_within_hunting_day,

        group_hunting_harvest_field_hunting_day_and_time,
        group_hunting_harvest_field_actor,
        group_hunting_harvest_field_author,
        group_hunting_harvest_field_deer_hunting_type,
        group_hunting_harvest_field_deer_hunting_other_type_description,
        group_hunting_harvest_field_not_edible,
        group_hunting_harvest_field_weight_estimated,
        group_hunting_harvest_field_weight_measured,
        group_hunting_harvest_field_fitness_class,
        group_hunting_harvest_field_antlers_type,
        group_hunting_harvest_field_antlers_width,
        group_hunting_harvest_field_antler_points_left,
        group_hunting_harvest_field_antler_points_right,
        group_hunting_harvest_field_antlers_lost,
        group_hunting_harvest_field_antlers_girth,
        group_hunting_harvest_field_antler_shaft_width,
        group_hunting_harvest_field_antlers_length,
        group_hunting_harvest_field_antlers_inner_width,
        group_hunting_harvest_field_alone,
        group_hunting_harvest_field_additional_information,
        group_hunting_harvest_field_additional_information_instructions,
        group_hunting_harvest_field_additional_information_instructions_white_tailed_deer,

        observation_label_observation_category,
        observation_label_within_moose_hunting,
        observation_label_within_deer_hunting,
        observation_label_amount,
        observation_label_tassu_verified_by_carnivore_authority,
        observation_label_tassu_observer_name,
        observation_label_tassu_observer_phonenumber,
        observation_label_tassu_official_additional_information,
        observation_label_tassu_in_yard_distance_to_residence,
        observation_label_tassu_litter,
        observation_label_tassu_pack,
        observation_label_description,
        group_hunting_observation_field_hunting_day_and_time,
        group_hunting_observation_field_observation_type,
        group_hunting_observation_field_actor,
        group_hunting_observation_field_author,
        group_hunting_observation_field_headline_specimen_details,
        group_hunting_observation_field_mooselike_male_amount,
        group_hunting_observation_field_mooselike_female_amount,
        group_hunting_observation_field_mooselike_female_1calf_amount,
        group_hunting_observation_field_mooselike_female_2calf_amount,
        group_hunting_observation_field_mooselike_female_3calf_amount,
        group_hunting_observation_field_mooselike_female_4calf_amount,
        group_hunting_observation_field_mooselike_calf_amount,
        group_hunting_observation_field_mooselike_unknown_specimen_amount,
        group_hunting_observation_field_mooselike_male_amount_within_deer_hunting,
        group_hunting_observation_field_mooselike_female_amount_within_deer_hunting,
        group_hunting_observation_field_mooselike_female_1calf_amount_within_deer_hunting,
        group_hunting_observation_field_mooselike_female_2calf_amount_within_deer_hunting,
        group_hunting_observation_field_mooselike_female_3calf_amount_within_deer_hunting,
        group_hunting_observation_field_mooselike_female_4calf_amount_within_deer_hunting,
        group_hunting_observation_field_mooselike_calf_amount_within_deer_hunting,
        group_hunting_observation_field_mooselike_unknown_specimen_amount_within_deer_hunting,

        // Specimen
        specimen_label_width_of_paw,
        specimen_label_length_of_paw,
        specimen_label_state_of_health,
        specimen_label_marking,

        // Specimen state of health
        specimen_state_of_health_healthy,
        specimen_state_of_health_ill,
        specimen_state_of_health_wounded,
        specimen_state_of_health_carcass,
        specimen_state_of_health_dead,

        // Specimen marking
        specimen_marking_not_marked,
        specimen_marking_collar_or_radio_transmitter,
        specimen_marking_leg_ring_or_wing_tag,
        specimen_marking_earmark,

        // group hunting day
        group_hunting_day_label_start_date_and_time,
        group_hunting_day_label_end_date_and_time,
        group_hunting_day_label_number_of_hunters,
        group_hunting_day_label_hunting_method,
        group_hunting_day_label_number_of_hounds,
        group_hunting_day_label_snow_depth_centimeters,
        group_hunting_day_label_break_duration_minutes,

        group_hunting_day_no_breaks,

        group_hunting_day_error_dates_not_within_permit,

        group_hunting_message_no_hunting_days_but_can_create,
        group_hunting_message_no_hunting_days,
        group_hunting_message_no_hunting_days_deer,

        // hunting method of the group hunting day
        group_hunting_method_passilinja_koira_ohjaajineen_metsassa,
        group_hunting_method_hiipiminen_pysayttavalle_koiralle,
        group_hunting_method_passilinja_ja_tiivis_ajoketju,
        group_hunting_method_passilinja_ja_miesajo_jaljityksena,
        group_hunting_method_jaljitys_eli_naakiminen_ilman_passeja,
        group_hunting_method_vaijynta_kulkupaikoilla,
        group_hunting_method_vaijynta_ravintokohteilla,
        group_hunting_method_houkuttelu,
        group_hunting_method_muu,

        // process proposed group harvest
        group_hunting_proposed_group_harvest_specimen,
        group_hunting_proposed_group_harvest_shooter,
        group_hunting_proposed_group_harvest_actor,

        group_member_selection_select_hunter,
        group_member_selection_select_observer,
        group_member_selection_search_by_name,
        group_member_selection_name_hint,

        // actor selection
        group_hunting_hunter_id,
        group_hunting_enter_hunter_id,
        group_hunting_invalid_hunter_id,
        group_hunting_searching_hunter_by_id,
        group_hunting_searching_observer_by_id,
        group_hunting_hunter_search_failed,
        group_hunting_observer_search_failed,
        group_hunting_other_hunter,
        group_hunting_other_observer,

        // deer hunting types
        deer_hunting_type_stand_hunting,
        deer_hunting_type_dog_hunting,
        deer_hunting_type_other,

        // antler types
        harvest_antler_type_hanko,
        harvest_antler_type_lapio,
        harvest_antler_type_seka,

        // fitness class
        harvest_fitness_class_erinomainen,
        harvest_fitness_class_normaali,
        harvest_fitness_class_laiha,
        harvest_fitness_class_naantynyt,

        // Observation category
        observation_category_normal,
        observation_category_moose_hunting,
        observation_category_deer_hunting,

        // Observation type
        observation_type_nako,
        observation_type_jalki,
        observation_type_uloste,
        observation_type_aani,
        observation_type_riistakamera,
        observation_type_koiran_riistatyo,
        observation_type_maastolaskenta,
        observation_type_kolmiolaskenta,
        observation_type_lentolaskenta,
        observation_type_haaska,
        observation_type_syonnos,
        observation_type_kelomispuu,
        observation_type_kiimakuoppa,
        observation_type_makuupaikka,
        observation_type_pato,
        observation_type_pesa,
        observation_type_pesa_keko,
        observation_type_pesa_penkka,
        observation_type_pesa_seka,
        observation_type_soidin,
        observation_type_luolasto,
        observation_type_pesimaluoto,
        observation_type_lepailyluoto,
        observation_type_pesimasuo,
        observation_type_muuton_aikainen_lepailyalue,
        observation_type_riistankulkupaikka,
        observation_type_poikueymparisto,
        observation_type_vaihtelevarakenteinen_mustikkametsa,
        observation_type_kuusisekoitteinen_metsa,
        observation_type_vaihtelevarakenteinen_mantysekoitteinen_metsa,
        observation_type_vaihtelevarakenteinen_lehtipuusekoitteinen_metsa,
        observation_type_suon_reunametsa,
        observation_type_hakomamanty,
        observation_type_ruokailukoivikko,
        observation_type_leppakuusimetsa_tai_koivikuusimetsa,
        observation_type_ruokailupajukko_tai_koivikko,
        observation_type_muu,

        // Hunting club membership
        hunting_club_membership_invitations,
        hunting_club_memberships,

        // POI
        poi_location_group_type_sighting_place,
        poi_location_group_type_mineral_lick,
        poi_location_group_type_feeding_place,
        poi_location_group_type_other,

        // Hunting control
        hunting_control_start_time,
        hunting_control_end_time,
        hunting_control_duration,
        hunting_control_event_type,
        hunting_control_number_of_inspectors,
        hunting_control_cooperation_type,
        hunting_control_wolf_territory,
        hunting_control_inspectors,
        hunting_control_location_description,
        hunting_control_event_description,
        hunting_control_number_of_customers,
        hunting_control_number_of_proof_orders,
        hunting_control_date,
        hunting_control_other_participants,
        hunting_control_duration_zero,
        hunting_control_choose_inspector,
        hunting_control_choose_cooperation,
        hunting_control_inspector_selection_search_by_name,
        hunting_control_inspector_selection_name_hint,
        hunting_control_error_no_inspectors_for_selected_date,
        hunting_control_attachments,
        hunting_control_add_attachment,

        hunting_control_cooperation_type_poliisi,
        hunting_control_cooperation_type_rajavartiosto,
        hunting_control_cooperation_type_mh,
        hunting_control_cooperation_type_oma,

        hunting_control_event_type_mooselike,
        hunting_control_event_type_large_carnivore,
        hunting_control_event_type_grouse,
        hunting_control_event_type_waterfowl,
        hunting_control_event_type_dog_discipline,
        hunting_control_event_type_other,

        // srva
        srva_event_label_other_species_description,
        srva_event_label_approver,
        srva_event_label_rejector,
        srva_event_label_specimen_amount,
        srva_event_label_event_category,
        srva_event_label_deportation_order_number,
        srva_event_label_event_type,
        srva_event_label_other_event_type_description,
        srva_event_label_event_type_detail,
        srva_event_label_other_event_type_detail_description,
        srva_event_label_method,
        srva_event_label_other_method_description,
        srva_event_label_event_result,
        srva_event_label_event_result_detail,
        srva_event_label_person_count,
        srva_event_label_hours_spent,
        srva_event_label_description,

        // srva event category
        srva_event_category_accident,
        srva_event_category_deportation,
        srva_event_category_injured_animal,

        // srva event type
        srva_event_type_traffic_accident,
        srva_event_type_railway_accident,
        srva_event_type_animal_near_houses_area,
        srva_event_type_animal_at_food_destination,
        srva_event_type_injured_animal,
        srva_event_type_animal_on_ice,
        srva_event_type_other,

        // srva event type details
        srva_event_type_detail_cared_house_area,
        srva_event_type_detail_farm_animal_building,
        srva_event_type_detail_urban_area,
        srva_event_type_detail_carcass_at_forest,
        srva_event_type_detail_carcass_near_houses_area,
        srva_event_type_detail_garbage_can,
        srva_event_type_detail_beehive,
        srva_event_type_detail_other,

        // srva event results
        srva_event_result_animal_found_dead,
        srva_event_result_animal_found_and_terminated,
        srva_event_result_animal_found_and_not_terminated,
        srva_event_result_accident_site_not_found,
        srva_event_result_animal_not_found,
        srva_event_result_animal_terminated,
        srva_event_result_animal_deported,
        srva_event_result_undue_alarm,

        // srva event result details
        srva_event_result_detail_animal_contacted_and_deported,
        srva_event_result_detail_animal_contacted,
        srva_event_result_detail_uncertain_result,

        // srva methods
        srva_method_traced_with_dog,
        srva_method_traced_without_dog,
        srva_method_dog,
        srva_method_pain_equipment,
        srva_method_sound_equipment,
        srva_method_vehicle,
        srva_method_chasing_with_people,
        srva_method_other,

        other_species,
        unknown_species,

        gender_label,
        gender_female,
        gender_male,
        gender_unknown,

        age_label,
        age_adult,
        age_young,
        age_less_than_one_year,
        age_between_one_and_two_years,
        age_eraus, // for bear observations
        age_unknown,

        // Training
        training_type_lahi,
        training_type_sahkoinen,

        jht_training_occupation_type_metsastyksenvalvoja,
        jht_training_occupation_type_metsastajatutkinnon_vastaanottaja,
        jht_training_occupation_type_ampumakokeen_vastaanottaja,
        jht_training_occupation_type_rhyn_edustaja_riistavahinkojen_maastokatselmuksessa,
        occupation_training_occupation_type_petoyhdyshenkilo,
        ;
    }

    /**
     * Intentionally don't follow normal naming conventions. By not following the string ids
     * can be used almost identically as on android:
     *
     *  getString(RR.plurals.hours)
     *  vs
     *  getString(R.plurals.hours)
     */
    @Suppress("ClassName", "EnumEntryName") // keep it similar to android resources
    enum class stringFormat {
        // args expected:
        // 1. string - permit number
        group_hunting_label_permit_formatted,

        // args expected
        // 1. string - hours
        // 2. string - minutes
        //
        // should be in format "%s %s" (space should probably be NBSP)
        generic_hours_and_minutes_format,
    }

    /**
     * Intentionally don't follow normal naming conventions. By not following the string ids
     * can be used almost identically as on android:
     *
     *  getString(RR.plurals.hours)
     *  vs
     *  getString(R.plurals.hours)
     */
    @Suppress("ClassName", "EnumEntryName") // keep it similar to android resources
    enum class plurals {
        // expected format "%d hours"
        hours,
        // expected format "%d minutes"
        minutes,
    }
}
