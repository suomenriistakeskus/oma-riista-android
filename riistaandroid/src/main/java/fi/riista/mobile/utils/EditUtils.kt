package fi.riista.mobile.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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
        fun showSpeciesCategoryDialog(parent: Fragment, activityResultLauncher: ActivityResultLauncher<Intent>) {
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
            val intent = Intent(fragment.context, ChooseSpeciesActivity::class.java)
            intent.putExtra(ChooseSpeciesActivity.EXTRA_SPECIES_CATEGORY, categoryCode)
            intent.putExtra(ChooseSpeciesActivity.EXTRA_SPECIES_LIST, speciesValues)
            intent.putExtra(ChooseSpeciesActivity.EXTRA_SHOW_OTHER, showOtherSpecies)
            activityResultLauncher.launch(intent)
        }
    }
}
