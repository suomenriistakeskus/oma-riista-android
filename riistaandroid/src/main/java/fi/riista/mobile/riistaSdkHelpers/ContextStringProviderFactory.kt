package fi.riista.mobile.riistaSdkHelpers

import android.content.Context
import fi.riista.common.resources.ContextStringProvider
import fi.riista.common.resources.RR
import fi.riista.common.resources.RStringId
import fi.riista.common.resources.StringIdMapper
import fi.riista.mobile.R


/**
 * A factory that is able to create [ContextStringProvider]s based on given [Context].
 *
 * Also specifies the mapping from [RStringId] to android string resource ids
 * (i.e. implements [StringIdMapper]).
 */
object ContextStringProviderFactory {
    private val stringIdMapper = AppStringIdMapper()

    fun createForContext(context: Context): ContextStringProvider {
        return ContextStringProvider(context, stringIdMapper)
    }
}

private class AppStringIdMapper : StringIdMapper {
    override fun mapToResourceId(stringId: RStringId): Int {
        return when (stringId) {
            RR.string.generic_yes -> R.string.yes
            RR.string.generic_no -> R.string.no
            RR.string.error_date_not_allowed -> R.string.error_date_not_allowed
            RR.string.group_hunting_label_club -> R.string.group_hunting_club
            RR.string.group_hunting_label_season -> R.string.group_hunting_season
            RR.string.group_hunting_label_species -> R.string.group_hunting_species
            RR.string.group_hunting_label_hunting_group -> R.string.group_hunting_group
            RR.string.group_hunting_label_permit_formatted -> R.string.group_hunting_permit

            RR.string.group_hunting_error_time_not_within_hunting_day ->
                R.string.group_hunting_error_time_not_within_hunting_day

            RR.string.group_hunting_harvest_field_hunting_day_and_time ->
                R.string.group_hunting_harvest_field_hunting_day_and_time
            RR.string.group_hunting_harvest_field_actor -> R.string.group_hunting_harvest_field_actor
            RR.string.group_hunting_harvest_field_author -> R.string.group_hunting_harvest_field_author

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
            RR.string.group_hunting_proposed_group_harvest_shooter ->
                R.string.group_hunting_proposed_group_harvest_shooter
            RR.string.group_hunting_proposed_group_harvest_specimen ->
                R.string.group_hunting_proposed_group_harvest_specimen
            RR.string.group_hunting_proposed_group_harvest_actor ->
                R.string.group_hunting_proposed_group_harvest_actor
            RR.string.group_hunting_harvest_field_deer_hunting_type ->
                R.string.deer_hunting_type
            RR.string.group_hunting_harvest_field_deer_hunting_other_type_description ->
                R.string.deer_hunting_type_description
            RR.string.group_hunting_harvest_field_not_edible ->
                R.string.not_edible
            RR.string.group_hunting_harvest_field_weight_estimated ->
                R.string.weight_estimated
            RR.string.group_hunting_harvest_field_weight_measured ->
                R.string.weight_measured
            RR.string.group_hunting_harvest_field_fitness_class ->
                R.string.fitness_class
            RR.string.group_hunting_harvest_field_antlers_type ->
                R.string.antlers_type
            RR.string.group_hunting_harvest_field_antlers_width ->
                R.string.antlers_width
            RR.string.group_hunting_harvest_field_antler_points_left ->
                R.string.antlers_points_left
            RR.string.group_hunting_harvest_field_antler_points_right ->
                R.string.antlers_points_right
            RR.string.group_hunting_harvest_field_antlers_lost ->
                R.string.antlers_lost
            RR.string.group_hunting_harvest_field_antlers_girth ->
                R.string.antlers_girth
            RR.string.group_hunting_harvest_field_antler_shaft_width ->
                R.string.antler_shaft_width
            RR.string.group_hunting_harvest_field_antlers_length ->
                R.string.antlers_length
            RR.string.group_hunting_harvest_field_antlers_inner_width ->
                R.string.antlers_inner_width
            RR.string.group_hunting_harvest_field_alone ->
                R.string.mooselike_calf
            RR.string.group_hunting_harvest_field_additional_information ->
                R.string.additional_info
            RR.string.group_hunting_harvest_field_additional_information_instructions ->
                R.string.additional_info_instructions
            RR.string.group_hunting_harvest_field_additional_information_instructions_white_tailed_deer ->
                R.string.additional_info_instructions_white_tailed_deer

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

            // observation type
            RR.string.observation_type_nako -> R.string.type_sight
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

            RR.string.hunting_club_membership_invitations -> R.string.my_details_hunting_club_membership_invitations
            RR.string.hunting_club_memberships -> R.string.my_details_hunting_club_memberships

            RR.string.poi_location_group_type_sighting_place -> R.string.poi_location_group_type_sighting_place
            RR.string.poi_location_group_type_mineral_lick -> R.string.poi_location_group_type_mineral_lick
            RR.string.poi_location_group_type_feeding_place -> R.string.poi_location_group_type_feeding_place
            RR.string.poi_location_group_type_other -> R.string.poi_location_group_type_other
        }
    }
}
