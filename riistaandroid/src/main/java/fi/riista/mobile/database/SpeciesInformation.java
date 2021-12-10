package fi.riista.mobile.database;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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

import fi.riista.mobile.R;
import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.SpeciesMapping;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.SpeciesCategory;
import fi.riista.mobile.models.srva.SrvaSpecies;
import fi.riista.mobile.srva.SrvaParametersHelper;
import fi.riista.mobile.utils.Utils;

/**
 * Species information manager
 * Set information with {@link #refreshInfo(android.content.Context)} before trying to read anything.
 */
public abstract class SpeciesInformation {

    // Mooselike animals
    public static final int FALLOW_DEER_ID = 47484;         // Kuusipeura
    public static final int MOOSE_ID = 47503;               // Hirvi
    public static final int ROE_DEER_ID = 47507;            // Metsäkauris
    public static final int WHITE_TAILED_DEER_ID = 47629;   // Valkohäntäpeura
    public static final int WILD_FOREST_DEER_ID = 200556;   // Metsäpeura

    // Large carnivores
    public static final int BEAR_ID = 47348;                // Karhu
    public static final int LYNX_ID = 46615;                // Ilves
    public static final int WOLF_ID = 46549;                // Susi
    public static final int WOLVERINE_ID = 47212;           // Ahma

    // Birds
    public static final int BEAN_GOOSE_ID = 26287;              // Metsähanhi
    public static final int COMMON_EIDER_ID = 26419;            // Haahka
    public static final int COOT_ID = 27381;                    // Nokikana
    public static final int GARGANEY_ID = 26388;                // Heinätavi
    public static final int GOOSANDER_ID = 26442;               // Isokoskelo
    public static final int GREYLAG_GOOSE_ID = 26291;           // Merihanhi
    public static final int LONG_TAILED_DUCK_ID = 26427;        // Alli
    public static final int PINTAIL_ID = 26382;                 // Jouhisorsa
    public static final int POCHARD_ID = 26407;                 // Punasotka
    public static final int RED_BREASTED_MERGANSER_ID = 26440;  // Tukkakoskelo
    public static final int SHOVELER_ID = 26394;                // Lapasorsa
    public static final int TUFTED_DUCK_ID = 26415;             // Tukkasotka
    public static final int WIGEON_ID = 26360;                  // Haapana

    // Other mammals
    public static final int EUROPEAN_BEAVER_ID = 48251;     // Euroopanmajava
    public static final int GREY_SEAL_ID = 47282;           // Halli eli harmaahylje
    public static final int MOUNTAIN_HARE_ID = 50106;       // Metsäjänis
    public static final int OTTER_ID = 47169;               // Saukko
    public static final int POLECAT_ID = 47240;             // Hilleri
    public static final int RINGED_SEAL_ID = 200555;        // Itämerennorppa
    public static final int WILD_BOAR_ID = 47926;           // Villisika

//    public static final Set<Integer> LARGE_CARNIVORES = new HashSet<>(Arrays.asList(BEAR_ID, LYNX_ID, WOLF_ID, WOLVERINE_ID));

    private static final String SPECIES_FILE_NAME = "species.json";

    private static final String TAG = "SpeciesInformation";

    private static SparseArray<SpeciesCategory> mSpeciesCategories = new SparseArray<>();
    private static SparseArray<Species> mSpecies = new SparseArray<>();

    public static boolean isPermitBasedMooselike(final int speciesCode) {
        return speciesCode == SpeciesInformation.MOOSE_ID
                || speciesCode == SpeciesInformation.FALLOW_DEER_ID
                || speciesCode == SpeciesInformation.WHITE_TAILED_DEER_ID
                || speciesCode == SpeciesInformation.WILD_FOREST_DEER_ID;
    }

    /**
     * Read species information from specifications file.
     * Textual information will be in language currently selected by user.
     */
    public static void refreshInfo(final Context context) {
        final String language = Utils.getLanguage().getLanguage();

        try (final InputStream inputStream = context.getAssets().open(SPECIES_FILE_NAME)) {
            final JSONObject jObject = new JSONObject(Utils.parseJSONStream(inputStream));

            loadSpeciesCategories(jObject, language);
            loadSpecies(jObject, language);

        } catch (final IOException | JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static void loadSpeciesCategories(final JSONObject jObject, final String language) {
        mSpeciesCategories.clear();

        try {
            final JSONArray jArray = jObject.getJSONArray("categories");

            for (int i = 0; i < jArray.length(); i++) {
                final JSONObject object = jArray.getJSONObject(i);

                final SpeciesCategory category = new SpeciesCategory();
                category.mId = object.getInt("id");
                category.mName = object.getJSONObject("name").getString(language);

                mSpeciesCategories.append(category.mId, category);
            }
        } catch (final JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static void loadSpecies(final JSONObject jObject, final String language) {
        mSpecies.clear();

        try {
            final JSONArray jArray = jObject.getJSONArray("species");

            for (int i = 0; i < jArray.length(); i++) {
                final JSONObject object = jArray.getJSONObject(i);
                final Species species = new Species();
                species.mId = object.getInt("id");
                species.mCategory = object.getInt("categoryId");
                species.mName = object.getJSONObject("name").getString(language);
                species.mMultipleSpecimenAllowedOnHarvests = object.getBoolean("multipleSpecimenAllowedOnHarvests");
                mSpecies.append(species.mId, species);
            }
        } catch (final JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static SparseArray<SpeciesCategory> getSpeciesCategories() {
        return mSpeciesCategories;
    }

    public static SpeciesCategory getCategory(final Integer categoryId) {
        return categoryId == null ? null : mSpeciesCategories.get(categoryId);
    }

    public static Species getSpecies(final Integer speciesId) {
        return speciesId == null ? null : mSpecies.get(speciesId);
    }

    public static String getSpeciesName(final Integer speciesId) {
        final Species species = getSpecies(speciesId);
        return species == null ? null : species.mName;
    }

    public static SpeciesCategory categoryForSpecies(final Integer speciesID) {
        if (speciesID != null) {
            final Species species = mSpecies.get(speciesID);
            if (species != null) {
                return mSpeciesCategories.get(species.mCategory);
            }
        }
        return null;
    }

    public static ArrayList<Species> getSpeciesForCategory(final int category) {
        final ArrayList<Species> results = new ArrayList<>();

        for (int i = 0; i < mSpecies.size(); ++i) {
            final Species species = mSpecies.valueAt(i);
            if (species.mCategory == category) {
                results.add(species);
            }
        }
        return results;
    }

    public static void sortSpeciesList(final List<Species> speciesList) {
        final Comparator<Species> comparator = (species1, species2) -> species1.mName.compareTo(species2.mName);
        Collections.sort(speciesList, comparator);
    }

    public static Drawable getSpeciesImage(final Context context, final Integer speciesId) {
        if (speciesId != null) {
            final int drawableId = SpeciesMapping.species.get(speciesId);
            if (drawableId > 0) {
                return context.getResources().getDrawable(drawableId);
            }
        }

        final Drawable unknown = context.getResources().getDrawable(R.drawable.ic_question_mark).mutate();
        unknown.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        return unknown;
    }

    public static boolean isMooseOrDeerRequiringPermitForHunting(final int gameSpeciesCode) {
        // TODO: Missing implementation
        return false;
    }

    public static ArrayList<Species> srvaSupportedSpecies(final boolean includeOther) {
        final ArrayList<Species> speciesList = new ArrayList<>();

        for (final SrvaSpecies species : SrvaParametersHelper.getInstance().getParameters().species) {
            speciesList.add(SpeciesInformation.getSpecies(species.code));
        }
        if (includeOther) {
            final Species other = new Species();
            other.mCategory = null;
            other.mId = null;
            other.mName = RiistaApplication.getInstance().getString(R.string.srva_other);

            speciesList.add(other);
        }

        return speciesList;
    }
}
