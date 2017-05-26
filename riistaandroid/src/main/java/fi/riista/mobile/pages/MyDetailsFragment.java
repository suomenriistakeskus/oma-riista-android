package fi.riista.mobile.pages;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.models.user.Address;
import fi.riista.mobile.models.user.Occupation;
import fi.riista.mobile.models.user.Rhy;
import fi.riista.mobile.models.user.UserInfo;
import fi.riista.mobile.utils.AppPreferences;

/**
 * My details/hunting card page.
 * Simply displays stored user information.
 */
public class MyDetailsFragment extends PageFragment {

    private static final String SIMPLE_DATE_FORMAT = "dd.MM.yyyy";
    private static final String SIMPLE_DURATION_FORMAT = "%s - %s";
    private static final String ADDRESS_FORMAT = "%s\n%s %s\n%s";
    private static final String MEMBERSHIP_NAME_FORMAT = "%s (%s)";
    private static final String OCCUPATION_FORMAT = "%s\n%s\n%s";

    private Context mContext;
    private TextView mNameText;
    private TextView mDateOfBirthText;
    private TextView mHomeMunicipality;
    private TextView mAddressText;
    private TextView mHunterIdText;
    private TextView mPaymentText;
    private TextView mMembershipText;
    private TextView mInsuranceText;
    private TextView mNoValidLicenceItem;

    private View mHuntingBanItem;
    private TextView mHuntingBanText;

    private View mHunterIdItem;
    private View mPaymentItem;
    private View mMembershipItem;

    private View mOccupationsTitle;
    private ViewGroup mOccupationsContainer;

    public static MyDetailsFragment newInstance() {
        return new MyDetailsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_details, container, false);
        mNameText = (TextView) view.findViewById(R.id.my_details_name_value);
        mDateOfBirthText = (TextView) view.findViewById(R.id.my_details_date_of_birth_value);
        mHomeMunicipality = (TextView) view.findViewById(R.id.my_details_home_municipality_value);
        mAddressText = (TextView) view.findViewById(R.id.my_details_address_value);
        mHunterIdText = (TextView) view.findViewById(R.id.my_details_hunter_id_value);
        mPaymentText = (TextView) view.findViewById(R.id.my_details_payment_value);
        mMembershipText = (TextView) view.findViewById(R.id.my_details_membership_value);
        mInsuranceText = (TextView) view.findViewById(R.id.my_details_insurance_policy);
        mNoValidLicenceItem = (TextView) view.findViewById(R.id.my_details_no_valid_license);

        mHuntingBanItem = view.findViewById(R.id.my_details_hunting_ban_item);
        mHuntingBanText = (TextView) view.findViewById(R.id.my_details_hunting_ban_value);

        mHunterIdItem = view.findViewById(R.id.my_details_hunter_id_item);
        mPaymentItem = view.findViewById(R.id.my_details_payment_item);
        mMembershipItem = view.findViewById(R.id.my_details_membership_item);

        mOccupationsTitle = view.findViewById(R.id.my_details_occupations_title);
        mOccupationsContainer = (ViewGroup) view.findViewById(R.id.my_details_occupations_container);

        mContext = inflater.getContext();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setViewTitle(getString(R.string.title_my_details));
        ((BaseActivity) getActivity()).onHasActionbarMenu(false);

        UserInfo info = AppPreferences.getUserInfo(mContext);

        // Timestamp check is to make sure the data from server is not in outdated format.
        if (info != null && info.getTimestamp() != null) {
            refreshDisplayInfo(info);
        }
    }

    private void refreshDisplayInfo(UserInfo info) {
        DateFormat df = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

        mNameText.setText(String.format("%s %s", info.getFirstName(), info.getLastName()));
        mDateOfBirthText.setText(df.format(info.getBirthDate()));

        Map<String, String> homeMunicipality = info.getHomeMunicipality();
        String languageCode = AppPreferences.getLanguageCodeSetting(mContext);
        if (homeMunicipality == null || homeMunicipality.isEmpty()) {
            mHomeMunicipality.setText(null);
        } else if (homeMunicipality.containsKey(languageCode)) {
            mHomeMunicipality.setText(homeMunicipality.get(languageCode));
        } else {
            mHomeMunicipality.setText(homeMunicipality.get(AppPreferences.LANGUAGE_CODE_FI));
        }

        if (info.getAddress() != null) {
            Address address = info.getAddress();
            mAddressText.setText(String.format(ADDRESS_FORMAT,
                    address.getStreetAddress(),
                    address.getPostalCode(),
                    address.getCity(),
                    address.getCountry() != null ? address.getCountry() : ""
            ));
        }

        refreshHunterRegistryInfo(info, df);
        refreshOccupationInfo(info, df);
    }

    private void refreshHunterRegistryInfo(UserInfo info, DateFormat df) {
        if (info.getHuntingBanStart() != null || info.getHuntingBanEnd() != null) {
            refreshHuntingBanInfo(info, df);

            mHunterIdItem.setVisibility(View.GONE);
            mPaymentItem.setVisibility(View.GONE);
            mMembershipItem.setVisibility(View.GONE);
            mInsuranceText.setVisibility(View.GONE);
            mNoValidLicenceItem.setVisibility(View.GONE);
            mHuntingBanItem.setVisibility(View.VISIBLE);
        } else if (info.getHuntingCardValidNow()) {
            refreshHuntingCardInfo(info, df);

            mHunterIdItem.setVisibility(View.VISIBLE);
            mPaymentItem.setVisibility(View.VISIBLE);
            mMembershipItem.setVisibility(View.VISIBLE);
            mInsuranceText.setVisibility(View.VISIBLE);
            mNoValidLicenceItem.setVisibility(View.GONE);
            mHuntingBanItem.setVisibility(View.GONE);
        } else {
            mHunterIdItem.setVisibility(View.GONE);
            mPaymentItem.setVisibility(View.GONE);
            mMembershipItem.setVisibility(View.GONE);
            mInsuranceText.setVisibility(View.GONE);
            mNoValidLicenceItem.setVisibility(View.VISIBLE);
            mHuntingBanItem.setVisibility(View.GONE);
        }
    }

    private void refreshHuntingBanInfo(UserInfo info, DateFormat df) {
        mHuntingBanText.setText(String.format(SIMPLE_DURATION_FORMAT,
                info.getHuntingBanStart() != null ? df.format(info.getHuntingBanStart()) : "",
                info.getHuntingBanEnd() != null ? df.format(info.getHuntingBanEnd()) : ""
        ));
    }

    private void refreshHuntingCardInfo(UserInfo info, DateFormat df) {
        mHunterIdText.setText(info.getHunterNumber());
        mPaymentText.setText(info.getHuntingCardValidNow() != null && info.getHuntingCardValidNow()
                ? String.format(getString(R.string.my_details_fee_paid_format), df.format(info.getHuntingCardStart()), df.format(info.getHuntingCardEnd()))
                : getString(R.string.my_details_fee_not_paid));

        Rhy rhy = info.getRhy();
        String languageCode = AppPreferences.getLanguageCodeSetting(mContext);
        if (rhy != null && rhy.getName() != null && !rhy.getName().isEmpty() && rhy.getName().containsKey(languageCode)) {
            mMembershipText.setText(String.format(MEMBERSHIP_NAME_FORMAT, rhy.getName().get(languageCode), rhy.getOfficialCode()));
        } else if (rhy != null && rhy.getName() != null && !rhy.getName().isEmpty()) {
            mMembershipText.setText(String.format(MEMBERSHIP_NAME_FORMAT, rhy.getName().get(AppPreferences.LANGUAGE_CODE_FI), rhy.getOfficialCode()));
        } else {
            mMembershipText.setText(null);
        }
    }

    private void refreshOccupationInfo(UserInfo info, DateFormat df) {
        mOccupationsContainer.removeAllViews();

        if (info.getOccupations() != null && !info.getOccupations().isEmpty()) {
            mOccupationsTitle.setVisibility(View.VISIBLE);
            mOccupationsContainer.removeAllViews();
            mOccupationsContainer.setVisibility(View.VISIBLE);

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View separator = null;

            for (Occupation occupation : info.getOccupations()) {
                View view = inflater.inflate(R.layout.view_occupation_item, mOccupationsContainer, false);
                TextView itemText = (TextView) view.findViewById(R.id.occupation_description);
                separator = view.findViewById(R.id.occupation_separator);

                String occupationDuration;
                if (occupation.getBeginDate() == null && occupation.getEndDate() == null) {
                    occupationDuration = getText(R.string.duration_indefinite).toString();
                } else {
                    occupationDuration = String.format(SIMPLE_DURATION_FORMAT,
                            occupation.getBeginDate() != null ? df.format(occupation.getBeginDate()) : "",
                            occupation.getEndDate() != null ? df.format(occupation.getEndDate()) : ""
                    );
                }

                // Swedish rhy and occupation names are localized. Otherwise use finnish ones
                Map<String, String> occupationTitle = occupation.getName();
                Map<String, String> organizationTitle = null;
                if (occupation.getOrganisation() != null) {
                    organizationTitle = occupation.getOrganisation().getName();
                }
                String languageCode = AppPreferences.getLanguageCodeSetting(mContext);

                if (occupationTitle != null && organizationTitle != null) {
                    itemText.setText(String.format(OCCUPATION_FORMAT,
                            organizationTitle.containsKey(languageCode) ? organizationTitle.get(languageCode) : organizationTitle.get(AppPreferences.LANGUAGE_CODE_FI),
                            occupationTitle.containsKey(languageCode) ? occupationTitle.get(languageCode) : occupationTitle.get(AppPreferences.LANGUAGE_CODE_FI),
                            occupationDuration));
                } else {
                    itemText.setText(String.format(OCCUPATION_FORMAT,
                            organizationTitle != null ? organizationTitle.get(AppPreferences.LANGUAGE_CODE_FI) : null,
                            occupationTitle != null ? occupationTitle.get(AppPreferences.LANGUAGE_CODE_FI) : null,
                            occupationDuration));
                }

                mOccupationsContainer.addView(view);
            }

            // Do not display separator after last item
            if (separator != null) {
                separator.setVisibility(View.GONE);
            }
        } else {
            mOccupationsTitle.setVisibility(View.GONE);
            mOccupationsContainer.setVisibility(View.GONE);
        }
    }
}
