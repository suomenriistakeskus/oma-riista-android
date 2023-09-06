package fi.riista.mobile.feature.permits.metsahallitusPermits.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.model.localizedWithFallbacks
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.util.letWith
import fi.riista.common.util.prefixed
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.formattedPeriodDates

class MetsahallitusPermitViewHolder(
    view: View,
    private val languageProvider: LanguageProvider,
    private val listener: Listener?,
) : RecyclerView.ViewHolder(view) {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        languageProvider: LanguageProvider,
        listener: Listener?,
    ): this(
        view = inflater.inflate(R.layout.view_mh_permit_card, parent, false),
        languageProvider = languageProvider,
        listener = listener,
    )

    interface Listener {
        fun onMetsahallitusPermitClicked(permit: CommonMetsahallitusPermit)
    }

    private val cardTitle: TextView = view.findViewById(R.id.mh_permit_card_title)
    private val permitName: TextView = view.findViewById(R.id.mh_permit_card_name)
    private val areaName: TextView = view.findViewById(R.id.mh_permit_card_area_name)
    private val permitPeriod: TextView = view.findViewById(R.id.mh_permit_card_period)

    private var boundPermit: CommonMetsahallitusPermit? = null

    init {
        view.setOnClickListener {
            listener?.letWith(boundPermit) { listener, permit ->
                listener.onMetsahallitusPermitClicked(permit)
            }
        }
    }


    fun bind(permit: CommonMetsahallitusPermit) {
        val titleText = when (val permitType = permit.permitType.localizedWithFallbacks(languageProvider)) {
            null -> itemView.context.getString(R.string.my_details_mh_card_title)
            else -> permitType
        }

        @SuppressLint("SetTextI18n")
        cardTitle.text = "$titleText, ${permit.permitIdentifier}"
        areaName.text = permit.areaNumber.plus(
            permit.areaName.localizedWithFallbacks(languageProvider)?.prefixed(" ")
        )
        permitName.text = permit.permitName.localizedWithFallbacks(languageProvider)
        permitPeriod.text = permit.formattedPeriodDates

        boundPermit = permit
    }
}


