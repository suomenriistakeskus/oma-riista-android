package fi.riista.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import fi.riista.mobile.models.MetsahallitusPermit
import fi.riista.mobile.repository.MetsahallitusPermitRepository
import fi.riista.mobile.utils.AbsentLiveData
import fi.riista.mobile.utils.SupportedLanguage
import javax.inject.Inject

class MetsahallitusPermitViewModel @Inject constructor(
        private val repository: MetsahallitusPermitRepository) : ViewModel() {

    // Mutable LiveData instances are kept private and not exposed to views.
    private val _permitIdentifier = MutableLiveData<String>()
    private val _selectedLanguage = MutableLiveData<SupportedLanguage>()

    private val _permit: LiveData<MetsahallitusPermit> = switchMap(_permitIdentifier) { id ->
        when (id) {
            null -> AbsentLiveData.create()
            else -> repository.findMetsahallitusPermit(id)
        }
    }

    private val _viewState = MediatorLiveData<ViewState>().apply {
        var lastPermit: MetsahallitusPermit? = null
        var lastLanguage: SupportedLanguage? = null

        fun update() {
            this.value = ViewState(lastPermit, lastLanguage)
        }

        addSource(_permit) {
            lastPermit = it
            update()
        }
        addSource(_selectedLanguage) {
            lastLanguage = it
            update()
        }
    }

    val permitTypeAndIdentifier: LiveData<String> = map(_viewState) { it.getPermitTypeAndIdentifier() }

    val permitName: LiveData<String> = map(_viewState) { it.permitName }

    val areaNumberAndName: LiveData<String> = map(_viewState) { it.getAreaNumberAndName() }

    val period: LiveData<String> = map(_viewState) { it.permit?.period }

    val harvestFeedbackUrl: LiveData<String> = map(_viewState) { it.harvestFeedbackUrl }

    fun setPermitIdentifier(permitIdentifier: String?) {
        permitIdentifier?.takeIf { it != _permitIdentifier.value }
            ?.let { _permitIdentifier.value = it }
    }

    fun setLanguageCode(languageCode: String?) {
        val selectedLanguage = SupportedLanguage.fromLanguageCode(languageCode)

        selectedLanguage?.takeIf { it != _selectedLanguage.value }
            ?.let { _selectedLanguage.value = it }
    }

    private class ViewState(val permit: MetsahallitusPermit?, selectedLanguage: SupportedLanguage?) {

        private val languageCode: String? = selectedLanguage?.languageCode

        val permitType: String?
            get() = permit?.getPermitType(languageCode)

        val permitName: String?
            get() = permit?.getPermitName(languageCode)

        val harvestFeedbackUrl: String?
            get() = permit?.getHarvestFeedbackUrl(languageCode)

        fun getPermitTypeAndIdentifier(): String? = permitType?.let { "$it, ${permit?.permitIdentifier}" }

        fun getAreaNumberAndName(): String? = permit?.getAreaNumberAndName(languageCode)
    }
}
