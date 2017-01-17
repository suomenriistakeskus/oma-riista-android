package fi.riista.mobile.activity;

import android.os.Bundle;

import fi.riista.mobile.R;
import fi.riista.mobile.models.SpeciesCategory;
import fi.riista.mobile.pages.ChooseSpecies;

public class ChooseSpeciesActivity extends BaseActivity {

    public static final String EXTRA_SPECIES_CATEGORY = SpeciesCategory.SPECIES_CATEGORY;
    public static final String EXTRA_SPECIES_LIST = "species_list";
    public static final String EXTRA_SHOW_OTHER = "show_other";

    public static final String RESULT_SPECIES = "result_species";

    public static final int SPECIES_REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        ChooseSpecies fragment = new ChooseSpecies();
        fragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction().add(R.id.layout_fragment_container, fragment).commit();
    }
}
