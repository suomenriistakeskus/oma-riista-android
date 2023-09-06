package fi.riista.mobile.riistaSdkHelpers

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import fi.riista.common.resources.ContextStringProvider
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringIdMapper
import fi.riista.mobile.R


/**
 * A factory that is able to create [ContextStringProvider]s based on given [Context].
 *
 * Also specifies the mapping from [RR.string] to android string resource ids
 * (i.e. implements [StringIdMapper]).
 */
object ContextStringProviderFactory {
    private val stringIdMapper = AppStringIdMapper()

    fun createForContext(context: Context): ContextStringProvider {
        return ContextStringProvider(context, stringIdMapper)
    }
}

private class AppStringIdMapper : StringIdMapper {
    @StringRes
    override fun mapToStringResourceId(stringId: RR.string): Int {
        return when (stringId) {
            RR.string.generic_yes -> R.string.yes
            RR.string.generic_no -> R.string.no

            RR.string.error_date_not_allowed -> R.string.error_date_not_allowed
            RR.string.error_datetime_in_future -> R.string.error_datetime_in_future
            RR.string.group_hunting_label_club -> R.string.group_hunting_club
            RR.string.group_hunting_label_season -> R.string.group_hunting_season
            RR.string.group_hunting_label_species -> R.string.group_hunting_species
            RR.string.group_hunting_label_hunting_group -> R.string.group_hunting_group
            RR.string.group_hunting_error_hunting_has_finished -> R.string.group_hunting_error_hunting_has_finished

            RR.string.group_hunting_error_time_not_within_hunting_day ->
                R.string.group_hunting_error_time_not_within_hunting_day

            RR.string.harvest_label_hunting_day_and_time ->
                R.string.group_hunting_harvest_field_hunting_day_and_time
            RR.string.harvest_label_actor -> R.string.group_hunting_harvest_field_actor
            RR.string.harvest_label_author -> R.string.group_hunting_harvest_field_author

            // group hunting day
            RR.string.group_hunting_day_label_start_date_and_time ->
                R.string.group_hunting_entries_on_map_start_date
            RR.string.group_hunting_day_label_end_date_and_time ->
                R.string.group_hunting_entries_on_map_end_date
            RR.string.group_hunting_day_label_number_of_hunters ->
                R.string.group_hunting_day_label_number_of_hunters
            RR.string.group_hunting_day_label_hunting_method ->
                R.string.group_hunting_day_label_hunting_method
            RR.string.group_hunting_day_label_number_of_hounds ->
                R.string.group_hunting_day_label_number_of_hounds
            RR.string.group_hunting_day_label_snow_depth_centimeters ->
                R.string.group_hunting_day_label_snow_depth
            RR.string.group_hunting_day_label_break_duration_minutes ->
                R.string.group_hunting_day_label_break_duration

            RR.string.group_hunting_day_no_breaks ->
                R.string.group_hunting_day_no_breaks

            RR.string.group_hunting_day_error_dates_not_within_permit ->
                R.string.group_hunting_day_error_dates_not_within_permit
            RR.string.group_hunting_message_no_hunting_days_but_can_create ->
                R.string.group_hunting_no_hunting_days_but_can_create
            RR.string.group_hunting_message_no_hunting_days ->
                R.string.group_hunting_no_hunting_days
            RR.string.group_hunting_message_no_hunting_days_deer ->
                R.string.group_hunting_no_hunting_days_deer

            // hunting method of the group hunting day
            RR.string.group_hunting_method_passilinja_koira_ohjaajineen_metsassa ->
                R.string.group_hunting_method_passilinja_koira_ohjaajineen_metsassa
            RR.string.group_hunting_method_hiipiminen_pysayttavalle_koiralle ->
                R.string.group_hunting_method_hiipiminen_pysayttavalle_koiralle
            RR.string.group_hunting_method_passilinja_ja_tiivis_ajoketju ->
                R.string.group_hunting_method_passilinja_ja_tiivis_ajoketju
            RR.string.group_hunting_method_passilinja_ja_miesajo_jaljityksena ->
                R.string.group_hunting_method_passilinja_ja_miesajo_jaljityksena
            RR.string.group_hunting_method_jaljitys_eli_naakiminen_ilman_passeja ->
                R.string.group_hunting_method_jaljitys_eli_naakiminen_ilman_passeja
            RR.string.group_hunting_method_vaijynta_kulkupaikoilla ->
                R.string.group_hunting_method_vaijynta_kulkupaikoilla
            RR.string.group_hunting_method_vaijynta_ravintokohteilla ->
                R.string.group_hunting_method_vaijynta_ravintokohteilla
            RR.string.group_hunting_method_houkuttelu ->
                R.string.group_hunting_method_houkuttelu
            RR.string.group_hunting_method_muu ->
                R.string.group_hunting_method_muu

            // process proposed group harvest
            RR.string.harvest_label_select_permit ->
                R.string.permit_selection_checkbox_text
            RR.string.harvest_label_permit_information ->
                R.string.harvest_permit_information_label
            RR.string.harvest_label_permit_required ->
                R.string.harvest_permit_number_required
            RR.string.harvest_label_wild_boar_feeding_place ->
                R.string.harvest_feeding_place_title
            RR.string.harvest_label_grey_seal_hunting_method ->
                R.string.harvest_hunting_type_title
            RR.string.harvest_label_is_taiga_bean_goose ->
                R.string.harvest_taiga_bean_goose_title
            RR.string.harvest_label_amount ->
                R.string.harvest_amount
            RR.string.harvest_label_description ->
                R.string.description
            RR.string.group_hunting_proposed_group_harvest_shooter ->
                R.string.group_hunting_proposed_group_harvest_shooter
            RR.string.group_hunting_proposed_group_harvest_specimen ->
                R.string.group_hunting_proposed_group_harvest_specimen
            RR.string.group_hunting_proposed_group_harvest_actor ->
                R.string.group_hunting_proposed_group_harvest_actor
            RR.string.harvest_label_deer_hunting_type ->
                R.string.deer_hunting_type
            RR.string.harvest_label_deer_hunting_other_type_description ->
                R.string.deer_hunting_type_description
            RR.string.harvest_label_not_edible ->
                R.string.not_edible
            RR.string.harvest_label_weight ->
                R.string.weight_title
            RR.string.harvest_label_weight_estimated ->
                R.string.weight_estimated
            RR.string.harvest_label_weight_measured ->
                R.string.weight_measured
            RR.string.harvest_label_fitness_class ->
                R.string.fitness_class
            RR.string.harvest_label_antlers_type ->
                R.string.antlers_type
            RR.string.harvest_label_antlers_width ->
                R.string.antlers_width
            RR.string.harvest_label_antler_points_left ->
                R.string.antlers_points_left
            RR.string.harvest_label_antler_points_right ->
                R.string.antlers_points_right
            RR.string.harvest_label_antlers_lost ->
                R.string.antlers_lost
            RR.string.harvest_label_antlers_girth ->
                R.string.antlers_girth
            RR.string.harvest_label_antler_shaft_width ->
                R.string.antler_shaft_width
            RR.string.harvest_label_antlers_length ->
                R.string.antlers_length
            RR.string.harvest_label_antlers_inner_width ->
                R.string.antlers_inner_width
            RR.string.harvest_label_alone ->
                R.string.mooselike_calf
            RR.string.harvest_label_additional_information ->
                R.string.additional_info
            RR.string.harvest_label_additional_information_instructions ->
                R.string.additional_info_instructions
            RR.string.harvest_label_additional_information_instructions_white_tailed_deer ->
                R.string.additional_info_instructions_white_tailed_deer
            RR.string.harvest_label_own_harvest -> R.string.harvest_label_own_harvest

            RR.string.harvest_label_hunting_club ->
                R.string.harvest_label_hunting_club
            RR.string.harvest_label_hunting_club_official_code ->
                R.string.harvest_label_hunting_club_official_code

            RR.string.hunting_club_selection_select_club ->
                R.string.hunting_club_selection_select_club
            RR.string.hunting_club_selection_search_by_name ->
                R.string.hunting_club_selection_search_by_name
            RR.string.hunting_club_selection_search_by_name_hint ->
                R.string.hunting_club_selection_search_by_name_hint
            RR.string.hunting_club_selection_no_club_selection ->
                R.string.hunting_club_selection_no_club_selection
            RR.string.hunting_club_selection_other_club ->
                R.string.hunting_club_selection_other_club

            RR.string.hunting_club_search_enter_club_official_code ->
                R.string.hunting_club_search_enter_club_official_code
            RR.string.hunting_club_search_invalid_official_code ->
                R.string.hunting_club_search_invalid_official_code
            RR.string.hunting_club_search_searching_by_official_code ->
                R.string.hunting_club_search_searching_by_official_code
            RR.string.hunting_club_search_search_failed ->
                R.string.hunting_club_search_search_failed

            RR.string.harvest_settings_add_harvest_for_other_hunter ->
                R.string.harvest_settings_add_harvest_for_other_hunter
            RR.string.harvest_settings_add_harvest_for_other_hunter_explanation ->
                R.string.harvest_settings_add_harvest_for_other_hunter_explanation
            RR.string.harvest_settings_enable_club_selection ->
                R.string.harvest_settings_enable_club_selection
            RR.string.harvest_settings_enable_club_selection_explanation ->
                R.string.harvest_settings_enable_club_selection_explanation

            // actor selection
            RR.string.group_hunting_hunter_id -> R.string.group_hunting_hunter_id
            RR.string.group_hunting_enter_hunter_id -> R.string.group_hunting_enter_hunter_id
            RR.string.group_hunting_invalid_hunter_id -> R.string.group_hunting_invalid_hunter_id
            RR.string.group_hunting_searching_hunter_by_id -> R.string.group_hunting_searching_hunter_by_id
            RR.string.group_hunting_searching_observer_by_id -> R.string.group_hunting_searching_observer_by_id
            RR.string.group_hunting_hunter_search_failed -> R.string.group_hunting_hunter_search_failed
            RR.string.group_hunting_observer_search_failed -> R.string.group_hunting_observer_search_failed
            RR.string.group_hunting_other_hunter -> R.string.group_hunting_other_hunter
            RR.string.group_hunting_other_observer -> R.string.group_hunting_other_observer

            RR.string.group_member_selection_select_hunter ->
                R.string.group_member_selection_select_hunter
            RR.string.group_member_selection_select_observer ->
                R.string.group_member_selection_select_observer
            RR.string.group_member_selection_search_by_name ->
                R.string.group_member_selection_search_name
            RR.string.group_member_selection_name_hint ->
                R.string.group_member_selection_enter_name

            // observation
            RR.string.group_hunting_observation_field_hunting_day_and_time ->
                R.string.group_hunting_observation_field_hunting_day_and_time
            RR.string.group_hunting_observation_field_observation_type ->
                R.string.observation_type
            RR.string.group_hunting_observation_field_actor ->
                R.string.group_hunting_observation_field_actor
            RR.string.group_hunting_observation_field_author ->
                R.string.group_hunting_harvest_field_author
            RR.string.group_hunting_observation_field_headline_specimen_details ->
                R.string.specimen_details
            RR.string.group_hunting_observation_field_mooselike_male_amount ->
                R.string.mooselike_male
            RR.string.group_hunting_observation_field_mooselike_female_amount ->
                R.string.mooselike_female
            RR.string.group_hunting_observation_field_mooselike_female_1calf_amount ->
                R.string.mooselike_female_calf
            RR.string.group_hunting_observation_field_mooselike_female_2calf_amount ->
                R.string.mooselike_female_calfs2
            RR.string.group_hunting_observation_field_mooselike_female_3calf_amount ->
                R.string.mooselike_female_calfs3
            RR.string.group_hunting_observation_field_mooselike_female_4calf_amount ->
                R.string.mooselike_female_calfs4
            RR.string.group_hunting_observation_field_mooselike_calf_amount ->
                R.string.mooselike_calf
            RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount ->
                R.string.mooselike_unknown
            RR.string.group_hunting_observation_field_mooselike_male_amount_within_deer_hunting ->
                R.string.mooselike_male_within_deer_hunting
            RR.string.group_hunting_observation_field_mooselike_female_amount_within_deer_hunting ->
                R.string.mooselike_female_within_deer_hunting
            RR.string.group_hunting_observation_field_mooselike_female_1calf_amount_within_deer_hunting ->
                R.string.mooselike_female_calf_within_deer_hunting
            RR.string.group_hunting_observation_field_mooselike_female_2calf_amount_within_deer_hunting ->
                R.string.mooselike_female_calfs2_within_deer_hunting
            RR.string.group_hunting_observation_field_mooselike_female_3calf_amount_within_deer_hunting ->
                R.string.mooselike_female_calfs3_within_deer_hunting
            RR.string.group_hunting_observation_field_mooselike_female_4calf_amount_within_deer_hunting ->
                R.string.mooselike_female_calfs4_within_deer_hunting
            RR.string.group_hunting_observation_field_mooselike_calf_amount_within_deer_hunting ->
                R.string.mooselike_calf_within_deer_hunting
            RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount_within_deer_hunting ->
                R.string.mooselike_unknown_within_deer_hunting

            // Observation labels
            RR.string.observation_label_observation_category -> R.string.observation_category
            RR.string.observation_label_within_moose_hunting -> R.string.within_moose_hunting
            RR.string.observation_label_within_deer_hunting -> R.string.within_deer_hunting
            RR.string.observation_label_amount -> R.string.harvest_amount
            RR.string.observation_label_tassu_verified_by_carnivore_authority -> R.string.tassu_verified_by_carnivore_authority
            RR.string.observation_label_tassu_observer_name -> R.string.tassu_observer_name
            RR.string.observation_label_tassu_observer_phonenumber -> R.string.tassu_observer_phone_number
            RR.string.observation_label_tassu_official_additional_information -> R.string.tassu_official_additional_info
            RR.string.observation_label_tassu_in_yard_distance_to_residence -> R.string.tassu_distance_to_residence
            RR.string.observation_label_tassu_litter -> R.string.tassu_litter
            RR.string.observation_label_tassu_pack -> R.string.tassu_pack
            RR.string.observation_label_description -> R.string.description

            // Observation category
            RR.string.observation_category_normal -> R.string.observation_category_normal
            RR.string.observation_category_moose_hunting -> R.string.observation_category_within_moose_hunting
            RR.string.observation_category_deer_hunting -> R.string.observation_category_within_deer_hunting

            // Specimen labels
            RR.string.specimen_label_width_of_paw -> R.string.tassu_paw_width
            RR.string.specimen_label_length_of_paw -> R.string.tassu_paw_length
            RR.string.specimen_label_state_of_health -> R.string.observation_state
            RR.string.specimen_label_marking -> R.string.observation_marked

            // state of health
            RR.string.specimen_state_of_health_healthy -> R.string.state_healthy
            RR.string.specimen_state_of_health_ill -> R.string.state_ill
            RR.string.specimen_state_of_health_wounded -> R.string.state_wounded
            RR.string.specimen_state_of_health_carcass -> R.string.state_carcass
            RR.string.specimen_state_of_health_dead -> R.string.state_dead

            // marking
            RR.string.specimen_marking_not_marked -> R.string.marked_none
            RR.string.specimen_marking_collar_or_radio_transmitter -> R.string.marked_collar
            RR.string.specimen_marking_leg_ring_or_wing_tag -> R.string.marked_ring
            RR.string.specimen_marking_earmark -> R.string.marked_ear

            // deer hunting types
            RR.string.deer_hunting_type_stand_hunting -> R.string.deer_hunting_type_standing
            RR.string.deer_hunting_type_dog_hunting -> R.string.deer_hunting_type_dog
            RR.string.deer_hunting_type_other -> R.string.deer_hunting_type_other

            // antler types
            RR.string.harvest_antler_type_hanko -> R.string.antlers_type_hanko
            RR.string.harvest_antler_type_lapio -> R.string.antlers_type_lapio
            RR.string.harvest_antler_type_seka -> R.string.antlers_type_seka

            // fitness class
            RR.string.harvest_fitness_class_erinomainen -> R.string.fitness_class_excellent
            RR.string.harvest_fitness_class_normaali -> R.string.fitness_class_normal
            RR.string.harvest_fitness_class_laiha -> R.string.fitness_class_thin
            RR.string.harvest_fitness_class_naantynyt -> R.string.fitness_class_starved

            // grey seal hunting method
            RR.string.grey_seal_hunting_method_shot -> R.string.harvest_hunting_type_shot
            RR.string.grey_seal_hunting_method_captured_alive -> R.string.harvest_hunting_type_captured_alive
            RR.string.grey_seal_hunting_method_shot_but_lost -> R.string.harvest_hunting_type_shot_but_lost

            // harvest report state
            RR.string.harvest_report_required -> R.string.harvest_create_report
            RR.string.harvest_report_state_sent_for_approval -> R.string.harvest_sent_for_approval
            RR.string.harvest_report_state_approved -> R.string.harvest_approved
            RR.string.harvest_report_state_rejected -> R.string.harvest_rejected

            // harvest state accepted to permit
            RR.string.harvest_permit_proposed -> R.string.harvest_permit_proposed
            RR.string.harvest_permit_accepted -> R.string.harvest_permit_accepted
            RR.string.harvest_permit_rejected -> R.string.harvest_permit_rejected

            // observation type
            RR.string.observation_type_nako -> R.string.type_sight
            RR.string.observation_type_poikue -> R.string.type_poikue
            RR.string.observation_type_pari -> R.string.type_pari
            RR.string.observation_type_jalki -> R.string.type_track
            RR.string.observation_type_uloste -> R.string.type_excrement
            RR.string.observation_type_aani -> R.string.type_sound
            RR.string.observation_type_riistakamera -> R.string.type_game_camera
            RR.string.observation_type_koiran_riistatyo -> R.string.type_dog
            RR.string.observation_type_maastolaskenta -> R.string.type_ground_count
            RR.string.observation_type_kolmiolaskenta -> R.string.type_triangulation_count
            RR.string.observation_type_lentolaskenta -> R.string.type_air_count
            RR.string.observation_type_haaska -> R.string.type_carcass
            RR.string.observation_type_syonnos -> R.string.type_feeding
            RR.string.observation_type_kelomispuu -> R.string.type_kelomispuu
            RR.string.observation_type_kiimakuoppa -> R.string.type_kiimakuoppa
            RR.string.observation_type_makuupaikka -> R.string.type_laying_location
            RR.string.observation_type_pato -> R.string.type_dam
            RR.string.observation_type_pesa -> R.string.type_nest
            RR.string.observation_type_pesa_keko -> R.string.type_nest_mound
            RR.string.observation_type_pesa_penkka -> R.string.type_nest_bank
            RR.string.observation_type_pesa_seka -> R.string.type_nest_mixed
            RR.string.observation_type_soidin -> R.string.type_soidin
            RR.string.observation_type_luolasto -> R.string.type_caves
            RR.string.observation_type_pesimaluoto -> R.string.type_nesting_islet
            RR.string.observation_type_lepailyluoto -> R.string.type_resting_islet
            RR.string.observation_type_pesimasuo -> R.string.type_nesting_swamp
            RR.string.observation_type_muuton_aikainen_lepailyalue -> R.string.type_migration_resting_area
            RR.string.observation_type_riistankulkupaikka -> R.string.type_game_path
            RR.string.observation_type_poikueymparisto -> R.string.type_poikueymparisto
            RR.string.observation_type_vaihtelevarakenteinen_mustikkametsa ->
                R.string.type_vaihtelevarakenteinen_mustikkametsa
            RR.string.observation_type_kuusisekoitteinen_metsa -> R.string.type_kuusisekotteinen_metsa
            RR.string.observation_type_vaihtelevarakenteinen_mantysekoitteinen_metsa ->
                R.string.type_vaihtelevarakenteinen_mantysekotteinen_metsa
            RR.string.observation_type_vaihtelevarakenteinen_lehtipuusekoitteinen_metsa ->
                R.string.type_vaihtelevarakenteinen_lehtipuusekotteinen_metsa
            RR.string.observation_type_suon_reunametsa -> R.string.type_suon_reunametsa
            RR.string.observation_type_hakomamanty -> R.string.type_hakomamanty
            RR.string.observation_type_ruokailukoivikko -> R.string.type_ruokailukoivikko
            RR.string.observation_type_leppakuusimetsa_tai_koivikuusimetsa ->
                R.string.type_leppakuusimetsa_tai_koivukuusimetsa
            RR.string.observation_type_ruokailupajukko_tai_koivikko -> R.string.type_ruokailupajukko_tai_koivikko
            RR.string.observation_type_muu -> R.string.type_other

            RR.string.error_observation_specimen_amount_at_least_two -> R.string.error_observation_specimen_amount_at_least_two


            RR.string.hunting_club_membership_invitations -> R.string.my_details_hunting_club_membership_invitations
            RR.string.hunting_club_memberships -> R.string.my_details_hunting_club_memberships

            RR.string.training_type_sahkoinen -> R.string.training_type_sahkoinen
            RR.string.training_type_lahi -> R.string.training_type_lahi

            RR.string.jht_training_occupation_type_metsastyksenvalvoja -> R.string.occupation_type_metsastyksenvalvoja
            RR.string.jht_training_occupation_type_metsastajatutkinnon_vastaanottaja -> R.string. occupation_type_metsastajatutkinnon_vastaanottaja
            RR.string.jht_training_occupation_type_ampumakokeen_vastaanottaja -> R.string.occupation_type_ampumakokeen_vastaanottaja
            RR.string.jht_training_occupation_type_rhyn_edustaja_riistavahinkojen_maastokatselmuksessa -> R.string.occupation_type_rhyn_edustaja_riistavahinkojen_maastokatselmuksessa
            RR.string.occupation_training_occupation_type_petoyhdyshenkilo -> R.string.occupation_type_petoyhdyshenkilo

            RR.string.poi_location_group_type_sighting_place -> R.string.poi_location_group_type_sighting_place
            RR.string.poi_location_group_type_mineral_lick -> R.string.poi_location_group_type_mineral_lick
            RR.string.poi_location_group_type_feeding_place -> R.string.poi_location_group_type_feeding_place
            RR.string.poi_location_group_type_other -> R.string.poi_location_group_type_other

            RR.string.hunting_control_start_time -> R.string.hunting_control_start_time
            RR.string.hunting_control_end_time -> R.string.hunting_control_end_time
            RR.string.hunting_control_duration -> R.string.hunting_control_duration
            RR.string.hunting_control_event_type -> R.string.hunting_control_event_type
            RR.string.hunting_control_number_of_inspectors -> R.string.hunting_control_number_of_inspectors
            RR.string.hunting_control_cooperation_type -> R.string.hunting_control_cooperation_type
            RR.string.hunting_control_wolf_territory -> R.string.hunting_control_wolf_territory
            RR.string.hunting_control_inspectors -> R.string.hunting_control_inspectors
            RR.string.hunting_control_location_description -> R.string.hunting_control_location_description
            RR.string.hunting_control_event_description -> R.string.hunting_control_event_description
            RR.string.hunting_control_number_of_customers -> R.string.hunting_control_number_of_customers
            RR.string.hunting_control_number_of_proof_orders -> R.string.hunting_control_number_of_proof_orders
            RR.string.hunting_control_date -> R.string.hunting_control_date
            RR.string.hunting_control_other_participants -> R.string.hunting_control_other_participants
            RR.string.hunting_control_duration_zero -> R.string.hunting_control_duration_zero
            RR.string.hunting_control_choose_inspector -> R.string.hunting_control_choose_inspectors
            RR.string.hunting_control_choose_cooperation -> R.string.hunting_control_choose_cooperation
            RR.string.hunting_control_attachments -> R.string.hunting_control_attachments
            RR.string.hunting_control_inspector_selection_search_by_name -> R.string.hunting_control_inspector_selection_search_by_name
            RR.string.hunting_control_inspector_selection_name_hint -> R.string.hunting_control_inspector_selection_name_hint
            RR.string.hunting_control_error_no_inspectors_for_selected_date -> R.string.hunting_control_error_no_inspectors_for_selected_date
            RR.string.hunting_control_error_no_self_as_inspector -> R.string.hunting_control_error_no_self_as_inspector
            RR.string.hunting_control_add_attachment -> R.string.hunting_control_add_attachment

            RR.string.hunting_control_cooperation_type_poliisi -> R.string.hunting_control_cooperation_type_poliisi
            RR.string.hunting_control_cooperation_type_rajavartiosto -> R.string.hunting_control_cooperation_type_rajavartiosto
            RR.string.hunting_control_cooperation_type_mh -> R.string.hunting_control_cooperation_type_mh
            RR.string.hunting_control_cooperation_type_oma -> R.string.hunting_control_cooperation_type_oma

            RR.string.hunting_control_event_type_mooselike -> R.string.hunting_control_event_type_mooselike
            RR.string.hunting_control_event_type_large_carnivore -> R.string.hunting_control_event_type_large_carnivore
            RR.string.hunting_control_event_type_grouse -> R.string.hunting_control_event_type_grouse
            RR.string.hunting_control_event_type_waterfowl -> R.string.hunting_control_event_type_waterfowl
            RR.string.hunting_control_event_type_dog_discipline -> R.string.hunting_control_event_type_dog_discipline
            RR.string.hunting_control_event_type_other -> R.string.hunting_control_event_type_other

            RR.string.hunting_control_hunter_details -> R.string.hunting_control_hunter_details
            RR.string.hunting_control_hunter_name -> R.string.hunting_control_hunter_name
            RR.string.hunting_control_hunter_date_of_birth -> R.string.hunting_control_hunter_date_of_birth
            RR.string.hunting_control_hunter_home_municipality -> R.string.hunting_control_hunter_home_municipality
            RR.string.hunting_control_hunter_number -> R.string.hunting_control_hunter_number
            RR.string.hunting_control_hunting_license -> R.string.hunting_control_hunting_license
            RR.string.hunting_control_hunting_license_status -> R.string.hunting_control_hunting_license_status
            RR.string.hunting_control_hunting_license_status_active -> R.string.hunting_control_hunting_license_status_active
            RR.string.hunting_control_hunting_license_status_inactive -> R.string.hunting_control_hunting_license_status_inactive
            RR.string.hunting_control_hunting_license_date_of_payment -> R.string.hunting_control_hunting_license_date_of_payment
            RR.string.hunting_control_shooting_tests  -> R.string.hunting_control_shooting_tests
            RR.string.hunting_control_reset_hunter_info -> R.string.hunting_control_reset_hunter_info
            RR.string.hunting_control_ssn -> R.string.hunting_control_ssn
            RR.string.hunting_control_searching_hunter -> R.string.hunting_control_searching_hunter
            RR.string.hunting_control_hunter_not_found -> R.string.hunting_control_hunter_not_found
            RR.string.hunting_control_network_error -> R.string.hunting_control_network_error
            RR.string.hunting_control_retry -> R.string.hunting_control_retry

            RR.string.shooting_test_state_waiting_to_start -> R.string.shooting_test_state_waiting
            RR.string.shooting_test_state_ongoing,
            RR.string.shooting_test_state_ongoing_ready_to_close -> R.string.shooting_test_state_ongoing
            RR.string.shooting_test_state_closed -> R.string.shooting_test_state_closed

            RR.string.shooting_test_type_moose -> R.string.shooting_test_type_moose
            RR.string.shooting_test_type_bear -> R.string.shooting_test_type_bear
            RR.string.shooting_test_type_roe_deer -> R.string.shooting_test_type_roe_deer
            RR.string.shooting_test_type_bow -> R.string.shooting_test_type_bow

            RR.string.shooting_test_result_qualified -> R.string.shooting_test_result_qualified
            RR.string.shooting_test_result_unqualified -> R.string.shooting_test_result_unqualified
            RR.string.shooting_test_result_timed_out -> R.string.shooting_test_result_timed_out
            RR.string.shooting_test_result_rebated -> R.string.shooting_test_result_rebated

            RR.string.srva_event_label_other_species_description -> R.string.srva_other_species_description
            RR.string.srva_event_label_approver -> R.string.srva_approver
            RR.string.srva_event_label_rejector -> R.string.srva_rejecter
            RR.string.srva_event_label_specimen_amount -> R.string.srva_specimen_amount
            RR.string.srva_event_label_event_category -> R.string.srva_event
            RR.string.srva_event_label_deportation_order_number -> R.string.srva_event_label_deportation_order_number
            RR.string.srva_event_label_event_type -> R.string.srva_type
            RR.string.srva_event_label_other_event_type_description -> R.string.srva_type_description
            RR.string.srva_event_label_event_type_detail -> R.string.srva_event_label_event_type_detail
            RR.string.srva_event_label_other_event_type_detail_description -> R.string.srva_event_label_other_event_type_detail_description
            RR.string.srva_event_label_method -> R.string.srva_method
            RR.string.srva_event_label_other_method_description -> R.string.srva_method_description
            RR.string.srva_event_label_event_result -> R.string.srva_result
            RR.string.srva_event_label_event_result_detail -> R.string.srva_event_label_event_result_detail
            RR.string.srva_event_label_person_count -> R.string.srva_person_count
            RR.string.srva_event_label_hours_spent -> R.string.srva_time_spent
            RR.string.srva_event_label_description -> R.string.description

            RR.string.srva_event_category_accident -> R.string.srva_accident
            RR.string.srva_event_category_deportation -> R.string.srva_deportation
            RR.string.srva_event_category_injured_animal -> R.string.srva_sick_animal

            RR.string.srva_event_type_traffic_accident -> R.string.srva_traffic_accident
            RR.string.srva_event_type_railway_accident -> R.string.srva_railway_accident
            RR.string.srva_event_type_animal_near_houses_area -> R.string.srva_animal_near_houses
            RR.string.srva_event_type_animal_at_food_destination -> R.string.srva_animal_at_food_destination
            RR.string.srva_event_type_injured_animal -> R.string.srva_injured_animal
            RR.string.srva_event_type_animal_on_ice -> R.string.srva_animal_on_ice
            RR.string.srva_event_type_other -> R.string.srva_other

            RR.string.srva_event_type_detail_cared_house_area -> R.string.srva_event_type_detail_cared_house_area
            RR.string.srva_event_type_detail_farm_animal_building -> R.string.srva_event_type_detail_farm_animal_building
            RR.string.srva_event_type_detail_urban_area -> R.string.srva_event_type_detail_urban_area
            RR.string.srva_event_type_detail_carcass_at_forest -> R.string.srva_event_type_detail_carcass_at_forest
            RR.string.srva_event_type_detail_carcass_near_houses_area -> R.string.srva_event_type_detail_carcass_near_houses_area
            RR.string.srva_event_type_detail_garbage_can -> R.string.srva_event_type_detail_garbage_can
            RR.string.srva_event_type_detail_beehive -> R.string.srva_event_type_detail_beehive
            RR.string.srva_event_type_detail_other -> R.string.srva_event_type_detail_other

            RR.string.srva_event_result_animal_found_dead -> R.string.srva_animal_found_dead
            RR.string.srva_event_result_animal_found_and_terminated -> R.string.srva_animal_found_and_terminated
            RR.string.srva_event_result_animal_found_and_not_terminated -> R.string.srva_animal_found_and_not_terminated
            RR.string.srva_event_result_accident_site_not_found -> R.string.srva_accident_site_not_found
            RR.string.srva_event_result_animal_not_found -> R.string.srva_animal_not_found
            RR.string.srva_event_result_animal_terminated -> R.string.srva_animal_terminated
            RR.string.srva_event_result_animal_deported -> R.string.srva_animal_deported
            RR.string.srva_event_result_undue_alarm -> R.string.srva_undue_alarm

            RR.string.srva_event_result_detail_animal_contacted_and_deported -> R.string.srva_event_result_detail_animal_contacted_and_deported
            RR.string.srva_event_result_detail_animal_contacted -> R.string.srva_event_result_detail_animal_contacted
            RR.string.srva_event_result_detail_uncertain_result -> R.string.srva_event_result_detail_uncertain_result

            RR.string.srva_method_traced_with_dog -> R.string.srva_method_traced_with_dog
            RR.string.srva_method_traced_without_dog -> R.string.srva_method_traced_without_dog
            RR.string.srva_method_dog -> R.string.srva_method_dog
            RR.string.srva_method_pain_equipment -> R.string.srva_method_pain_equipment
            RR.string.srva_method_sound_equipment -> R.string.srva_method_sound_equipment
            RR.string.srva_method_vehicle -> R.string.srva_method_vehicle
            RR.string.srva_method_chasing_with_people -> R.string.srva_method_chasing_with_people
            RR.string.srva_method_other -> R.string.srva_other

            RR.string.other_species -> R.string.species_other
            RR.string.unknown_species -> R.string.species_unknown

            RR.string.gender_label -> R.string.gender_title
            RR.string.gender_female -> R.string.gender_female
            RR.string.gender_male -> R.string.gender_male
            RR.string.gender_unknown -> R.string.gender_unknown

            RR.string.age_label -> R.string.age_title
            RR.string.age_adult -> R.string.age_adult
            RR.string.age_young -> R.string.age_young
            RR.string.age_less_than_one_year -> R.string.age_less_than_year
            RR.string.age_between_one_and_two_years -> R.string.age_year_or_two
            RR.string.age_eraus -> R.string.age_eraus
            RR.string.age_unknown -> R.string.age_unknown

            RR.string.sun_day_selection_label -> R.string.sun_day_selection_label
            RR.string.sun_sunrise_label -> R.string.sun_sunrise_label
            RR.string.sun_sunset_label -> R.string.sun_sunset_label
            RR.string.sun_instructions -> R.string.sun_instructions
            RR.string.sun_disclaimer -> R.string.sun_disclaimer
        }
    }

    @StringRes
    override fun mapToStringFormatResourceId(stringFormatId: RR.stringFormat): Int {
        return when (stringFormatId) {
            RR.stringFormat.generic_hours_and_minutes_format -> R.string.hours_and_minutes
            RR.stringFormat.group_hunting_label_permit_formatted -> R.string.group_hunting_permit
            RR.stringFormat.double_format_zero_decimals -> R.string.double_format_zero_decimals
            RR.stringFormat.double_format_one_decimal -> R.string.double_format_one_decimal
            RR.stringFormat.date_format_short -> R.string.date_format_short
        }
    }

    @PluralsRes
    override fun mapToPluralsResourceId(pluralsId: RR.plurals): Int {
        return when (pluralsId) {
            RR.plurals.hours -> R.plurals.hours
            RR.plurals.minutes -> R.plurals.minutes
        }
    }
}
