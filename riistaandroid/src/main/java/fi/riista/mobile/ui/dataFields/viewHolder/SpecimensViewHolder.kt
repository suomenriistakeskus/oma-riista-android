package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.SpecimenField
import fi.riista.mobile.R
import fi.riista.mobile.feature.specimens.SpecimensActivity
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

interface SpecimensActivityLauncher<FieldId : DataFieldId> {
    fun viewSpecimens(fieldId: FieldId, mode: SpecimensActivity.Mode, specimenData: SpecimenFieldDataContainer)
}

class SpecimensViewHolder<FieldId : DataFieldId>(
    private val activityLauncher: SpecimensActivityLauncher<FieldId>,
    view: View
) : DataFieldViewHolder<FieldId, SpecimenField<FieldId>>(view) {

    private val specimenButton: MaterialButton = view.findViewById(R.id.btn_specimen_details)

    init {
        specimenButton.setOnClickListener {
            boundDataField?.let { boundField ->
                activityLauncher.viewSpecimens(
                    fieldId = boundField.id,
                    mode = if (boundField.settings.readOnly) {
                        SpecimensActivity.Mode.VIEW
                    } else {
                        SpecimensActivity.Mode.EDIT
                    },
                    specimenData = boundField.specimenData
                )
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: SpecimenField<FieldId>) {
        // nop
    }

    class Factory<FieldId : DataFieldId>(
        private val activityLauncher: SpecimensActivityLauncher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, SpecimenField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.SPECIMEN
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, SpecimenField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_specimen_list, container, attachToRoot)
            return SpecimensViewHolder(activityLauncher, view)
        }
    }
}
