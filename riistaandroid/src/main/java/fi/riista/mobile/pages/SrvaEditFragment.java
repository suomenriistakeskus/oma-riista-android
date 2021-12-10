package fi.riista.mobile.pages;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.DiaryImageManager;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.ChooseSpeciesActivity;
import fi.riista.mobile.activity.EditActivity;
import fi.riista.mobile.activity.EditActivity.EditBridge;
import fi.riista.mobile.activity.EditActivity.EditListener;
import fi.riista.mobile.activity.SrvaSpecimenActivity;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.models.srva.SrvaEventParameters;
import fi.riista.mobile.models.srva.SrvaMethod;
import fi.riista.mobile.models.srva.SrvaParameters;
import fi.riista.mobile.models.srva.SrvaSpecies;
import fi.riista.mobile.models.srva.SrvaSpecimen;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.srva.SrvaParametersHelper;
import fi.riista.mobile.srva.SrvaValidator;
import fi.riista.mobile.ui.ChoiceView;
import fi.riista.mobile.ui.FullScreenImageDialog;
import fi.riista.mobile.utils.BaseDatabase.DeleteListener;
import fi.riista.mobile.utils.BaseDatabase.SaveListener;
import fi.riista.mobile.utils.EditUtils;
import fi.riista.mobile.utils.UiUtils;
import fi.riista.mobile.utils.Utils;

public class SrvaEditFragment extends Fragment
        implements EditListener, EditBridge, DiaryImageManager.ImageManagerActivityAPI {

    private DiaryImageManager mImageManager;

    private LinearLayout mViewContainer;
    private SrvaEvent mSrvaEvent;
    private Button mDateButton;
    private Button mTimeButton;
    private MaterialButton mSpeciesButton;
    private TextInputEditText mAmountInput;

    private final ActivityResultLauncher<Intent> chooseSpeciesActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onActivitySpeciesResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> chooseSpecimenActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onActivitySpecimenResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> selectPhotoActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                mImageManager.handleSelectPhotoResult(result.getResultCode(), result.getData());
                onImagesChanged(mImageManager.getImages());
            }
    );

    private final ActivityResultLauncher<Intent> captureImageActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                mImageManager.handleCaptureImageResult(result.getResultCode(), result.getData());
                onImagesChanged(mImageManager.getImages());
            }
    );

    public static SrvaEditFragment newInstance(final SrvaEvent event) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable("srva_event", event);

        final SrvaEditFragment fragment = new SrvaEditFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageManager = new DiaryImageManager(requireActivity(), this);

        resetSrvaEvent();
    }

    private void resetSrvaEvent() {
        final Bundle bundle = requireArguments();
        final SrvaEvent event = (SrvaEvent) bundle.getSerializable("srva_event");
        mSrvaEvent = (SrvaEvent) Utils.cloneObject(event);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_srva_edit, container, false);

        mViewContainer = view.findViewById(R.id.layout_srva_container);

        mDateButton = view.findViewById(R.id.btn_edit_date);
        mTimeButton = view.findViewById(R.id.btn_edit_time);
        mSpeciesButton = view.findViewById(R.id.btn_select_species);
        mAmountInput = view.findViewById(R.id.srva_amount_input);
        LinearLayout mImagesLayout = view.findViewById(R.id.edit_image_view);

        mDateButton.setOnClickListener(v -> {
            final Context context = getContext();
            if (context != null) {
                EditUtils.showDateDialog(getContext(), mSrvaEvent.toDateTime(), this::onDateTimeChanged);
            }
        });
        mTimeButton.setOnClickListener(v -> {
            final Context context = getContext();
            if (context != null) {
                EditUtils.showTimeDialog(getContext(), mSrvaEvent.toDateTime(), this::onDateTimeChanged);
            }
        });

        mDateButton.setText(mSrvaEvent.toDateTime().toString(EditUtils.DATE_FORMAT));
        mTimeButton.setText(mSrvaEvent.toDateTime().toString(EditUtils.TIME_FORMAT));

        mSpeciesButton.setOnClickListener(v -> {
            final ArrayList<Species> speciesList = new ArrayList<>();
            for (final SrvaSpecies species : getParameters().species) {
                speciesList.add(SpeciesInformation.getSpecies(species.code));
            }

            EditUtils.startSpeciesActivity(
                    this,
                    2,
                    speciesList,
                    true,
                    chooseSpeciesActivityResultLaunch
            );
        });

        mAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                final Integer amount = Utils.parseInt(s.toString());
                if (amount != null) {
                    changeSpecimenAmount(amount);
                }
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        });

        mImageManager.setItems(mSrvaEvent.getImages());
        mImageManager.setup(mImagesLayout, captureImageActivityResultLaunch, selectPhotoActivityResultLaunch);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        final EditActivity editActivity = (EditActivity) requireActivity();
        editActivity.connectEditFragment(this, this);
    }

    private void onActivitySpeciesResult(int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final Species species = (Species) data.getSerializableExtra(ChooseSpeciesActivity.RESULT_SPECIES);
            if (species.mId != -1) {
                mSrvaEvent.gameSpeciesCode = species.mId;
                mSrvaEvent.otherSpeciesDescription = null;
            } else {
                // Other
                mSrvaEvent.gameSpeciesCode = null;
                mSrvaEvent.otherSpeciesDescription = "";
            }
            updateUi();
        }
    }

    private void onActivitySpecimenResult(int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final List<SrvaSpecimen> specimens =
                    (List<SrvaSpecimen>) data.getSerializableExtra(SrvaSpecimenActivity.RESULT_SRVA_SPECIMEN);
            mSrvaEvent.specimens.clear();
            mSrvaEvent.specimens.addAll(specimens);
            changeSpecimenAmount(specimens.size());
        }
        updateUi();
    }

    private SrvaParameters getParameters() {
        return SrvaParametersHelper.getInstance().getParameters();
    }

    private SrvaEventParameters getEventParametersByName(final String name) {
        for (final SrvaEventParameters event : getParameters().events) {
            if (event.name.equals(mSrvaEvent.eventName)) {
                return event;
            }
        }
        return null;
    }

    private void onDateTimeChanged(final DateTime dateTime) {
        mSrvaEvent.setPointOfTime(dateTime);
        mDateButton.setText(dateTime.toString(EditUtils.DATE_FORMAT));
        mTimeButton.setText(dateTime.toString(EditUtils.TIME_FORMAT));

        validate();
    }

    private void updateUi() {
        mDateButton.setEnabled(isEditModeOn());
        mTimeButton.setEnabled(isEditModeOn());

        mViewContainer.removeAllViews();

        createSelectSpeciesButton();
        if (mSrvaEvent.otherSpeciesDescription != null) {
            createOtherSpeciesDescriptionChoice();
        }
        createApproverInfo();

        createSpecimenDetailsButton();

        createEventNameChoice();

        if (mSrvaEvent.eventName != null) {
            createEventTypeChoice();
        }

        if ("OTHER".equals(mSrvaEvent.eventType)) {
            createEventOtherInput();
        } else {
            mSrvaEvent.otherTypeDescription = null;
        }

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
        // Make sure specimen are initialized.
        changeSpecimenAmount(Math.max(mSrvaEvent.totalSpecimenAmount, 1));

        final Species species = SpeciesInformation.getSpecies(mSrvaEvent.gameSpeciesCode);

        if (species != null) {
            mSrvaEvent.otherSpeciesDescription = null;
            mSpeciesButton.setText(species.mName);
            mSpeciesButton.setIcon(SpeciesInformation.getSpeciesImage(getContext(), species.mId));
            mSpeciesButton.setIconTint(null);
        } else if (mSrvaEvent.otherSpeciesDescription != null) {
            mSpeciesButton.setText(getString(R.string.srva_other));
            mSpeciesButton.setIconResource(R.drawable.ic_question_mark);
            mSpeciesButton.setIconTintResource(R.color.edit_mode_button_icon_tint);
        }

        mSpeciesButton.setEnabled(isEditModeOn());

        mAmountInput.setEnabled(isEditModeOn());
        mAmountInput.setText(String.format(Locale.getDefault(), "%d", mSrvaEvent.totalSpecimenAmount));
    }

    private void createOtherSpeciesDescriptionChoice() {
        final ChoiceView<String> choice = new ChoiceView<>(getActivity(), getString(R.string.srva_other_species_description));
        choice.setEditTextChoice(mSrvaEvent.otherSpeciesDescription, text -> {
            mSrvaEvent.otherSpeciesDescription = text;
            validate();
        });
        choice.setEditTextMode(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 1);
        choice.setEditTextMaxLength(255);

        addChoiceView(choice, true);
    }

    private void createApproverInfo() {
        if (!SrvaEvent.STATE_APPROVED.equals(mSrvaEvent.state) && !SrvaEvent.STATE_REJECTED.equals(mSrvaEvent.state)) {
            return;
        }

        final LinearLayout root =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_approver, mViewContainer, false);

        final ImageView image = root.findViewById(R.id.img_approver_state);
        final TextView nameText = root.findViewById(R.id.txt_approver_name);

        final Activity activity = requireActivity();

        final int trafficLightColor;
        String state;
        if (SrvaEvent.STATE_APPROVED.equals(mSrvaEvent.state)) {
            trafficLightColor = activity.getResources().getColor(R.color.harvest_approved);
            state = getString(R.string.srva_approver);
        } else {
            trafficLightColor = activity.getResources().getColor(R.color.harvest_rejected);
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

    private void changeSpecimenAmount(final int amount) {
        if (mSrvaEvent.specimens == null) {
            mSrvaEvent.specimens = new ArrayList<>();
        }

        // Add missing
        for (int i = mSrvaEvent.specimens.size(); i < amount; ++i) {
            mSrvaEvent.specimens.add(new SrvaSpecimen());
        }

        // Remove extras
        while (mSrvaEvent.specimens.size() > amount) {
            mSrvaEvent.specimens.remove(mSrvaEvent.specimens.size() - 1);
        }
        mSrvaEvent.totalSpecimenAmount = mSrvaEvent.specimens.size();

        validate();
    }

    private void createSpecimenDetailsButton() {
        final Activity activity = requireActivity();
        final int margin = UiUtils.dipToPixels(activity, 0);

        float width = getResources().getDimension(R.dimen.specimen_button_width);
        float height = UiUtils.dipToPixels(activity, 60);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) width, (int) height);
        params.setMargins(UiUtils.dipToPixels(activity, 12), margin, margin, margin);

        final MaterialButton button =
                new MaterialButton(new ContextThemeWrapper(getActivity(), R.style.PrimaryButton), null, 0);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setText(R.string.specimen_details);
        button.setTextColor(getResources().getColor(R.color.onPrimary));
        button.setIconResource(R.drawable.ic_list);
        button.setIconTintResource(R.color.onPrimary);

        mViewContainer.addView(button);

        button.setOnClickListener(v -> {
            final Intent intent = new Intent(getActivity(), SrvaSpecimenActivity.class);
            intent.putExtra(SrvaSpecimenActivity.EXTRA_SRVA_EVENT, mSrvaEvent);
            intent.putExtra(SrvaSpecimenActivity.EXTRA_EDIT_MODE, isEditModeOn());
            chooseSpecimenActivityResultLaunch.launch(intent);
        });
    }

    private boolean isMethodOtherChecked() {
        for (final SrvaMethod method : mSrvaEvent.methods) {
            if ("OTHER".equals(method.name)) {
                return method.isChecked;
            }
        }
        return false;
    }

    private void createEventNameChoice() {
        final List<String> names = new ArrayList<>();
        for (final SrvaEventParameters event : getParameters().events) {
            names.add(event.name);
        }

        final ChoiceView<String> eventNameChoice = new ChoiceView<>(getActivity(), getString(R.string.srva_event));
        eventNameChoice.setLocalizationAlias("INJURED_ANIMAL", R.string.srva_sick_animal);
        eventNameChoice.setChoices(names, mSrvaEvent.eventName, true, (position, value) -> {
            // Don't reset these values
            final String pointOfTime = mSrvaEvent.pointOfTime;
            final GeoLocation location = mSrvaEvent.geoLocation;
            final Integer gameSpeciesCode = mSrvaEvent.gameSpeciesCode;
            final String otherSpeciesDescription = mSrvaEvent.otherSpeciesDescription;
            final int totalSpecimenAmount = mSrvaEvent.totalSpecimenAmount;
            final List<SrvaSpecimen> specimens = mSrvaEvent.specimens;

            resetSrvaEvent();
            mSrvaEvent.eventType = null;
            mSrvaEvent.eventResult = null;
            mSrvaEvent.methods.clear();

            mSrvaEvent.eventName = value;
            mSrvaEvent.pointOfTime = pointOfTime;
            mSrvaEvent.geoLocation = location;
            mSrvaEvent.gameSpeciesCode = gameSpeciesCode;
            mSrvaEvent.otherSpeciesDescription = otherSpeciesDescription;
            mSrvaEvent.totalSpecimenAmount = totalSpecimenAmount;
            mSrvaEvent.specimens = specimens;

            updateUi();
        });
        addChoiceView(eventNameChoice, false);
    }

    private void createEventTypeChoice() {
        final List<String> types = new ArrayList<>();
        final SrvaEventParameters params = getEventParametersByName(mSrvaEvent.eventName);

        if (params != null) {
            types.addAll(params.types);
        }

        final ChoiceView<String> eventTypeChoice = new ChoiceView<>(getActivity(), getString(R.string.srva_type));
        eventTypeChoice.setChoices(types, mSrvaEvent.eventType, true, (position, value) -> {
            mSrvaEvent.eventType = value;
            updateUi();
        });

        addChoiceView(eventTypeChoice, true);
    }

    private void createEventOtherInput() {
        final ChoiceView<String> otherChoice = new ChoiceView<>(getActivity(), getString(R.string.srva_type_description));
        otherChoice.setEditTextChoice(mSrvaEvent.otherTypeDescription, text -> {
            mSrvaEvent.otherTypeDescription = text;
            validate();
        });
        otherChoice.setEditTextMode(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 1);

        addChoiceView(otherChoice, true);
    }

    private void createResultChoice() {
        final List<String> results = new ArrayList<>();
        final SrvaEventParameters params = getEventParametersByName(mSrvaEvent.eventName);

        if (params != null) {
            results.addAll(params.results);
        }

        final ChoiceView<String> resultChoice = new ChoiceView<>(getActivity(), getString(R.string.srva_result));
        resultChoice.setChoices(results, mSrvaEvent.eventResult, true, (position, value) -> {
            mSrvaEvent.eventResult = value;
            validate();
        });

        addChoiceView(resultChoice, false);
    }

    private SrvaMethod getModelMethod(final String name) {
        if (mSrvaEvent.methods == null) {
            mSrvaEvent.methods = new ArrayList<>();
        }

        for (final SrvaMethod method : mSrvaEvent.methods) {
            if (name.equals(method.name)) {
                return method;
            }
        }

        // No such method in event, add it
        final SrvaMethod m = new SrvaMethod();
        m.name = name;
        m.isChecked = false;

        mSrvaEvent.methods.add(m);

        return m;
    }

    private void createMethodChoice() {
        final ChoiceView<String> choice = new ChoiceView<>(getActivity(), getString(R.string.srva_method));
        choice.startMultipleChoices();

        final SrvaEventParameters params = getEventParametersByName(mSrvaEvent.eventName);
        if (params != null) {
            for (final SrvaMethod method : params.methods) {
                final SrvaMethod modelMethod = getModelMethod(method.name);
                choice.addMultipleChoice(modelMethod.name, modelMethod.isChecked, check -> {
                    modelMethod.isChecked = check;
                    updateUi();
                });
            }
        }

        addChoiceView(choice, true);
    }

    private void createMethodOtherChoice() {
        final ChoiceView<String> choice = new ChoiceView<>(getActivity(), getString(R.string.srva_method_description));
        choice.setEditTextChoice(mSrvaEvent.otherMethodDescription, text -> {
            mSrvaEvent.otherMethodDescription = text;
            validate();
        });
        choice.setEditTextMode(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 1);

        addChoiceView(choice, true);
    }

    private void createPersonCountChoice() {
        final ChoiceView<String> choice = new ChoiceView<>(getActivity(), getString(R.string.srva_person_count));
        choice.setEditTextChoice("" + mSrvaEvent.personCount, text -> {
            final Integer count = Utils.parseInt(text);
            if (count != null) {
                mSrvaEvent.personCount = count;
            }
            validate();
        });
        choice.setEditTextMaxValue(100.0f);

        addChoiceView(choice, true);
    }

    private void createTimeSpentChoice() {
        final ChoiceView<String> choice = new ChoiceView<>(getActivity(), getString(R.string.srva_time_spent));
        choice.setEditTextChoice("" + mSrvaEvent.timeSpent, text -> {
            final Integer time = Utils.parseInt(text);
            if (time != null) {
                mSrvaEvent.timeSpent = time;
            }
            validate();
        });
        choice.setEditTextMaxValue(999.0f);

        addChoiceView(choice, true);
    }

    private void addChoiceView(final ChoiceView<String> choiceView, final boolean topMargin) {
        mViewContainer.addView(choiceView);

        if (topMargin) {
            choiceView.setTopMargin(10);
        }
        choiceView.setChoiceEnabled(isEditModeOn());
    }

    private boolean validate() {
        final boolean valid = SrvaValidator.validate(mSrvaEvent);

        final EditActivity activity = (EditActivity) requireActivity();
        activity.setEditValid(valid);

        return valid;
    }

    private boolean isEditModeOn() {
        final EditActivity activity = (EditActivity) requireActivity();
        return isEditable() && activity.isEditModeOn();
    }

    private void onImagesChanged(final List<GameLogImage> images) {
        mSrvaEvent.setLocalImages(images);
    }

    @Override
    public Location getLocation() {
        return mSrvaEvent.toLocation();
    }

    @Override
    public String getLocationSource() {
        return mSrvaEvent.geoLocation != null ? mSrvaEvent.geoLocation.source : null;
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
    public void onEditStart() {
        mDateButton.setEnabled(isEditable());
        mTimeButton.setEnabled(isEditable());

        mImageManager.setEditMode(isEditModeOn());

        updateUi();
    }

    @Override
    public void onEditCancel() {
        mDateButton.setEnabled(false);
        mTimeButton.setEnabled(false);

        mImageManager.setEditMode(false);

        resetSrvaEvent();

        updateUi();

        mImageManager.updateItems(mSrvaEvent.getImages());
    }

    @Override
    public void onEditSave() {
        if (validate()) {
            mSrvaEvent.modified = true;
            mSrvaEvent.srvaEventSpecVersion = AppConfig.SRVA_SPEC_VERSION;

            SrvaDatabase.getInstance().saveEvent(mSrvaEvent, new SaveListener() {
                @Override
                public void onSaved(final long localId) {
                    final EditActivity activity = (EditActivity) requireActivity();
                    activity.finishEdit(mSrvaEvent.type, mSrvaEvent.toDateTime(), localId);
                }

                @Override
                public void onError() {
                    // TODO Report error
                }
            });
        }
    }

    @Override
    public void onDelete() {
        SrvaDatabase.getInstance().deleteEvent(mSrvaEvent, false, new DeleteListener() {
            @Override
            public void onDelete() {
                final EditActivity activity = (EditActivity) requireActivity();
                activity.finishEdit(mSrvaEvent.type, mSrvaEvent.toDateTime(), -1);
            }

            @Override
            public void onError() {
                // TODO
            }
        });
    }

    @Override
    public void onLocationChanged(final Location location, final String source) {
        mSrvaEvent.geoLocation = GeoLocation.fromLocation(location);
        mSrvaEvent.geoLocation.source = source;
        validate();
    }

    @Override
    public void onDescriptionChanged(final String description) {
        mSrvaEvent.description = description;
        validate();
    }

    @Override
    public void viewImage(final GameLogImage image) {
        final FragmentActivity activity = getActivity();

        if (activity != null) {
            final FragmentTransaction fragmentTransaction =
                    getActivity().getSupportFragmentManager().beginTransaction();

            final FullScreenImageDialog dialog = FullScreenImageDialog.newInstance(image);
            dialog.show(fragmentTransaction, FullScreenImageDialog.TAG);
        }
    }

    @Override
    public boolean hasPhotoPermissions() {
        final Context context = requireContext();

        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPhotoPermissions() {
        if (!hasPhotoPermissions()) {
            // Ignore result, user has to tap item again to use granted permission or request it again
            final Activity activity = requireActivity();
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        }
    }
}
