package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.ChooseSpeciesActivity;
import fi.riista.mobile.activity.EditActivity;
import fi.riista.mobile.activity.EditActivity.EditBridge;
import fi.riista.mobile.activity.EditActivity.EditListener;
import fi.riista.mobile.activity.ImageViewerActivity;
import fi.riista.mobile.activity.SrvaSpecimenActivity;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.models.srva.SrvaEventParameters;
import fi.riista.mobile.models.srva.SrvaMethod;
import fi.riista.mobile.models.srva.SrvaParameters;
import fi.riista.mobile.models.srva.SrvaSpecies;
import fi.riista.mobile.models.srva.SrvaSpecimen;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.srva.SrvaDatabase.SrvaEventListener;
import fi.riista.mobile.srva.SrvaParametersHelper;
import fi.riista.mobile.srva.SrvaValidator;
import fi.riista.mobile.ui.ChoiceView;
import fi.riista.mobile.ui.ChoiceView.OnCheckListener;
import fi.riista.mobile.ui.ChoiceView.OnChoiceListener;
import fi.riista.mobile.ui.ChoiceView.OnTextListener;
import fi.riista.mobile.ui.HeaderTextView;
import fi.riista.mobile.ui.SelectSpeciesButton;
import fi.riista.mobile.utils.BaseDatabase.DeleteListener;
import fi.riista.mobile.utils.BaseDatabase.SaveListener;
import fi.riista.mobile.utils.UiUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;

public class SrvaEditFragment extends Fragment implements EditListener, EditBridge {

    private static final int SPECIMEN_REQUEST_CODE = 150;

    public static SrvaEditFragment newInstance(SrvaEvent event) {
        SrvaEditFragment fragment = new SrvaEditFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("srva_event", event);
        fragment.setArguments(bundle);

        return fragment;
    }

    @ViewId(R.id.layout_srva_container)
    private LinearLayout mViewContainer;

    private SrvaEvent mSrvaEvent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetSrvaEvent();
    }

    private void resetSrvaEvent() {
        SrvaEvent event = (SrvaEvent) getArguments().getSerializable("srva_event");
        mSrvaEvent = (SrvaEvent) Utils.cloneObject(event);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_srva_edit, container, false);
        ViewAnnotations.apply(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EditActivity editActivity = (EditActivity) getActivity();
        editActivity.connectEditFragment(this, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ChooseSpeciesActivity.SPECIES_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Species species = (Species) data.getSerializableExtra(ChooseSpeciesActivity.RESULT_SPECIES);
                if (species.mId != -1) {
                    mSrvaEvent.gameSpeciesCode = species.mId;
                    mSrvaEvent.otherSpeciesDescription = null;
                } else {
                    //Other
                    mSrvaEvent.gameSpeciesCode = null;
                    mSrvaEvent.otherSpeciesDescription = "";
                }

                updateUi();
            }
        } else if (requestCode == SPECIMEN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                List<SrvaSpecimen> specimens = (List<SrvaSpecimen>) data.getSerializableExtra(SrvaSpecimenActivity.RESULT_SRVA_SPECIMEN);
                mSrvaEvent.specimens.clear();
                mSrvaEvent.specimens.addAll(specimens);
            }
            updateUi();
        }
    }

    private SrvaParameters getParameters() {
        return SrvaParametersHelper.getInstance().getParameters();
    }

    private SrvaEventParameters getEventParametersByName(String name) {
        for (SrvaEventParameters event : getParameters().events) {
            if (event.name.equals(mSrvaEvent.eventName)) {
                return event;
            }
        }
        return null;
    }

    private void updateUi() {
        mViewContainer.removeAllViews();

        addHeaderView(getString(R.string.species));

        createSelectSpeciesButton();
        if (mSrvaEvent.otherSpeciesDescription != null) {
            createOtherSpeciesDescriptionChoice();
        }
        createApproverInfo();

        createSpecimenDetailsButton();

        addHeaderView(getString(R.string.srva_event));

        createEventNameChoice();

        if (mSrvaEvent.eventName != null) {
            createEventTypeChoice();
        }

        if ("OTHER".equals(mSrvaEvent.eventType)) {
            createEventOtherInput();
        } else {
            mSrvaEvent.otherTypeDescription = null;
        }

        addHeaderView(getString(R.string.srva_result));

        createResultChoice();
        createMethodChoice();
        if (isMethodOtherChecked()) {
            createMethodOtherChoice();
        } else {
            mSrvaEvent.otherMethodDescription = null;
        }
        createPersonCountChoice();
        createTimeSpentChoice();

        validate();
    }

    private void createSelectSpeciesButton() {
        int padding = UiUtils.dipToPixels(getActivity(), 5);
        SelectSpeciesButton speciesButton = new SelectSpeciesButton(getActivity());
        speciesButton.setBackgroundColor(getResources().getColor(R.color.edit_choice_bg));
        speciesButton.setPadding(padding, padding, padding, padding);
        speciesButton.getSpeciesButton().setEnabled(isEditModeOn());

        //Make sure specimen are initialized
        changeSpecimenAmount(Math.max(mSrvaEvent.totalSpecimenAmount, 1));

        speciesButton.getAmountInput().setEnabled(isEditModeOn());
        speciesButton.getAmountInput().setText("" + mSrvaEvent.totalSpecimenAmount);
        speciesButton.getAmountInput().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Integer amount = Utils.parseInt(s.toString());
                if (amount != null) {
                    changeSpecimenAmount(amount);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Species species = SpeciesInformation.getSpecies(mSrvaEvent.gameSpeciesCode);
        if (species != null) {
            mSrvaEvent.otherSpeciesDescription = null;
            speciesButton.setSpecies(species);
        } else if (mSrvaEvent.otherSpeciesDescription != null) {
            speciesButton.setSpeciesText(getString(R.string.srva_other));
        }

        speciesButton.getSpeciesButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Species> speciesList = new ArrayList<>();
                for (SrvaSpecies species : getParameters().species) {
                    speciesList.add(SpeciesInformation.getSpecies(species.code));
                }

                UiUtils.startChooseSpeciesActivity(getActivity(), SrvaEditFragment.this, 2, speciesList, true);
            }
        });

        mViewContainer.addView(speciesButton);
    }

    private void createOtherSpeciesDescriptionChoice() {
        ChoiceView choice = new ChoiceView(getActivity(), getString(R.string.srva_other_species_description));
        choice.setEditTextChoice(mSrvaEvent.otherSpeciesDescription, new OnTextListener() {
            @Override
            public void onText(String text) {
                mSrvaEvent.otherSpeciesDescription = text;
                validate();
            }
        });
        choice.setEditTextMode(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 1);
        addChoiceView(choice, true);
    }

    private void createApproverInfo() {
        if (!SrvaEvent.STATE_APPROVED.equals(mSrvaEvent.state) && !SrvaEvent.STATE_REJECTED.equals(mSrvaEvent.state)) {
            return;
        }
        LinearLayout root = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_approver, mViewContainer, false);

        ImageView image = (ImageView) root.findViewById(R.id.img_approver_state);
        TextView nameText = (TextView) root.findViewById(R.id.txt_approver_name);

        int trafficLightColor;
        String state;
        if (SrvaEvent.STATE_APPROVED.equals(mSrvaEvent.state)) {
            trafficLightColor = getActivity().getResources().getColor(R.color.harvest_approved);
            state = getString(R.string.srva_approver);
        } else {
            trafficLightColor = getActivity().getResources().getColor(R.color.harvest_rejected);
            state = getString(R.string.srva_rejecter);
        }
        image.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP);

        state += " (";
        if (mSrvaEvent.approverInfo != null) {
            if (mSrvaEvent.approverInfo.firstName != null) {
                state += mSrvaEvent.approverInfo.firstName;
            }
            state += " ";
            if (mSrvaEvent.approverInfo.lastName != null) {
                state += mSrvaEvent.approverInfo.lastName;
            }
        }
        state += ")";
        nameText.setText(state);

        UiUtils.setTopMargin(root, 10);
        mViewContainer.addView(root);
    }

    private void changeSpecimenAmount(int amount) {
        if (mSrvaEvent.specimens == null) {
            mSrvaEvent.specimens = new ArrayList<>();
        }

        //Add missing
        for (int i = mSrvaEvent.specimens.size(); i < amount; ++i) {
            mSrvaEvent.specimens.add(new SrvaSpecimen());
        }

        //Remove extras
        while (mSrvaEvent.specimens.size() > amount) {
            mSrvaEvent.specimens.remove(mSrvaEvent.specimens.size() - 1);
        }
        mSrvaEvent.totalSpecimenAmount = mSrvaEvent.specimens.size();

        validate();
    }

    private void createSpecimenDetailsButton() {
        int margin = UiUtils.dipToPixels(getActivity(), 10);

        Button button = new Button(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, margin, margin, margin);
        button.setLayoutParams(params);
        button.setBackgroundResource(R.drawable.button);
        button.setText(R.string.specimen_details);
        mViewContainer.addView(button);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SrvaSpecimenActivity.class);
                intent.putExtra(SrvaSpecimenActivity.EXTRA_SRVA_EVENT, mSrvaEvent);
                intent.putExtra(SrvaSpecimenActivity.EXTRA_EDIT_MODE, isEditModeOn());
                startActivityForResult(intent, SPECIMEN_REQUEST_CODE);
            }
        });
    }

    private boolean isMethodOtherChecked() {
        for (SrvaMethod method : mSrvaEvent.methods) {
            if ("OTHER".equals(method.name)) {
                return method.isChecked;
            }
        }
        return false;
    }

    private void createEventNameChoice() {
        List<String> names = new ArrayList<>();
        for (SrvaEventParameters event : getParameters().events) {
            names.add(event.name);
        }

        ChoiceView eventNameChoice = new ChoiceView(getActivity(), getString(R.string.srva_event));
        eventNameChoice.setLocalizationAlias("INJURED_ANIMAL", R.string.srva_sick_animal);
        eventNameChoice.setChoices(names, mSrvaEvent.eventName, true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String value) {
                //Dont reset these values
                String pointOfTime = mSrvaEvent.pointOfTime;
                GeoLocation location = mSrvaEvent.geoLocation;
                Integer gameSpeciesCode = mSrvaEvent.gameSpeciesCode;
                String otherSpeciesDescription = mSrvaEvent.otherSpeciesDescription;
                int totalSpecimenAmount = mSrvaEvent.totalSpecimenAmount;
                List<SrvaSpecimen> specimens = mSrvaEvent.specimens;

                resetSrvaEvent();

                mSrvaEvent.eventName = value;
                mSrvaEvent.pointOfTime = pointOfTime;
                mSrvaEvent.geoLocation = location;
                mSrvaEvent.gameSpeciesCode = gameSpeciesCode;
                mSrvaEvent.otherSpeciesDescription = otherSpeciesDescription;
                mSrvaEvent.totalSpecimenAmount = totalSpecimenAmount;
                mSrvaEvent.specimens = specimens;

                updateUi();
            }
        });
        addChoiceView(eventNameChoice, false);
    }

    private void createEventTypeChoice() {
        List<String> types = new ArrayList<>();
        SrvaEventParameters params = getEventParametersByName(mSrvaEvent.eventName);
        if (params != null) {
            types.addAll(params.types);
        }

        ChoiceView eventTypeChoice = new ChoiceView(getActivity(), getString(R.string.srva_type));
        eventTypeChoice.setChoices(types, mSrvaEvent.eventType, true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String value) {
                mSrvaEvent.eventType = value;
                updateUi();
            }
        });
        addChoiceView(eventTypeChoice, true);
    }

    private void createEventOtherInput() {
        ChoiceView otherChoice = new ChoiceView(getActivity(), getString(R.string.srva_type_description));
        otherChoice.setEditTextChoice(mSrvaEvent.otherTypeDescription, new OnTextListener() {
            @Override
            public void onText(String text) {
                mSrvaEvent.otherTypeDescription = text;
                validate();
            }
        });
        otherChoice.setEditTextMode(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 1);
        addChoiceView(otherChoice, true);
    }

    private void createResultChoice() {
        List<String> results = new ArrayList<>();
        SrvaEventParameters params = getEventParametersByName(mSrvaEvent.eventName);
        if (params != null) {
            results.addAll(params.results);
        }

        ChoiceView resultChoice = new ChoiceView(getActivity(), getString(R.string.srva_result));
        resultChoice.setChoices(results, mSrvaEvent.eventResult, true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String value) {
                mSrvaEvent.eventResult = value;
                validate();
            }
        });
        addChoiceView(resultChoice, false);
    }

    private SrvaMethod getModelMethod(String name) {
        if (mSrvaEvent.methods == null) {
            mSrvaEvent.methods = new ArrayList<>();
        }

        for (SrvaMethod method : mSrvaEvent.methods) {
            if (name.equals(method.name)) {
                return method;
            }
        }

        //No such method in event, add it
        SrvaMethod m = new SrvaMethod();
        m.name = name;
        m.isChecked = false;
        mSrvaEvent.methods.add(m);

        return m;
    }

    private void createMethodChoice() {
        ChoiceView choice = new ChoiceView(getActivity(), getString(R.string.srva_method));
        choice.startMultipleChoices();

        SrvaEventParameters params = getEventParametersByName(mSrvaEvent.eventName);
        if (params != null) {
            for (SrvaMethod method : params.methods) {
                final SrvaMethod modelMethod = getModelMethod(method.name);
                choice.addMultipleChoice(modelMethod.name, modelMethod.isChecked, new OnCheckListener() {
                    @Override
                    public void onCheck(boolean check) {
                        modelMethod.isChecked = check;
                        updateUi();
                    }
                });
            }
        }
        addChoiceView(choice, true);
    }

    private void createMethodOtherChoice() {
        ChoiceView choice = new ChoiceView(getActivity(), getString(R.string.srva_method_description));
        choice.setEditTextChoice(mSrvaEvent.otherMethodDescription, new OnTextListener() {
            @Override
            public void onText(String text) {
                mSrvaEvent.otherMethodDescription = text;
                validate();
            }
        });
        choice.setEditTextMode(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 1);
        addChoiceView(choice, true);
    }

    private void createPersonCountChoice() {
        ChoiceView choice = new ChoiceView(getActivity(), getString(R.string.srva_person_count));
        choice.setEditTextChoice("" + mSrvaEvent.personCount, new OnTextListener() {
            @Override
            public void onText(String text) {
                Integer count = Utils.parseInt(text);
                if (count != null) {
                    mSrvaEvent.personCount = count;
                }
                validate();
            }
        });
        choice.setEditTextMaxValue(100.0f);
        addChoiceView(choice, true);
    }

    private void createTimeSpentChoice() {
        ChoiceView choice = new ChoiceView(getActivity(), getString(R.string.srva_time_spent));
        choice.setEditTextChoice("" + mSrvaEvent.timeSpent, new OnTextListener() {
            @Override
            public void onText(String text) {
                Integer time = Utils.parseInt(text);
                if (time != null) {
                    mSrvaEvent.timeSpent = time;
                }
                validate();
            }
        });
        choice.setEditTextMaxValue(999.0f);
        addChoiceView(choice, true);
    }

    private void addChoiceView(ChoiceView choiceView, boolean topMargin) {
        mViewContainer.addView(choiceView);

        if (topMargin) {
            choiceView.setTopMargin(10);
        }
        choiceView.setChoiceEnabled(isEditModeOn());
    }

    private void addHeaderView(String text) {
        HeaderTextView header = new HeaderTextView(getActivity());
        header.setText(text);
        mViewContainer.addView(header);
    }

    private boolean validate() {
        boolean valid = SrvaValidator.validate(mSrvaEvent);

        ((EditActivity) getActivity()).setEditValid(valid);

        return valid;
    }

    private boolean isEditModeOn() {
        return isEditable() && ((EditActivity) getActivity()).isEditModeOn();
    }

    @Override
    public DateTime getDate() {
        return mSrvaEvent.toDateTime();
    }

    @Override
    public Location getLocation() {
        return mSrvaEvent.toLocation();
    }

    @Override
    public String getLocationSource() {
        if (mSrvaEvent.geoLocation != null) {
            return mSrvaEvent.geoLocation.source;
        }
        return null;
    }

    @Override
    public String getDescription() {
        return mSrvaEvent.description;
    }

    @Override
    public boolean isEditable() {
        return mSrvaEvent.canEdit;
    }

    @Override
    public List<LogImage> getImages() {
        return mSrvaEvent.getAllImages();
    }

    @Override
    public void onEditStart() {
        updateUi();
    }

    @Override
    public void onEditCancel() {
        resetSrvaEvent();

        updateUi();
    }

    @Override
    public void onEditSave() {
        if (validate()) {
            mSrvaEvent.modified = true;
            mSrvaEvent.srvaEventSpecVersion = AppConfig.SRVA_SPEC_VERSION;

            SrvaDatabase.getInstance().saveEvent(mSrvaEvent, new SaveListener() {
                @Override
                public void onSaved(long localId) {
                    ((EditActivity) getActivity()).finishEdit(mSrvaEvent.type, mSrvaEvent.toDateTime(), localId);
                }

                @Override
                public void onError() {
                }
            });
        }
    }

    @Override
    public void onDelete() {
        SrvaDatabase.getInstance().deleteEvent(mSrvaEvent, false, new DeleteListener() {
            @Override
            public void onDelete() {
                ((EditActivity) getActivity()).finishEdit(mSrvaEvent.type, mSrvaEvent.toDateTime(), -1);
            }

            @Override
            public void onError() {
            }
        });
    }

    @Override
    public void onDateChanged(DateTime date) {
        mSrvaEvent.setPointOfTime(date);
        validate();
    }

    @Override
    public void onLocationChanged(Location location, String source) {
        mSrvaEvent.geoLocation = GeoLocation.fromLocation(location);
        mSrvaEvent.geoLocation.source = source;
        validate();
    }

    @Override
    public void onDescriptionChanged(String description) {
        mSrvaEvent.description = description;
        validate();
    }

    @Override
    public void onImagesChanged(List<LogImage> images) {
        mSrvaEvent.setLocalImages(images);
    }

    @Override
    public void onViewImage(final String uuid) {
        SrvaDatabase.getInstance().loadEventsWithAnyImages(new SrvaEventListener() {
            @Override
            public void onEvents(List<SrvaEvent> events) {
                ArrayList<LogImage> images = new ArrayList<>();
                for (SrvaEvent event : events) {
                    images.addAll(event.getAllImages());
                }

                Intent intent = ImageViewerActivity.createIntent(getActivity(), images, uuid);
                startActivity(intent);
            }
        });
    }

}
