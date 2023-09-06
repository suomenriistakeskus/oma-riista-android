package fi.riista.common.domain.harvest.ui.view

import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.indicatorColor
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.common.HarvestSpecimenFieldProducer
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.PermitNumber
import fi.riista.common.domain.permit.harvestPermit.HarvestPermitProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.model.localizedWithFallbacks
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.GenderField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.helpers.WeightFormatter
import fi.riista.common.ui.helpers.formatWeight
import fi.riista.common.util.toStringOrMissingIndicator

internal class ViewHarvestFieldProducer(
    private val harvestPermitProvider: HarvestPermitProvider?,
    private val stringProvider: StringProvider,
    private val languageProvider: LanguageProvider?,
) {
    private val specimenFieldProducer = HarvestSpecimenFieldProducer(
        stringProvider = stringProvider
    )

    private val weightFormatter = WeightFormatter(stringProvider)

    fun createField(
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        harvest: CommonHarvestData,
    ) : DataField<CommonHarvestField>? {
        return when (fieldSpecification.fieldId) {
            CommonHarvestField.LOCATION ->
                LocationField(fieldSpecification.fieldId, harvest.location) {
                    readOnly = true
                }
            CommonHarvestField.SPECIES_CODE_AND_IMAGE ->
                SpeciesField(
                    id = fieldSpecification.fieldId,
                    species = harvest.species,
                    entityImage = harvest.images.primaryImage
                ) {
                    readOnly = true
                    showEntityImage = true
                }
            CommonHarvestField.SPECIES_CODE ->
                SpeciesField(fieldSpecification.fieldId, harvest.species) {
                    readOnly = true
                }
            CommonHarvestField.DATE_AND_TIME ->
                DateAndTimeField(fieldSpecification.fieldId, harvest.pointOfTime) {
                    readOnly = true
                }
            CommonHarvestField.DEER_HUNTING_TYPE -> {
                val deerHuntingTypeValue = harvest.deerHuntingType.value?.resourcesStringId
                    ?.let { stringId ->
                        stringProvider.getString(stringId)
                    } ?: harvest.deerHuntingType.rawBackendEnumValue

                return deerHuntingTypeValue.createValueField(
                    fieldSpecification = fieldSpecification,
                    label = RR.string.harvest_label_deer_hunting_type
                )
            }
            CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION -> {
                val description = harvest.deerHuntingOtherTypeDescription ?: "-"

                description.createValueField(
                    fieldSpecification = fieldSpecification,
                    label = RR.string.harvest_label_deer_hunting_other_type_description
                )
            }
            CommonHarvestField.ACTOR ->
                harvest.actorInfo.personWithHunterNumber
                    ?.let { "${it.byName} ${it.lastName}" }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_actor
                    ) {
                        paddingTop = Padding.MEDIUM
                    }
            CommonHarvestField.SELECTED_CLUB -> {
                if (languageProvider == null) {
                    logger.e { "No language provider, cannot produce field for ${fieldSpecification.fieldId}" }
                    return null
                }

                harvest.selectedClub.organization?.name?.localizedWithFallbacks(languageProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_hunting_club,
                    )
            }
            CommonHarvestField.AUTHOR ->
                harvest.authorInfo
                    ?.let { "${it.byName} ${it.lastName}" }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_author
                    ) {
                        paddingTop = Padding.MEDIUM
                        paddingBottom = Padding.LARGE
                    }
            CommonHarvestField.GENDER ->
                harvest.specimens
                    .firstOrNull()
                    ?.gender?.value
                    .let { gender ->
                        GenderField(fieldSpecification.fieldId, gender) {
                            readOnly = true
                            showUnknown = harvest.unknownGenderAllowed && gender == Gender.UNKNOWN
                        }
                    }
            CommonHarvestField.AGE ->
                harvest.specimens
                    .firstOrNull()
                    ?.age?.value
                    .let { age ->
                        AgeField(fieldSpecification.fieldId, age) {
                            readOnly = true
                            showUnknown = harvest.unknownAgeAllowed && age == GameAge.UNKNOWN
                            paddingBottom = Padding.MEDIUM_LARGE
                        }
                    }
            CommonHarvestField.SPECIMEN_AMOUNT ->
                harvest.amount
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_amount,
                    )
            CommonHarvestField.SPECIMENS ->
                specimenFieldProducer.createSpecimenField(
                    fieldSpecification = fieldSpecification,
                    harvest = harvest,
                    harvestReportingType = null,
                ) {
                    readOnly = true
                }
            CommonHarvestField.NOT_EDIBLE ->
                harvest.specimens
                    .firstOrNull()
                    ?.notEdible
                    ?.let {
                        when (it) {
                            true -> RR.string.generic_yes
                            false -> RR.string.generic_no
                        }
                    }
                    ?.let { stringProvider.getString(it) }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_not_edible
                    )
            CommonHarvestField.WEIGHT ->
                harvest.specimens
                    .firstOrNull()
                    ?.weight
                    ?.formatWeight(weightFormatter, harvest.species)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_weight
                    )
            CommonHarvestField.WEIGHT_ESTIMATED ->
                harvest.specimens
                    .firstOrNull()
                    ?.weightEstimated
                    ?.formatWeight(weightFormatter, harvest.species)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_weight_estimated
                    )
            CommonHarvestField.WEIGHT_MEASURED ->
                harvest.specimens
                    .firstOrNull()
                    ?.weightMeasured
                    ?.formatWeight(weightFormatter, harvest.species)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_weight_measured
                    )
            CommonHarvestField.FITNESS_CLASS ->
                harvest.specimens
                    .firstOrNull()
                    ?.fitnessClass
                    ?.let {
                        it.value?.resourcesStringId?.let { stringId ->
                            stringProvider.getString(stringId)
                        } ?: it.rawBackendEnumValue
                    }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_fitness_class
                    )
            CommonHarvestField.ANTLERS_TYPE ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlersType
                    ?.let {
                        it.value?.resourcesStringId?.let { stringId ->
                            stringProvider.getString(stringId)
                        } ?: it.rawBackendEnumValue
                    }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antlers_type
                    )
            CommonHarvestField.ANTLERS_WIDTH ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlersWidth
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antlers_width
                    )
            CommonHarvestField.ANTLER_POINTS_LEFT ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlerPointsLeft
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antler_points_left
                    )
            CommonHarvestField.ANTLER_POINTS_RIGHT ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlerPointsRight
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antler_points_right
                    )
            CommonHarvestField.ANTLERS_LOST ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlersLost
                    ?.let {
                        when (it) {
                            true -> RR.string.generic_yes
                            false -> RR.string.generic_no
                        }
                    }
                    ?.let { stringProvider.getString(it) }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antlers_lost
                    )
            CommonHarvestField.ANTLERS_GIRTH ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlersGirth
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antlers_girth
                    )
            CommonHarvestField.ANTLER_SHAFT_WIDTH ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlerShaftWidth
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antler_shaft_width
                    )
            CommonHarvestField.ANTLERS_LENGTH ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlersLength
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antlers_length
                    )
            CommonHarvestField.ANTLERS_INNER_WIDTH ->
                harvest.specimens
                    .firstOrNull()
                    ?.antlersInnerWidth
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_antlers_inner_width
                    )
            CommonHarvestField.ALONE ->
                harvest.specimens
                    .firstOrNull()
                    ?.alone
                    ?.let {
                        when (it) {
                            true -> RR.string.generic_yes
                            false -> RR.string.generic_no
                        }
                    }
                    ?.let { stringProvider.getString(it) }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_alone
                    )
            CommonHarvestField.ADDITIONAL_INFORMATION ->
                harvest.specimens
                    .firstOrNull()
                    ?.additionalInfo
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_additional_information
                    )
            CommonHarvestField.PERMIT_INFORMATION -> permitInformation(
                fieldSpecification = fieldSpecification,
                permitNumber = harvest.permitNumber,
                fallbackPermitType = harvest.permitType,
            )
            CommonHarvestField.HARVEST_REPORT_STATE ->
                harvest.harvestState?.let { harvestState ->
                    LabelField(
                        id = fieldSpecification.fieldId,
                        text = stringProvider.getString(harvestState.resourcesStringId),
                        type = LabelField.Type.INDICATOR
                    ) {
                        this.indicatorColor = harvestState.indicatorColor
                        paddingTop = Padding.SMALL
                    }
                }
            CommonHarvestField.WILD_BOAR_FEEDING_PLACE ->
                harvest.feedingPlace
                    ?.let {
                        when (it) {
                            true -> RR.string.generic_yes
                            false -> RR.string.generic_no
                        }
                    }
                    ?.let { stringProvider.getString(it) }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_wild_boar_feeding_place
                    )
            CommonHarvestField.GREY_SEAL_HUNTING_METHOD ->
                harvest.greySealHuntingMethod.localized(stringProvider)
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_grey_seal_hunting_method
                    )
            CommonHarvestField.IS_TAIGA_BEAN_GOOSE ->
                harvest.taigaBeanGoose
                    ?.let {
                        when (it) {
                            true -> RR.string.generic_yes
                            false -> RR.string.generic_no
                        }
                    }
                    ?.let { stringProvider.getString(it) }
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_is_taiga_bean_goose
                    )
            CommonHarvestField.DESCRIPTION ->
                harvest.description
                    .createValueField(
                        fieldSpecification = fieldSpecification,
                        label = RR.string.harvest_label_description
                    )
            CommonHarvestField.OWN_HARVEST,
            CommonHarvestField.ACTOR_HUNTER_NUMBER,
            CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
            CommonHarvestField.SELECTED_CLUB_OFFICIAL_CODE,
            CommonHarvestField.SELECTED_CLUB_OFFICIAL_CODE_INFO_OR_ERROR,
            CommonHarvestField.ANTLER_INSTRUCTIONS,
            CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS,
            CommonHarvestField.HUNTING_DAY_AND_TIME,
            CommonHarvestField.ERROR_DATE_NOT_WITHIN_PERMIT,
            CommonHarvestField.ERROR_DATETIME_IN_FUTURE,
            CommonHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
            CommonHarvestField.HEADLINE_SHOOTER,
            CommonHarvestField.HEADLINE_SPECIMEN,
            CommonHarvestField.SELECT_PERMIT,
            CommonHarvestField.PERMIT_REQUIRED_NOTIFICATION -> null

        }
    }

    @Suppress("NAME_SHADOWING")
    private fun permitInformation(
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        permitNumber: PermitNumber?,
        fallbackPermitType: String?
    ): StringField<CommonHarvestField> {
        return permitNumber
            ?.let { permitNumber ->
                val permitType = harvestPermitProvider?.getPermit(permitNumber)?.permitType
                    ?: fallbackPermitType

                // prefix with permit type if exists, otherwise display just the number
                permitType?.let {
                    "$it\n$permitNumber"
                } ?: permitNumber
            }
            .createValueField(
                fieldSpecification = fieldSpecification,
                label = RR.string.harvest_label_permit_information
            )
    }

    private fun Any?.createValueField(
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        label: RR.string,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<CommonHarvestField> {
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

    companion object {
        private val logger by getLogger(ViewHarvestFieldProducer::class)
    }
}
