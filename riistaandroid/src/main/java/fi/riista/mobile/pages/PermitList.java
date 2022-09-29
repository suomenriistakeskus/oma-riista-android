package fi.riista.mobile.pages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.AppConfig;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.activity.HarvestPermitActivity;
import fi.riista.mobile.database.PermitManager;
import fi.riista.mobile.database.SpeciesResolver;
import fi.riista.mobile.models.Permit;
import fi.riista.mobile.models.PermitSpeciesAmount;
import fi.riista.mobile.network.CheckPermitNumberTask;
import fi.riista.mobile.utils.FinnishHuntingPermitNumberValidator;
import fi.riista.mobile.utils.KeyboardUtils;
import fi.vincit.androidutilslib.context.WorkContext;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME;
import static java.lang.String.format;

public class PermitList extends PageFragment {

    private static final String TAG = "PermitList";
    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy";

    // Permit items are listed this number of days before and after current data.
    private static final int PERMIT_DATE_DAYS_LIMIT = 30;

    @Inject
    @Named(APPLICATION_WORK_CONTEXT_NAME)
    WorkContext mNetworkTaskContext;

    @Inject
    SpeciesResolver mSpeciesResolver;

    @Inject
    PermitManager mPermitManager;

    private String mPresetNumber;

    private EditText mPermitNumberInput;
    private Button mPermitNumberButton;
    private ProgressBar mPermitNumberProgress;
    private TextView mErrorText;

    private List<PermitListItem> mPermitListItems = new ArrayList<>();
    private PermitListAdapter mAdapter;

    public static PermitList newInstance(final String permitNumber) {
        final PermitList permitList = new PermitList();
        permitList.mPresetNumber = permitNumber;
        return permitList;
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    @Override
    public void onAttach(@NonNull final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_permit_list, container, false);

        mPermitNumberInput = root.findViewById(R.id.permit_list_manual_input);
        mPermitNumberButton = root.findViewById(R.id.permit_list_manual_button);
        mPermitNumberProgress = root.findViewById(R.id.permit_list_progress_bar);
        mErrorText = root.findViewById(R.id.permit_list_error_text);

        mAdapter = new PermitListAdapter(getActivity(), mPermitListItems);

        final ListView permitListView = root.findViewById(R.id.permit_list_item_list);
        permitListView.setAdapter(mAdapter);
        permitListView.setOnItemClickListener((parentView, selectedItemView, position, id) -> {
            final PermitListItem permitItem = (PermitListItem) mAdapter.getItem(position);

            if (permitItem != null) {
                final Intent result = new Intent();
                result.putExtra(HarvestPermitActivity.RESULT_PERMIT_NUMBER, permitItem.getPermitNumber());
                result.putExtra(HarvestPermitActivity.RESULT_PERMIT_TYPE, permitItem.getPermitType());
                result.putExtra(HarvestPermitActivity.RESULT_PERMIT_SPECIES, permitItem.getGameSpeciesCode());

                final BaseActivity activity = (BaseActivity) getActivity();
                activity.setResult(Activity.RESULT_OK, result);
                activity.finish();
            }
        });

        setupPermitNumberInput();

        if (mPresetNumber != null && !mPresetNumber.isEmpty()) {
            mPermitNumberInput.setText(mPresetNumber);
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        setViewTitle(getString(R.string.permit_list_page_title));

        mPermitListItems = permitListToListItems(mPermitManager.getAvailablePermits());
        mAdapter.setItems(mPermitListItems);
        mAdapter.getFilter().filter(mPermitNumberInput.getText().toString());
    }

    private List<PermitListItem> permitListToListItems(final List<Permit> permitList) {
        final List<PermitListItem> result = new ArrayList<>();

        for (final Permit permit : permitList) {
            // Only list available permits.
            if (!permit.getUnavailable()) {
                result.addAll(permitToListItems(permit));
            }
        }

        return result;
    }

    private List<PermitListItem> permitToListItems(final Permit permit) {
        final List<PermitListItem> result = new ArrayList<>();

        if (!permit.getUnavailable()) {
            for (final PermitSpeciesAmount speciesItem : permit.getSpeciesAmounts()) {
                if (mPermitManager.isSpeciesSeasonActive(speciesItem, PERMIT_DATE_DAYS_LIMIT)) {

                    final PermitListItem item = new PermitListItem(
                            permit.getPermitType(), permit.getPermitNumber(),
                            speciesItem.getGameSpeciesCode(), speciesItem.getAmount(),
                            speciesItem.getBeginDate(), speciesItem.getEndDate(),
                            speciesItem.getBeginDate2(), speciesItem.getEndDate2());

                    result.add(item);
                }
            }
        }

        return result;
    }

    private void setupPermitNumberInput() {
        mPermitNumberButton.setOnClickListener(v -> {
            mPermitNumberButton.setEnabled(false);
            hideKeyboard();

            final String permitNumberInput = mPermitNumberInput.getText().toString();

            final CheckPermitNumberTask task = new CheckPermitNumberTask(
                    mNetworkTaskContext,
                    permitNumberInput,
                    AppConfig.HARVEST_SPEC_VERSION) {

                @Override
                protected void onFinishObject(final Permit permit) {
                    mPermitNumberProgress.setVisibility(View.GONE);

                    mPermitManager.addManualPermit(permit);
                    mPermitListItems.addAll(permitToListItems(permit));

                    mAdapter.notifyDataSetChanged();
                    mAdapter.getFilter().filter(permitNumberInput);

                    updateErrorNoteVisibility(permitNumberInput);
                }

                @Override
                protected void onError() {
                    Log.d(TAG, format("Failed to get permit [%d]", getHttpStatusCode()));

                    mPermitNumberProgress.setVisibility(View.GONE);
                }
            };
            mPermitNumberProgress.setVisibility(View.VISIBLE);
            task.start();
        });

        mPermitNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                final String text = mPermitNumberInput.getText().toString();

                mAdapter.getFilter().filter(s.toString());

                mPermitNumberButton.setEnabled(!text.isEmpty()
                        && FinnishHuntingPermitNumberValidator.validate(text, true)
                        && mPermitManager.getPermit(text) == null);

                updateErrorNoteVisibility(text);
            }
        });
    }

    private void updateErrorNoteVisibility(final String input) {
        final boolean isValidInput = !input.isEmpty()
                && FinnishHuntingPermitNumberValidator.validate(input, true);

        int visibility = View.GONE;

        if (isValidInput) {
            final Permit permit = mPermitManager.getPermit(input);

            if (permit != null && permit.getUnavailable()) {
                visibility = View.VISIBLE;
            }
        }

        mErrorText.setVisibility(visibility);
    }

    private void hideKeyboard() {
        final Activity activity = requireActivity();

        // Check if no view has focus.
        final View view = activity.getCurrentFocus();

        KeyboardUtils.hideKeyboard(activity, view);
    }

    public class PermitListAdapter extends BaseAdapter implements Filterable {

        @SuppressLint("SimpleDateFormat")
        private final SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);

        private LayoutInflater mInflater;
        private List<PermitListItem> mOriginalItems;
        private List<PermitListItem> mFilteredItems;
        private PermitItemFilter mFilter = new PermitItemFilter();

        PermitListAdapter(final Context context, final List<PermitListItem> items) {
            mOriginalItems = items;
            mFilteredItems = items;

            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mFilteredItems.size();
        }

        @Override
        public Object getItem(final int position) {
            return mFilteredItems.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_permit_list_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.numberText = convertView.findViewById(R.id.permit_item_number);
                viewHolder.typeText = convertView.findViewById(R.id.permit_item_type);
                viewHolder.detailsText = convertView.findViewById(R.id.permit_item_details);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final PermitListItem item = (PermitListItem) getItem(position);

            viewHolder.numberText.setText(item.getPermitNumber());
            viewHolder.typeText.setText(item.getPermitType());
            viewHolder.detailsText.setText(parseSpeciesAmountText(item));

            return convertView;
        }

        /**
         * Clears any filtering.
         *
         * @param items List items
         */
        void setItems(final List<PermitListItem> items) {
            mOriginalItems = items;
            mFilteredItems = items;
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        private String parseSpeciesAmountText(final PermitListItem permitListItem) {
            final DecimalFormat amountFormat = new DecimalFormat("0.#");

            String text = format("%s %s %s\n%s - %s",
                    mSpeciesResolver.findSpecies(permitListItem.getGameSpeciesCode()).mName,
                    amountFormat.format(permitListItem.getAmount()),
                    getString(R.string.permit_list_amount_short),
                    mDateFormat.format(permitListItem.getBeginDate().toDate()),
                    mDateFormat.format(permitListItem.getEndDate().toDate()));

            if (permitListItem.getBeginDate2() != null && permitListItem.getEndDate2() != null) {
                text += format(",\n%s - %s",
                        mDateFormat.format(permitListItem.getBeginDate2().toDate()),
                        mDateFormat.format(permitListItem.getEndDate2().toDate())
                );
            }

            return text;
        }

        private class PermitItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {

                final String filterString = constraint.toString().toLowerCase();

                final FilterResults results = new FilterResults();

                final List<PermitListItem> list = mOriginalItems;
                final ArrayList<PermitListItem> resultList = new ArrayList<>(list.size());

                for (final PermitListItem permitItem : list) {
                    if (permitItem.getPermitNumber().toLowerCase().contains(filterString)) {
                        resultList.add(permitItem);
                    }
                }

                results.values = resultList;
                results.count = resultList.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(final CharSequence constraint, final FilterResults results) {
                mFilteredItems = (ArrayList<PermitListItem>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    static class ViewHolder {
        TextView numberText;
        TextView typeText;
        TextView detailsText;
    }

    private class PermitListItem {
        private String permitType;
        private String permitNumber;
        private Integer gameSpeciesCode;
        private Double amount;
        private LocalDate beginDate;
        private LocalDate endDate;
        private LocalDate beginDate2;
        private LocalDate endDate2;

        PermitListItem(final String permitType,
                       final String permitNumber,
                       final Integer gameSpeciesCode,
                       final Double amount,
                       final LocalDate beginDate,
                       final LocalDate endDate,
                       final LocalDate beginDate2,
                       final LocalDate endDate2) {

            this.permitType = permitType;
            this.permitNumber = permitNumber;
            this.gameSpeciesCode = gameSpeciesCode;
            this.amount = amount;
            this.beginDate = beginDate;
            this.endDate = endDate;
            this.beginDate2 = beginDate2;
            this.endDate2 = endDate2;
        }

        String getPermitType() {
            return permitType;
        }

        String getPermitNumber() {
            return permitNumber;
        }

        public Integer getGameSpeciesCode() {
            return gameSpeciesCode;
        }

        public Double getAmount() {
            return amount;
        }

        LocalDate getBeginDate() {
            return beginDate;
        }

        LocalDate getEndDate() {
            return endDate;
        }

        LocalDate getBeginDate2() {
            return beginDate2;
        }

        LocalDate getEndDate2() {
            return endDate2;
        }
    }
}
