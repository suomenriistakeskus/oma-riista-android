package fi.riista.common.domain.observation.ui.view

import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.isDeer
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.ui.common.ObservationSpecimenFieldProducer
import fi.riista.common.domain.userInfo.CarnivoreAuthorityInformationProvider
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.util.toStringOrMissingIndicator

internal class ViewObservationFieldProducer(
    carnivoreAuthorityInformationProvider: CarnivoreAuthorityInformationProvider,
    metadataProvider: MetadataProvider,
    private val stringProvider: StringProvider,
) {
    private val specimenFieldProducer = ObservationSpecimenFieldProducer(
        carnivoreAuthorityInformationProvider = carnivoreAuthorityInformationProvider,
        metadataProvider = metadataProvider,
        stringProvider = stringProvider
    )

    fun createField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ) : DataField<CommonObservationField>? {
        return when (fieldSpecification.fieldId) {
            CommonObservationField.LOCATION ->
                LocationField(fieldSpecification.fieldId, observation.location) {
                    readOnly = true
                }
            CommonObservationField.SPECIES_AND_IMAGE ->
                SpeciesField(
                        id = fieldSpecification.fieldId,
                        species = observation.species,
                        entityImage = observation.images.primaryImage,
                ) {
                    showEntityImage = true
                }
            CommonObservationField.DATE_AND_TIME ->
                DateAndTimeField(fieldSpecification.fieldId, observation.pointOfTime) {
                    readOnly = true
                }
            CommonObservationField.OBSERVATION_CATEGORY ->
                observation.observationCategory.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_observation_category
                    )
            CommonObservationField.OBSERVATION_TYPE ->
                observation.observationType.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.group_hunting_observation_field_observation_type
                    )
            CommonObservationField.WITHIN_MOOSE_HUNTING -> {
                when (observation.observationCategory.value == ObservationCategory.MOOSE_HUNTING) {
                    true -> RR.string.generic_yes
                    false -> RR.string.generic_no
                }
                    .let { stringProvider.getString(it) }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_within_moose_hunting
                    )
            }
            CommonObservationField.WITHIN_DEER_HUNTING ->  {
                when (observation.observationCategory.value == ObservationCategory.DEER_HUNTING) {
                    true -> RR.string.generic_yes
                    false -> RR.string.generic_no
                }
                    .let { stringProvider.getString(it) }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_within_deer_hunting
                    )
            }
            CommonObservationField.DEER_HUNTING_TYPE ->
                observation.deerHuntingType.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_deer_hunting_type
                    )
            CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION ->
                observation.deerHuntingOtherTypeDescription
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_deer_hunting_other_type_description
                    )
            CommonObservationField.SPECIMEN_AMOUNT ->
                observation.totalSpecimenAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_amount
                    )
            CommonObservationField.SPECIMENS ->
                specimenFieldProducer.createSpecimenField(
                    fieldSpecification = fieldSpecification,
                    observation = observation
                ) {
                    readOnly = true
                }
            CommonObservationField.MOOSE_LIKE_MALE_AMOUNT ->
                observation.mooselikeMaleAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = when (observation.species.isDeer()) {
                            true -> RR.string.group_hunting_observation_field_mooselike_male_amount_within_deer_hunting
                            false -> RR.string.group_hunting_observation_field_mooselike_male_amount
                        }
                    ) {
                        paddingTop = Padding.LARGE
                    }
            CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT ->
                observation.mooselikeFemaleAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = when (observation.species.isDeer()) {
                            true -> RR.string.group_hunting_observation_field_mooselike_female_amount_within_deer_hunting
                            false -> RR.string.group_hunting_observation_field_mooselike_female_amount
                        }
                    )
            CommonObservationField.MOOSE_LIKE_CALF_AMOUNT ->
                observation.mooselikeCalfAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = when (observation.species.isDeer()) {
                            true -> RR.string.group_hunting_observation_field_mooselike_calf_amount_within_deer_hunting
                            false -> RR.string.group_hunting_observation_field_mooselike_calf_amount
                        }
                    )
            CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT ->
                observation.mooselikeFemale1CalfAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = when (observation.species.isDeer()) {
                            true -> RR.string.group_hunting_observation_field_mooselike_female_1calf_amount_within_deer_hunting
                            false -> RR.string.group_hunting_observation_field_mooselike_female_1calf_amount
                        }
                    )
            CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT ->
                observation.mooselikeFemale2CalfsAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = when (observation.species.isDeer()) {
                            true -> RR.string.group_hunting_observation_field_mooselike_female_2calf_amount_within_deer_hunting
                            false -> RR.string.group_hunting_observation_field_mooselike_female_2calf_amount
                        }
                    )
            CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT ->
                observation.mooselikeFemale3CalfsAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = when (observation.species.isDeer()) {
                            true -> RR.string.group_hunting_observation_field_mooselike_female_3calf_amount_within_deer_hunting
                            false -> RR.string.group_hunting_observation_field_mooselike_female_3calf_amount
                        }
                    )
            CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT ->
                observation.mooselikeFemale4CalfsAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = when (observation.species.isDeer()) {
                            true -> RR.string.group_hunting_observation_field_mooselike_female_4calf_amount_within_deer_hunting
                            false -> RR.string.group_hunting_observation_field_mooselike_female_4calf_amount
                        }
                    )
            CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT ->
                observation.mooselikeUnknownSpecimenAmount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = when (observation.species.isDeer()) {
                            true -> RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount_within_deer_hunting
                            false -> RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount
                        }
                    )
            CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY ->
                when (observation.verifiedByCarnivoreAuthority) {
                    true -> RR.string.generic_yes
                    false -> RR.string.generic_no
                    null -> null
                }
                    ?.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_tassu_verified_by_carnivore_authority
                    )
            CommonObservationField.TASSU_OBSERVER_NAME ->
                observation.observerName
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_tassu_observer_name
                    )
            CommonObservationField.TASSU_OBSERVER_PHONENUMBER ->
                observation.observerPhoneNumber
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_tassu_observer_phonenumber
                    )
            CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO ->
                observation.officialAdditionalInfo
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_tassu_official_additional_information
                    )
            CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE ->
                observation.inYardDistanceToResidence
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_tassu_in_yard_distance_to_residence
                    )
            CommonObservationField.TASSU_LITTER ->
                when (observation.litter) {
                    true -> RR.string.generic_yes
                    false -> RR.string.generic_no
                    null -> null
                }
                    ?.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_tassu_litter
                    )
            CommonObservationField.TASSU_PACK ->
                when (observation.pack) {
                    true -> RR.string.generic_yes
                    false -> RR.string.generic_no
                    null -> null
                }
                    ?.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_tassu_pack
                    )
            CommonObservationField.DESCRIPTION ->
                observation.description
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.observation_label_description
                    )
            CommonObservationField.ERROR_SPECIMEN_AMOUNT_AT_LEAST_TWO -> null
        }
    }

    private fun Any?.createValueField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        label: RR.string,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<CommonObservationField> {
        val value = this.toStringOrMissingIndicator()

        return StringField(fieldSpecification.fieldId, value) {
            readOnly = true
            singleLine = true
            this.label = stringProvider.getString(label)
            paddingTop = Padding.SMALL_MEDIUM
            paddingBottom = Padding.SMALL

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }
}
