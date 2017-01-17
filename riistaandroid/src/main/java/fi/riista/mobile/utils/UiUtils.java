package fi.riista.mobile.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.ChooseSpeciesActivity;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.SpeciesCategory;
import fi.riista.mobile.models.user.UserInfo;

public class UiUtils {

    public static void startSpeciesSelection(final FragmentActivity activity, final Fragment fragment) {
        SparseArray<SpeciesCategory> speciesCategories = SpeciesInformation.getSpeciesCategories();

        final ArrayList<SpeciesCategory> categories = new ArrayList<>();
        ArrayList<String> categoryNames = new ArrayList<>();
        for (int i = 0; i < speciesCategories.size(); i++) {
            int speciesCategory = speciesCategories.keyAt(i);
            SpeciesCategory category = speciesCategories.get(speciesCategory);
            categories.add(category);
            categoryNames.add(category.mName);
        }
        CharSequence[] speciesArray = categoryNames.toArray(new CharSequence[categoryNames.size()]);

        new AlertDialog.Builder(activity)
                .setTitle(activity.getResources().getString(R.string.species_prompt))
                .setItems(speciesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int category = categories.get(which).mId;

                        ArrayList<Species> species = SpeciesInformation.getSpeciesForCategory(category);
                        SpeciesInformation.sortSpeciesList(species);

                        startChooseSpeciesActivity(activity, fragment, category, species, false);
                    }
                })
                .show();
    }

    public static void startChooseSpeciesActivity(FragmentActivity activity, Fragment fragment,
                                                  int category, ArrayList<Species> species, boolean showOther) {
        Intent intent = new Intent(activity, ChooseSpeciesActivity.class);
        intent.putExtra(ChooseSpeciesActivity.EXTRA_SPECIES_CATEGORY, category);
        intent.putExtra(ChooseSpeciesActivity.EXTRA_SPECIES_LIST, species);
        intent.putExtra(ChooseSpeciesActivity.EXTRA_SHOW_OTHER, showOther);

        if (fragment != null) {
            fragment.startActivityForResult(intent, ChooseSpeciesActivity.SPECIES_REQUEST_CODE);
        } else {
            activity.startActivityForResult(intent, ChooseSpeciesActivity.SPECIES_REQUEST_CODE);
        }
    }

    public static int dipToPixels(Context context, int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    public static void setTopMargin(View view, int dip) {
        LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.topMargin = UiUtils.dipToPixels(view.getContext(), dip);
        view.setLayoutParams(params);
    }

    public static int getSrvaVisibility(Context context) {
        int srvaVisibility = View.GONE;
        UserInfo userInfo = AppPreferences.getUserInfo(context);
        if (userInfo != null && userInfo.getEnableSrva()) {
            srvaVisibility = View.VISIBLE;
        }
        return srvaVisibility;
    }
}
