package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.ChooseSpeciesActivity;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.SpeciesCategory;

/**
 * Species selection screen
 */
public class ChooseSpecies extends PageFragment {

    private SpeciesCategory mCategory;
    private ArrayList<Species> mSpeciesList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_species_list, container, false);
        ListView list = view.findViewById(R.id.speciesListView);

        mSpeciesList = new ArrayList<>();

        Bundle arguments = getArguments();
        if (arguments != null) {
            mCategory = SpeciesInformation.getSpeciesCategories().get(arguments.getInt(SpeciesCategory.SPECIES_CATEGORY));
            mSpeciesList.addAll((ArrayList<Species>) arguments.getSerializable(ChooseSpeciesActivity.EXTRA_SPECIES_LIST));

            if (arguments.getBoolean(ChooseSpeciesActivity.EXTRA_SHOW_OTHER)) {
                Species other = new Species();
                other.mCategory = -1;
                other.mId = -1;
                other.mName = getString(R.string.srva_other);
                mSpeciesList.add(other);
            }
        }
        setViewTitle(mCategory.mName);

        list.setAdapter(null);
        ArrayAdapter<Species> adapter = new ArrayAdapter<Species>(getActivity(),
                R.layout.view_species_item, mSpeciesList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                Species species = mSpeciesList.get(position);

                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View view = convertView;
                if (convertView == null) {
                    view = inflater.inflate(R.layout.view_species_item, parent, false);
                }

                ImageView imageView = view.findViewById(R.id.item_image);
                imageView.setImageDrawable(SpeciesInformation.getSpeciesImage(getActivity(), species.mId));

                TextView textView = view.findViewById(R.id.itemText);
                textView.setText(species.mName);
                return view;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((parentView, selectedItemView, position, id) -> {
            if (mSpeciesList.get(position) != null) {
                Species species = mSpeciesList.get(position);

                Intent results = new Intent();
                results.putExtra(ChooseSpeciesActivity.RESULT_SPECIES, species);
                putFieldId(results);

                Activity activity = requireActivity();
                activity.setResult(Activity.RESULT_OK, results);
                activity.finish();
            }
        });
        return view;
    }

    private void putFieldId(@NonNull Intent results) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            results.putExtra(
                    ChooseSpeciesActivity.EXTRA_FIELD_ID,
                    getArguments().getInt(ChooseSpeciesActivity.EXTRA_FIELD_ID, ChooseSpeciesActivity.INVALID_FIELD_ID)
            );
        }
    }
}
