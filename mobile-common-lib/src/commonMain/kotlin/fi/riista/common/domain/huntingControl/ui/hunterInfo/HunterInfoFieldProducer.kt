package fi.riista.common.domain.huntingControl.ui.hunterInfo

import fi.riista.common.domain.huntingControl.model.HuntingControlHunterInfo
import fi.riista.common.model.extensions.formatShort
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.ui.dataField.ButtonField
import fi.riista.common.ui.dataField.CustomUserInterfaceField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.util.toStringOrMissingIndicator

internal class HunterInfoFieldProducer(
    private val languageProvider: LanguageProvider,
    private val stringProvider: StringProvider,
) {

    fun createField(
        fieldSpecification: FieldSpecification<HunterInfoField>,
        hunterInfo: HuntingControlHunterInfo?,
        hunterSearch: HunterSearch,
    ) : DataField<HunterInfoField>? {
        return when (fieldSpecification.fieldId.type) {
            HunterInfoField.Type.SCAN_QR_CODE -> {
                CustomUserInterfaceField(
                    id = HunterInfoField(HunterInfoField.Type.SCAN_QR_CODE)
                ) {
                    paddingTop = Padding.SMALL
                    paddingBottom = Padding.SMALL
                }
            }
            HunterInfoField.Type.ENTER_HUNTER_NUMBER -> {
                when (hunterSearch.searchTerm) {
                    is SearchTerm.SearchableHunterNumber -> {
                        IntField(
                            id = fieldSpecification.fieldId,
                            value = hunterSearch.searchTerm.hunterNumber.toIntOrNull()
                        ) {
                            readOnly = hunterSearch.status in listOf(
                                HunterSearch.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER,
                                HunterSearch.Status.HUNTER_FOUND,
                            )
                            maxValue = 99999999 // hunternumber is 8 digits
                            requirementStatus = fieldSpecification.requirementStatus

                            label = stringProvider.getString(RR.string.hunting_control_hunter_number)
                            paddingTop = Padding.NONE
                            paddingBottom = Padding.NONE
                        }
                    }
                    else -> null
                }
            }
            HunterInfoField.Type.ENTERED_SSN -> {
                when (hunterSearch.searchTerm) {
                    is SearchTerm.SearchableSsn -> {
                        StringField(
                            id = fieldSpecification.fieldId,
                            value = hunterSearch.searchTerm.ssn,
                        ) {
                            readOnly = true
                            singleLine = false
                            this.label = stringProvider.getString(RR.string.hunting_control_ssn)
                            paddingTop = Padding.SMALL_MEDIUM
                            paddingBottom = Padding.SMALL
                        }
                    }
                    else -> null
                }
            }
            HunterInfoField.Type.HUNTER_NUMBER_INFO_OR_ERROR -> {
                val type: LabelField.Type = when (hunterSearch.status) {
                    HunterSearch.Status.ENTERING_HUNTER_NUMBER,
                    HunterSearch.Status.VALID_SEARCH_TERM_ENTERED,
                    HunterSearch.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER,
                    HunterSearch.Status.SEARCHING_PERSON_BY_SSN,
                    HunterSearch.Status.HUNTER_FOUND -> LabelField.Type.INFO
                    HunterSearch.Status.INVALID_HUNTER_NUMBER,
                    HunterSearch.Status.SSN_SEARCH_FAILED_HUNTER_NOT_FOUND,
                    HunterSearch.Status.SSN_SEARCH_FAILED_NETWORK_ERROR,
                    HunterSearch.Status.SEARCH_FAILED_HUNTER_NOT_FOUND,
                    HunterSearch.Status.SEARCH_FAILED_NETWORK_ERROR -> LabelField.Type.ERROR
                }
                val text: String = when (hunterSearch.status) {
                    HunterSearch.Status.ENTERING_HUNTER_NUMBER ->
                        stringProvider.getString(RR.string.group_hunting_enter_hunter_id)
                    HunterSearch.Status.INVALID_HUNTER_NUMBER ->
                        stringProvider.getString(RR.string.group_hunting_invalid_hunter_id)
                    HunterSearch.Status.VALID_SEARCH_TERM_ENTERED ->
                        stringProvider.getString(RR.string.group_hunting_searching_hunter_by_id)
                    HunterSearch.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER,
                    HunterSearch.Status.SEARCHING_PERSON_BY_SSN ->
                        stringProvider.getString(RR.string.hunting_control_searching_hunter)
                    HunterSearch.Status.HUNTER_FOUND -> ""
                    HunterSearch.Status.SSN_SEARCH_FAILED_HUNTER_NOT_FOUND,
                    HunterSearch.Status.SEARCH_FAILED_HUNTER_NOT_FOUND ->
                        stringProvider.getString(RR.string.hunting_control_hunter_not_found)
                    HunterSearch.Status.SSN_SEARCH_FAILED_NETWORK_ERROR,
                    HunterSearch.Status.SEARCH_FAILED_NETWORK_ERROR ->
                        stringProvider.getString(RR.string.hunting_control_network_error)
                }
                return LabelField(
                    id = fieldSpecification.fieldId,
                    text = text,
                    type = type
                ) {
                    paddingTop = Padding.NONE
                    paddingBottom = Padding.LARGE
                }
            }
            HunterInfoField.Type.RETRY_BUTTON -> {
                ButtonField(
                    id = fieldSpecification.fieldId,
                    text = stringProvider.getString(RR.string.hunting_control_retry),
                ) {
                    paddingTop = Padding.NONE
                    paddingBottom = Padding.SMALL
                }
            }
            HunterInfoField.Type.PERSONAL_DATA_HEADER -> {
                stringProvider.getString(RR.string.hunting_control_hunter_details).createCaptionField(fieldSpecification)
            }
            HunterInfoField.Type.NAME -> {
                hunterInfo?.name.createValueField(fieldSpecification, RR.string.hunting_control_hunter_name)
            }
            HunterInfoField.Type.DATE_OF_BIRTH -> {
                hunterInfo?.dateOfBirth?.formatShort(stringProvider)
                    .createValueField(fieldSpecification, RR.string.hunting_control_hunter_date_of_birth)
            }
            HunterInfoField.Type.HOME_MUNICIPALITY -> {
                hunterInfo?.homeMunicipality?.localized(language = languageProvider.getCurrentLanguage())
                    .createValueField(fieldSpecification, RR.string.hunting_control_hunter_home_municipality)
            }
            HunterInfoField.Type.HUNTER_NUMBER -> {
                hunterInfo?.hunterNumber.createValueField(fieldSpecification, RR.string.hunting_control_hunter_number)
            }
            HunterInfoField.Type.HUNTING_LICENSE_HEADER -> {
                stringProvider.getString(RR.string.hunting_control_hunting_license).createCaptionField(
                    fieldSpecification = fieldSpecification,
                    icon = if (hunterInfo?.huntingLicenseActive == true) {
                        LabelField.Icon.VERIFIED
                    } else {
                        null
                    },
                )
            }
            HunterInfoField.Type.HUNTING_LICENSE_STATUS -> {
                when (hunterInfo?.huntingLicenseActive) {
                    true -> stringProvider.getString(RR.string.hunting_control_hunting_license_status_active)
                    false -> stringProvider.getString(RR.string.hunting_control_hunting_license_status_inactive)
                    null -> null
                }.createValueField(fieldSpecification, RR.string.hunting_control_hunting_license_status)
            }
            HunterInfoField.Type.HUNTING_LICENSE_DAY_OF_PAYMENT -> {
                hunterInfo?.huntingLicenseDateOfPayment?.formatShort(stringProvider)
                    .createValueField(fieldSpecification, RR.string.hunting_control_hunting_license_date_of_payment)
            }
            HunterInfoField.Type.SHOOTING_TEST_HEADER -> {
                stringProvider.getString(RR.string.hunting_control_shooting_tests).createCaptionField(fieldSpecification)
            }
            HunterInfoField.Type.SPECIES_CAPTION -> {
                val index = fieldSpecification.fieldId.index
                if (hunterInfo != null && hunterInfo.shootingTests.size > index) {
                    val test = hunterInfo.shootingTests[index]
                    val testType = test.type.localized(stringProvider)
                    testType.createCaptionField(fieldSpecification)
                } else {
                    null
                }
            }
            HunterInfoField.Type.SHOOTING_TEST_INFO -> {
                val index = fieldSpecification.fieldId.index
                if (hunterInfo != null && hunterInfo.shootingTests.size > index) {
                    val test = hunterInfo.shootingTests[index]
                    val text = "${test.rhyName}\n${test.begin.formatShort(stringProvider)} - ${test.end.formatShort(stringProvider)}"
                    LabelField(
                        id = fieldSpecification.fieldId,
                        text = text,
                        type = LabelField.Type.INFO
                    ) {
                        paddingTop = Padding.NONE
                        paddingBottom = Padding.NONE
                    }
                } else {
                    null
                }
            }
            HunterInfoField.Type.RESET_BUTTON -> {
                ButtonField(
                    id = fieldSpecification.fieldId,
                    text = stringProvider.getString(RR.string.hunting_control_reset_hunter_info),
                ) {
                    paddingTop = Padding.MEDIUM
                    paddingBottom = Padding.SMALL
                }
            }
        }
    }

    private fun Any?.createValueField(
        fieldSpecification: FieldSpecification<HunterInfoField>,
        label: RR.string,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<HunterInfoField> {
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

    private fun Any.createCaptionField(
        fieldSpecification: FieldSpecification<HunterInfoField>,
        icon: LabelField.Icon? = null,
    ): LabelField<HunterInfoField> {
        val value = this.toStringOrMissingIndicator()
        return LabelField(
            id = fieldSpecification.fieldId,
            text = value,
            type = LabelField.Type.CAPTION
        ) {
            paddingTop = Padding.MEDIUM_LARGE
            paddingBottom = Padding.SMALL // content right below this item
            this.captionIcon = icon
        }
    }
}
