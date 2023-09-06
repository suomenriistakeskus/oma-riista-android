package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.constants.SpeciesConstants
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.isDeer
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.model.ObservationConstants
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.ui.common.ObservationSpecimenFieldProducer
import fi.riista.common.domain.userInfo.CarnivoreAuthorityInformationProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.TrueOrFalse
import fi.riista.common.model.toTrueOrFalseValue
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.EnumStringListFieldFactory
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.util.LocalDateTimeProvider

internal class ModifyObservationFieldProducer(
    carnivoreAuthorityInformationProvider: CarnivoreAuthorityInformationProvider,
    private val metadataProvider: MetadataProvider,
    private val stringProvider: StringProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    private val specimenFieldProducer = ObservationSpecimenFieldProducer(
        carnivoreAuthorityInformationProvider = carnivoreAuthorityInformationProvider,
        metadataProvider = metadataProvider,
        stringProvider = stringProvider
    )

    internal val selectableObservationSpecies: SpeciesField.SelectableSpecies = SpeciesField.SelectableSpecies.All

    private val observationCategoryFieldFactory = EnumStringListFieldFactory.create<ObservationCategory>(stringProvider)
    private val observationTypeFieldFactory = EnumStringListFieldFactory.create<ObservationType>(stringProvider)
    // for within moose/deer hunting fields
    private val trueOrFalseFieldFactory = EnumStringListFieldFactory.create<TrueOrFalse>(stringProvider)
    private val deerHuntingTypeFieldFactory = EnumStringListFieldFactory.create<DeerHuntingType>(stringProvider)

    fun createField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ) : DataField<CommonObservationField>? {
        return when (fieldSpecification.fieldId) {
            CommonObservationField.LOCATION -> createLocationField(fieldSpecification, observation)
            CommonObservationField.SPECIES_AND_IMAGE -> createSpeciesCodeField(fieldSpecification, observation)
            CommonObservationField.DATE_AND_TIME -> createDateAndTimeField(fieldSpecification, observation)
            CommonObservationField.OBSERVATION_TYPE -> createObservationTypeField(fieldSpecification, observation)
            CommonObservationField.OBSERVATION_CATEGORY -> createObservationCategoryField(fieldSpecification, observation)
            CommonObservationField.WITHIN_MOOSE_HUNTING -> createWithinMooseHuntingField(fieldSpecification, observation)
            CommonObservationField.WITHIN_DEER_HUNTING -> createWithinDeerHuntingField(fieldSpecification, observation)
            CommonObservationField.DEER_HUNTING_TYPE -> createDeerHuntingTypeField(fieldSpecification, observation)
            CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION -> createDeerHuntingOtherTypeDescriptionField(fieldSpecification, observation)
            CommonObservationField.SPECIMEN_AMOUNT -> createSpecimenAmountField(fieldSpecification, observation)
            CommonObservationField.ERROR_SPECIMEN_AMOUNT_AT_LEAST_TWO -> errorSpecimenAmountTooLow(fieldSpecification)
            CommonObservationField.SPECIMENS ->
                specimenFieldProducer.createSpecimenField(
                    fieldSpecification = fieldSpecification,
                    observation = observation
                ) {
                    readOnly = observation.observationType.value == ObservationType.PARI
                }
            CommonObservationField.MOOSE_LIKE_MALE_AMOUNT -> createMooseLikeMaleAmount(fieldSpecification, observation)
            CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT -> createMooseLikeFemaleAmount(fieldSpecification, observation)
            CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT -> createMooseLikeFemale1CalfAmount(fieldSpecification, observation)
            CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT -> createMooseLikeFemale2CalfAmount(fieldSpecification, observation)
            CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT -> createMooseLikeFemale3CalfAmount(fieldSpecification, observation)
            CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT -> createMooseLikeFemale4CalfAmount(fieldSpecification, observation)
            CommonObservationField.MOOSE_LIKE_CALF_AMOUNT -> createMooseLikeCalfAmount(fieldSpecification, observation)
            CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT -> createMooseLikeUnknownSpecimenAmount(fieldSpecification, observation)
            CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY -> createVerifiedByCarnivoreAuthorityField(fieldSpecification, observation)
            CommonObservationField.TASSU_OBSERVER_NAME -> createObserverNameField(fieldSpecification, observation)
            CommonObservationField.TASSU_OBSERVER_PHONENUMBER -> createObserverPhoneNumberField(fieldSpecification, observation)
            CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO -> createOfficialAdditionalInformationField(fieldSpecification, observation)
            CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE,
            CommonObservationField.TASSU_LITTER,
            CommonObservationField.TASSU_PACK -> {
                // these are meant to be readonly fields and thus should not be displayed while modifying
                logger.d { "Requested to display ${fieldSpecification.fieldId} which is assumed to be readonly field" }
                null
            }
            CommonObservationField.DESCRIPTION -> createDescriptionField(fieldSpecification, observation)
        }
    }

    private fun createLocationField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): LocationField<CommonObservationField> {
        return LocationField(fieldSpecification.fieldId, observation.location) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun createSpeciesCodeField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): SpeciesField<CommonObservationField> {
        return SpeciesField(
            id = fieldSpecification.fieldId,
            species = observation.species,
            entityImage = observation.images.primaryImage,
        ) {
            showEntityImage = true
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            selectableSpecies = selectableObservationSpecies
        }
    }

    private fun createDateAndTimeField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): DateAndTimeField<CommonObservationField> {
        return DateAndTimeField(fieldSpecification.fieldId, observation.pointOfTime) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxDateTime = localDateTimeProvider.now()
        }
    }

    private fun createObservationTypeField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringListField<CommonObservationField> {
        val possibleObservationTypes = metadataProvider.observationMetadata.getSpeciesMetadata(observation)
            ?.getObservationTypes(observationCategory = observation.observationCategory)
            ?: listOf()

        return createEnumChoiceField(
            fieldSpecification = fieldSpecification,
            selected = observation.observationType,
            values = possibleObservationTypes,
            label = RR.string.group_hunting_observation_field_observation_type,
            factory = observationTypeFieldFactory
        )
    }

    private fun createObservationCategoryField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringListField<CommonObservationField> {
        val possibleObservationCategories = metadataProvider.observationMetadata.getSpeciesMetadata(observation)
            ?.getAvailableObservationCategories()
            ?: listOf()

        return createEnumChoiceField(
            fieldSpecification = fieldSpecification,
            selected = observation.observationCategory,
            values = possibleObservationCategories,
            label = RR.string.observation_label_observation_category,
            factory = observationCategoryFieldFactory
        )
    }

    private fun createWithinMooseHuntingField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringListField<CommonObservationField> {
        val trueOrFalseValue = when (observation.observationCategory.value) {
            ObservationCategory.NORMAL -> TrueOrFalse.FALSE
            ObservationCategory.MOOSE_HUNTING -> TrueOrFalse.TRUE
            ObservationCategory.DEER_HUNTING -> null
            null -> null
        }
            .let {
                BackendEnum.create(it)
            }

        return createEnumChoiceField(
            fieldSpecification = fieldSpecification,
            selected = trueOrFalseValue,
            label = RR.string.observation_label_within_moose_hunting,
            factory = trueOrFalseFieldFactory
        )
    }

    private fun createWithinDeerHuntingField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringListField<CommonObservationField> {
        val trueOrFalseValue = when (observation.observationCategory.value) {
            ObservationCategory.NORMAL -> TrueOrFalse.FALSE
            ObservationCategory.MOOSE_HUNTING -> null
            ObservationCategory.DEER_HUNTING -> TrueOrFalse.TRUE
            null -> null
        }
            .let {
                BackendEnum.create(it)
            }

        return createEnumChoiceField(
            fieldSpecification = fieldSpecification,
            selected = trueOrFalseValue,
            label = RR.string.observation_label_within_deer_hunting,
            factory = trueOrFalseFieldFactory
        )
    }

    private fun createDeerHuntingTypeField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringListField<CommonObservationField> {
        return createEnumChoiceField(
            fieldSpecification = fieldSpecification,
            selected = observation.deerHuntingType,
            label = RR.string.harvest_label_deer_hunting_type,
            factory = deerHuntingTypeFieldFactory
        )
    }

    private fun createDeerHuntingOtherTypeDescriptionField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringField<CommonObservationField> {
        return createStringField(
            fieldSpecification = fieldSpecification,
            value = observation.deerHuntingOtherTypeDescription ?: "",
            label = RR.string.harvest_label_deer_hunting_other_type_description
        )
    }

    private fun createSpecimenAmountField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createIntField(
            fieldSpecification = fieldSpecification,
            value = observation.totalSpecimenAmount,
            label = RR.string.observation_label_amount,
            maxValue = ObservationConstants.MAX_SPECIMEN_AMOUNT,
        ) {
            readOnly = observation.observationType.value == ObservationType.PARI
        }
    }

    private fun errorSpecimenAmountTooLow(
        fieldSpecification: FieldSpecification<CommonObservationField>,
    ): LabelField<CommonObservationField> {

        return LabelField(
            id = fieldSpecification.fieldId,
            text = stringProvider.getString(RR.string.error_observation_specimen_amount_at_least_two),
            type = LabelField.Type.ERROR,
        ) {
            paddingTop = Padding.NONE
        }
    }

    private fun createMooseLikeMaleAmount(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createAmountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeMaleAmount,
            labelId = if (observation.species.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_male_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_male_amount
            },
        )
    }

    private fun createMooseLikeFemaleAmount(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createAmountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemaleAmount,
            labelId = if (observation.species.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_amount
            },
        )
    }

    private fun createMooseLikeFemale1CalfAmount(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createAmountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemale1CalfAmount,
            labelId = if (observation.species.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_1calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_1calf_amount
            },
        )
    }

    private fun createMooseLikeFemale2CalfAmount(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createAmountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemale2CalfsAmount,
            labelId = if (observation.species.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_2calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_2calf_amount
            },
        )
    }

    private fun createMooseLikeFemale3CalfAmount(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createAmountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemale3CalfsAmount,
            labelId = if (observation.species.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_3calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_3calf_amount
            },
        )
    }

    private fun createMooseLikeFemale4CalfAmount(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createAmountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemale4CalfsAmount,
            labelId = if (observation.species.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_4calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_4calf_amount
            },
        )
    }

    private fun createMooseLikeCalfAmount(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createAmountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeCalfAmount,
            labelId = if (observation.species.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_calf_amount
            },
        )
    }

    private fun createMooseLikeUnknownSpecimenAmount(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): IntField<CommonObservationField> {
        return createAmountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeUnknownSpecimenAmount,
            labelId = if (observation.species.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount
            },
        )
    }

    private fun createVerifiedByCarnivoreAuthorityField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringListField<CommonObservationField> {
        observation.verifiedByCarnivoreAuthority
        val trueOrFalseValue = observation.verifiedByCarnivoreAuthority
            ?.toTrueOrFalseValue()
            .let {
                BackendEnum.create(it)
            }

        return createEnumChoiceField(
            fieldSpecification = fieldSpecification,
            selected = trueOrFalseValue,
            label = RR.string.observation_label_tassu_verified_by_carnivore_authority,
            factory = trueOrFalseFieldFactory
        )
    }

    private fun createObserverNameField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringField<CommonObservationField> {
        return createStringField(
            fieldSpecification = fieldSpecification,
            value = observation.observerName ?: "",
            label = RR.string.observation_label_tassu_observer_name
        ) {
            singleLine = true
        }
    }

    private fun createObserverPhoneNumberField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringField<CommonObservationField> {
        return createStringField(
            fieldSpecification = fieldSpecification,
            value = observation.observerPhoneNumber ?: "",
            label = RR.string.observation_label_tassu_observer_phonenumber
        ) {
            singleLine = true
        }
    }

    private fun createOfficialAdditionalInformationField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringField<CommonObservationField> {
        return createStringField(
            fieldSpecification = fieldSpecification,
            value = observation.officialAdditionalInfo ?: "",
            label = RR.string.observation_label_tassu_official_additional_information
        )
    }

    private fun createDescriptionField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
    ): StringField<CommonObservationField> {
        return createStringField(
            fieldSpecification = fieldSpecification,
            value = observation.description ?: "",
            label = RR.string.observation_label_description,
        )
    }

    private fun createAmountField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        amount: Int?,
        labelId: RR.string,
    ): IntField<CommonObservationField> {
        return IntField(fieldSpecification.fieldId, amount) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_OBSERVATION_MOOSELIKE_AMOUNT
            label = stringProvider.getString(labelId)
        }
    }

    private fun createStringField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        value: String,
        label: RR.string,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<CommonObservationField> {
        return StringField(fieldSpecification.fieldId, value) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            this.label = stringProvider.getString(label)

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }

    private fun createIntField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        value: Int?,
        label: RR.string,
        maxValue: Int,
        configureSettings: (IntField.DefaultIntFieldSettings.() -> Unit)? = null
    ): IntField<CommonObservationField> {
        return IntField(fieldSpecification.fieldId, value) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            this.label = stringProvider.getString(label)
            this.maxValue = maxValue

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }

    private fun <E> createEnumChoiceField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        selected: BackendEnum<E>,
        label: RR.string,
        factory: EnumStringListFieldFactory<E>,
        configureSettings: (StringListField.DefaultStringListFieldSettings.() -> Unit)? = null
    ): StringListField<CommonObservationField> where E : Enum<E>, E : RepresentsBackendEnum, E : LocalizableEnum {
        return createEnumChoiceField(
            fieldSpecification = fieldSpecification,
            selected = selected,
            values = factory.allEnumValues,
            label = label,
            factory = factory,
            configureSettings = configureSettings
        )
    }

    private fun <E> createEnumChoiceField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        selected: BackendEnum<E>,
        values: List<BackendEnum<E>>,
        label: RR.string,
        factory: EnumStringListFieldFactory<E>,
        configureSettings: (StringListField.DefaultStringListFieldSettings.() -> Unit)? = null
    ): StringListField<CommonObservationField> where E : Enum<E>, E : RepresentsBackendEnum, E : LocalizableEnum {
        return factory.create(
            fieldId = fieldSpecification.fieldId,
            currentEnumValue = selected,
            enumValues = values,
            allowEmptyValue = !fieldSpecification.requirementStatus.isRequired(),
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            this.label = stringProvider.getString(label)
            paddingTop = Padding.SMALL_MEDIUM
            paddingBottom = Padding.SMALL_MEDIUM

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }

    companion object {
        private val logger by getLogger(ModifyObservationFieldProducer::class)
    }
}
