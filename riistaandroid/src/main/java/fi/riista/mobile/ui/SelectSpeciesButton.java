package fi.riista.mobile.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import fi.riista.mobile.R;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.utils.Utils;

public class SelectSpeciesButton extends LinearLayout {

    private Button mSpeciesButton;
    private TextView mSpeciesText;
    private TextView mSpeciesMandatoryMark;
    private ImageView mSpeciesImage;
    private EditText mAmountInput;

    public SelectSpeciesButton(Context context) {
        super(context);

        init();
    }

    public SelectSpeciesButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.button_select_species, this);

        mSpeciesButton = (Button) findViewById(R.id.speciesButton);
        mSpeciesText = (TextView) findViewById(R.id.speciestext);
        mSpeciesMandatoryMark = (TextView) findViewById(R.id.speciesmandatorymark);
        mSpeciesImage = (ImageView) findViewById(R.id.species_image);
        mAmountInput = (EditText) findViewById(R.id.harvestAmount);
    }

    public Button getSpeciesButton() {
        return mSpeciesButton;
    }

    public EditText getAmountInput() {
        return mAmountInput;
    }

    public void setSpecies(Species species) {
        if (species == null) {
            // Should reset selection here just in case
            return;
        }

        mSpeciesText.setText(species.mName);
        mSpeciesMandatoryMark.setVisibility(View.GONE);
        mSpeciesImage.setImageDrawable(Utils.getSpeciesImage(getContext(), species.mId));
    }

    public void setSpeciesText(String name) {
        mSpeciesText.setText(name);
        mSpeciesMandatoryMark.setVisibility(View.GONE);
        mSpeciesImage.setImageDrawable(Utils.getSpeciesImage(getContext(), null));
    }
}
