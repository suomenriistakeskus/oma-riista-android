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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.joda.time.LocalDate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.activity.HarvestPermitActivity;
import fi.riista.mobile.activity.MainActivity;
import fi.riista.mobile.database.PermitManager;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.Permit;
import fi.riista.mobile.models.PermitSpeciesAmount;
import fi.riista.mobile.network.CheckPermitNumberTask;
import fi.riista.mobile.utils.FinnishHuntingPermitNumberValidator;
import fi.riista.mobile.utils.Utils;

public class PermitList extends PageFragment {

    protected static final String DATE_TIME_FORMAT = "dd.MM.yyyy";

    // Permit items are listed this number of days before and after current data.
    protected static final int PERMIT_DATE_DAYS_LIMIT = 30;

    private GameHarvest mLogEvent;
    private String mPresetNumber;

    private EditText mPermitNumberInput;
    private Button mPermitNumberButton;
    private ProgressBar mPermitNumberProgress;
    private TextView mErrorText;

    private List<PermitListItem> mPermitListItems = new ArrayList<>();
    private PermitListAdapter mAdapter;

    public static PermitList newInstance(GameHarvest harvest, String permitNumber) {
        PermitList permitList = new PermitList();
        permitList.mLogEvent = harvest;
        permitList.mPresetNumber = permitNumber;

        return permitList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_permit_list, container, false);

        mPermitNumberInput = (EditText) root.findViewById(R.id.permit_list_manual_input);
        mPermitNumberButton = (Button) root.findViewById(R.id.permit_list_manual_button);
        mPermitNumberProgress = (ProgressBar) root.findViewById(R.id.permit_list_progress_bar);
        mErrorText = (TextView) root.findViewById(R.id.permit_list_error_text);

        mAdapter = new PermitListAdapter(getActivity(), mPermitListItems);

        ListView permitListView = (ListView) root.findViewById(R.id.permit_list_item_list);
        permitListView.setAdapter(mAdapter);
        permitListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                PermitListItem permitItem = (PermitListItem) mAdapter.getItem(position);
                if (permitItem != null) {

                    Intent result = new Intent();
                    result.putExtra(HarvestPermitActivity.RESULT_PERMIT_NUMBER, permitItem.getPermitNumber());
                    result.putExtra(HarvestPermitActivity.RESULT_PERMIT_TYPE, permitItem.getPermitType());
                    result.putExtra(HarvestPermitActivity.RESULT_PERMIT_SPECIES, permitItem.getGameSpeciesCode());

                    BaseActivity activity = (BaseActivity) getActivity();
                    activity.setResult(Activity.RESULT_OK, result);
                    activity.finish();
                }
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

        if (mLogEvent == null) {
            getActivity().finish();
        }

        setViewTitle(getString(R.string.permit_list_page_title));

        mPermitListItems = permitListToListItems(PermitManager.getInstance(getActivity()).getAvailablePermits());
        mAdapter.setItems(mPermitListItems);
        mAdapter.getFilter().filter(mPermitNumberInput.getText().toString());
    }

    private List<PermitListItem> permitListToListItems(List<Permit> permitList) {
        List<PermitListItem> result = new ArrayList<>();

        for (Permit permit : permitList) {

            // Only list available permits
            if (!permit.getUnavailable()) {
                result.addAll(permitToListItems(permit));
            }
        }

        return result;
    }

    private List<PermitListItem> permitToListItems(Permit permit) {
        List<PermitListItem> result = new ArrayList<>();

        if (permit.getUnavailable()) {
            return result;
        }

        for (PermitSpeciesAmount speciesItem : permit.getSpeciesAmounts()) {
            if (PermitManager.getInstance(getActivity()).isSpeciesSeasonActive(speciesItem, PERMIT_DATE_DAYS_LIMIT)) {

                PermitListItem item = new PermitListItem(permit.getPermitType(), permit.getPermitNumber(),
                        speciesItem.getGameSpeciesCode(), speciesItem.getAmount(),
                        speciesItem.getBeginDate(), speciesItem.getEndDate(),
                        speciesItem.getBeginDate2(), speciesItem.getEndDate2());

                result.add(item);
            }
        }

        return result;
    }

    private void setupPermitNumberInput() {
        mPermitNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPermitNumberButton.setEnabled(false);
                hideKeyboard();

                CheckPermitNumberTask task = new CheckPermitNumberTask(((MainActivity) getActivity()).getWorkContext(), mPermitNumberInput.getText().toString()) {
                    @Override
                    protected void onDone(Permit permit) {
                        mPermitNumberProgress.setVisibility(View.GONE);

                        PermitManager.getInstance(getActivity()).addManualPermit(permit);
                        mPermitListItems.addAll(permitToListItems(permit));

                        mAdapter.notifyDataSetChanged();
                        mAdapter.getFilter().filter(mPermitNumberInput.getText().toString());

                        updateErrorNoteVisibility(mPermitNumberInput.getText().toString());
                    }

                    @Override
                    protected void onError() {
                        Log.d(Utils.LOG_TAG, String.format("Failed to get permit [%d]", getHttpStatusCode()));

                        mPermitNumberProgress.setVisibility(View.GONE);
                    }
                };
                mPermitNumberProgress.setVisibility(View.VISIBLE);
                task.start();
            }
        });

        mPermitNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mPermitNumberInput.getText().toString();

                mAdapter.getFilter().filter(s.toString());

                mPermitNumberButton.setEnabled(!text.isEmpty()
                        && FinnishHuntingPermitNumberValidator.validate(text, true)
                        && PermitManager.getInstance(getActivity()).getPermit(text) == null);

                updateErrorNoteVisibility(text);
            }
        });
    }

    private void updateErrorNoteVisibility(String input) {
        boolean isValidInput = !input.isEmpty()
                && FinnishHuntingPermitNumberValidator.validate(input, true);

        if (isValidInput) {
            Permit permit = PermitManager.getInstance(getActivity()).getPermit(input);

            if (permit != null && permit.getUnavailable()) {
                mErrorText.setVisibility(View.VISIBLE);
            } else {
                mErrorText.setVisibility(View.GONE);
            }
        } else {
            mErrorText.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public class PermitListAdapter extends BaseAdapter implements Filterable {

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);

        private LayoutInflater mInflater;
        private List<PermitListItem> mOriginalItems;
        private List<PermitListItem> mFilteredItems;
        private PermitItemFilter mFilter = new PermitItemFilter();

        PermitListAdapter(Context context, List<PermitListItem> items) {
            mOriginalItems = items;
            mFilteredItems = items;

            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mFilteredItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mFilteredItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_permit_list_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.numberText = (TextView) convertView.findViewById(R.id.permit_item_number);
                viewHolder.typeText = (TextView) convertView.findViewById(R.id.permit_item_type);
                viewHolder.detailsText = (TextView) convertView.findViewById(R.id.permit_item_details);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            PermitListItem item = (PermitListItem) getItem(position);

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
        void setItems(List<PermitListItem> items) {
            mOriginalItems = items;
            mFilteredItems = items;
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        private String parseSpeciesAmountText(PermitListItem permitListItem) {
            DecimalFormat amountFormat = new DecimalFormat("0.#");

            String text = String.format("%s %s %s\n%s - %s",
                    SpeciesInformation.getSpecies(permitListItem.getGameSpeciesCode()).mName,
                    amountFormat.format(permitListItem.getAmount()),
                    getString(R.string.permit_list_amount_short),
                    mDateFormat.format(permitListItem.getBeginDate().toDate()),
                    mDateFormat.format(permitListItem.getEndDate().toDate()));

            if (permitListItem.getBeginDate2() != null && permitListItem.getEndDate2() != null) {
                text += String.format(",\n%s - %s",
                        mDateFormat.format(permitListItem.getBeginDate2().toDate()),
                        mDateFormat.format(permitListItem.getEndDate2().toDate())
                );
            }

            return text;
        }

        private class PermitItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                String filterString = constraint.toString().toLowerCase();

                FilterResults results = new FilterResults();

                final List<PermitListItem> list = mOriginalItems;

                int count = list.size();
                final ArrayList<PermitListItem> resultList = new ArrayList<>(count);

                String filterableString;

                for (PermitListItem permitItem : list) {
                    filterableString = permitItem.getPermitNumber();
                    if (filterableString.toLowerCase().contains(filterString)) {
                        resultList.add(permitItem);
                    }
                }

                results.values = resultList;
                results.count = resultList.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
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

        PermitListItem(String permitType, String permitNumber, Integer gameSpeciesCode, Double amount, LocalDate beginDate, LocalDate endDate, LocalDate beginDate2, LocalDate endDate2) {
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
