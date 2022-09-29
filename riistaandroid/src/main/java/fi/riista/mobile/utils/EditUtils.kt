package fi.riista.mobile.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.mobile.R
import fi.riista.mobile.activity.ChooseSpeciesActivity
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.models.Species
import fi.riista.mobile.models.SpeciesCategory
import org.joda.time.DateTime
import java.util.*

class EditUtils {

    interface OnDateTimeListener {
        fun onDateTime(dateTime: DateTime)
    }

    interface OnDeleteListener {
        fun onDelete()
    }

    companion object {
        const val DATE_FORMAT = "dd.MM.yyyy"
        const val TIME_FORMAT = "HH:mm"

        @JvmStatic
        fun showDateDialog(context: Context, date: DateTime, listener: OnDateTimeListener) {
            //DatePicker months are 0-based
            val dateDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Bug in most Android 4.x versions: onDateSet is called for both button click and dialog dismiss
                // With this check only button clicks are handled
                if (view.isShown) {
                    val newDate = DateTime(year, monthOfYear + 1, dayOfMonth, date.hourOfDay, date.minuteOfHour)
                    listener.onDateTime(newDate)
                }
            }, date.year().get(), date.monthOfYear().get() - 1, date.dayOfMonth().get())
            dateDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
            dateDialog.show()
        }

        @JvmStatic
        fun showTimeDialog(context: Context, date: DateTime, listener: OnDateTimeListener) {
            val timeDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                if (view.isShown) {
                    val newDate = DateTime(date.year, date.monthOfYear, date.dayOfMonth, hourOfDay, minute)
                    listener.onDateTime(newDate)
                }
            }, date.hourOfDay().get(), date.minuteOfHour().get(), true)
            timeDialog.show()
        }

        @JvmStatic
        fun showDeleteDialog(context: Context, listener: OnDeleteListener) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.delete_entry_caption)
                    .setMessage(R.string.deleta_entry_text)
                    .setPositiveButton(android.R.string.ok) { _, _ -> listener.onDelete() }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        }

        @JvmStatic
        fun <FieldId : DataFieldId> startSpeciesSelection(
            parentFragment: Fragment,
            activityResultLauncher: ActivityResultLauncher<Intent>,
            fieldId: FieldId,
            selectableSpecies: SpeciesField.SelectableSpecies,
        ) {
            when (selectableSpecies) {
                SpeciesField.SelectableSpecies.All -> showSpeciesCategoryDialog(
                    parent = parentFragment,
                    fieldId = fieldId.toInt(),
                    activityResultLauncher = activityResultLauncher,
                )
                is SpeciesField.SelectableSpecies.Listed -> {
                    var showOtherSpecies = false
                    val species: ArrayList<Species> = selectableSpecies.species.mapNotNullTo(
                        destination = ArrayList<Species>()
                    ) { species ->
                        when (species) {
                            is fi.riista.common.domain.model.Species.Known ->
                                SpeciesInformation.getSpecies(species.speciesCode)
                            fi.riista.common.domain.model.Species.Other -> {
                                showOtherSpecies = true
                                null
                            }
                            fi.riista.common.domain.model.Species.Unknown -> null
                        }
                    }

                    startSpeciesActivity(
                        fragment = parentFragment,
                        categoryCode = 2, // todo: remove hard coded category
                        speciesValues = species,
                        showOtherSpecies = showOtherSpecies,
                        fieldId = fieldId.toInt(),
                        activityResultLauncher = activityResultLauncher
                    )
                }
            }

        }

        @JvmStatic
        fun showSpeciesCategoryDialog(parent: Fragment, activityResultLauncher: ActivityResultLauncher<Intent>) {
            showSpeciesCategoryDialog(parent, fieldId = ChooseSpeciesActivity.INVALID_FIELD_ID, activityResultLauncher)
        }

        @JvmStatic
        fun showSpeciesCategoryDialog(
            parent: Fragment,
            fieldId: Int,
            activityResultLauncher: ActivityResultLauncher<Intent>
        ) {
            val speciesCategories = SpeciesInformation.getSpeciesCategories()

            val availableCategories = ArrayList<SpeciesCategory>()
            val availableNames = ArrayList<String>()
            for (i in 0 until speciesCategories.size()) {
                val category = speciesCategories.get(speciesCategories.keyAt(i))
                availableCategories.add(category)
                availableNames.add(category.mName)
            }

            parent.context?.let {
                AlertDialog.Builder(it)
                    .setTitle(parent.resources.getString(R.string.species_prompt))
                    .setItems(availableNames.toTypedArray<CharSequence>()) { _, which ->
                        val categoryCode = availableCategories[which].mId
                        val speciesInCategory = SpeciesInformation.getSpeciesForCategory(categoryCode)
                        SpeciesInformation.sortSpeciesList(speciesInCategory)

                        startSpeciesActivity(
                            fragment = parent,
                            categoryCode = categoryCode,
                            speciesValues = speciesInCategory,
                            showOtherSpecies = false,
                            fieldId = fieldId,
                            activityResultLauncher = activityResultLauncher
                        )
                    }
                    .show()
            }
        }

        @JvmStatic
        fun startSpeciesActivity(
            fragment: Fragment,
            categoryCode: Int,
            speciesValues: ArrayList<Species>,
            showOtherSpecies: Boolean,
            activityResultLauncher: ActivityResultLauncher<Intent>
        ) {
            startSpeciesActivity(fragment, categoryCode, speciesValues, showOtherSpecies, fieldId = ChooseSpeciesActivity.INVALID_FIELD_ID, activityResultLauncher)
        }

        @JvmStatic
        fun startSpeciesActivity(
            fragment: Fragment,
            categoryCode: Int,
            speciesValues: ArrayList<Species>,
            showOtherSpecies: Boolean,
            fieldId: Int,
            activityResultLauncher: ActivityResultLauncher<Intent>
        ) {
            val intent = Intent(fragment.context, ChooseSpeciesActivity::class.java)
            intent.putExtra(ChooseSpeciesActivity.EXTRA_SPECIES_CATEGORY, categoryCode)
            intent.putExtra(ChooseSpeciesActivity.EXTRA_SPECIES_LIST, speciesValues)
            intent.putExtra(ChooseSpeciesActivity.EXTRA_SHOW_OTHER, showOtherSpecies)
            intent.putExtra(ChooseSpeciesActivity.EXTRA_FIELD_ID, fieldId)
            activityResultLauncher.launch(intent)
        }
    }
}
