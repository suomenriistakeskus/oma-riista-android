package fi.riista.mobile.database;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.SpeciesCategory;
import fi.riista.mobile.utils.Utils;

/**
 * Species information manager
 * Set information with {@link #refreshInfo(android.content.Context)} before trying to read anything.
 */
public abstract class SpeciesInformation {

    public static final int SPECIES_GREY_SEAL = 47282;

    private static final String SPECIES_FILE_NAME = "species.json";

    private static SparseArray<SpeciesCategory> mSpeciesCategories = new SparseArray<>();
    private static SparseArray<Species> mSpecies = new SparseArray<>();

    /**
     * Read species information from specifications file.
     * Textual information will be in language currently selected by user.
     */
    public static void refreshInfo(Context context) {
        String language = Utils.getLanguage().getLanguage();
        InputStream inputStream;

        try {
            inputStream = context.getAssets().open(SPECIES_FILE_NAME);
            JSONObject jObject = new JSONObject(Utils.parseJSONStream(inputStream));

            loadSpeciesCategories(jObject, language);
            loadSpecies(jObject, language);
        } catch (IOException | JSONException e) {
            Log.e(SpeciesInformation.class.getSimpleName(), e.getMessage());
        }
    }

    private static void loadSpeciesCategories(JSONObject jObject, String language) {
        mSpeciesCategories.clear();

        try {
            JSONArray jArray = jObject.getJSONArray("categories");

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject object = jArray.getJSONObject(i);
                SpeciesCategory category = new SpeciesCategory();
                category.mId = object.getInt("id");
                category.mName = object.getJSONObject("name").getString(language);
                mSpeciesCategories.append(category.mId, category);
            }
        } catch (JSONException e) {
            Log.e(GameDatabase.class.getSimpleName(), e.getMessage());
        }
    }

    private static void loadSpecies(JSONObject jObject, String language) {
        mSpecies.clear();

        try {
            JSONArray jArray = jObject.getJSONArray("species");

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject object = jArray.getJSONObject(i);
                Species species = new Species();
                species.mId = object.getInt("id");
                species.mCategory = object.getInt("categoryId");
                species.mName = object.getJSONObject("name").getString(language);
                species.mMultipleSpecimenAllowedOnHarvests = object.getBoolean("multipleSpecimenAllowedOnHarvests");
                mSpecies.append(species.mId, species);
            }
        } catch (JSONException e) {
            Log.e(GameDatabase.class.getSimpleName(), e.getMessage());
        }
    }

    public static SparseArray<SpeciesCategory> getSpeciesCategories() {
        return mSpeciesCategories;
    }

    public static SparseArray<Species> getSpeciesList() {
        return mSpecies;
    }

    public static Species getSpecies(Integer speciesId) {
        if (speciesId != null) {
            return mSpecies.get(speciesId);
        }
        return null;
    }

    public static SpeciesCategory categoryForSpecies(Integer speciesID) {
        if (speciesID != null) {
            Species species = mSpecies.get(speciesID);
            if (species != null) {
                return mSpeciesCategories.get(species.mCategory);
            }
        }
        return null;

    }

    public static ArrayList<Species> getSpeciesForCategory(int category) {
        ArrayList<Species> results = new ArrayList<>();
        for (int i = 0; i < mSpecies.size(); ++i) {
            Species species = mSpecies.valueAt(i);
            if (species.mCategory == category) {
                results.add(species);
            }
        }
        return results;
    }

    public static void sortSpeciesList(List<Species> speciesList) {
        Comparator<Species> comparator = new Comparator<Species>() {
            @Override
            public int compare(Species species1, Species species2) {
                return species1.mName.compareTo(species2.mName);
            }
        };
        Collections.sort(speciesList, comparator);
    }
}
