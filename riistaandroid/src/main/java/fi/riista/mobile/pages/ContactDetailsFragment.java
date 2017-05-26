package fi.riista.mobile.pages;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.utils.Utils;

public class ContactDetailsFragment extends PageFragment {

    public static ContactDetailsFragment newInstance() {
        return new ContactDetailsFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        setViewTitle(getString(R.string.title_contact_details));
        ((BaseActivity) getActivity()).onHasActionbarMenu(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_details, container, false);
        TextView textView = (TextView) view.findViewById(R.id.customer_service_times);

        String textFormat = getResources().getString(R.string.customer_service_times);
        String text = String.format(textFormat, getResources().getString(R.string.customer_service_service_time));
        textView.setText(text);

        textView = (TextView) view.findViewById(R.id.phonenumber);
        final String phoneNumber = getResources().getString(R.string.customer_service_phone_number);
        textView.setText(phoneNumber);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "tel:" + phoneNumber.replace(" ", "");
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivitySafe(intent);
            }
        });

        setupEmailView(view);

        return view;
    }

    void setupEmailView(View view) {
        final String emailAddress = getResources().getString(R.string.customer_service_email_address);
        TextView emailTextView = (TextView) view.findViewById(R.id.emailtext);
        emailTextView.setText(emailAddress);
        LinearLayout emailView = (LinearLayout) view.findViewById(R.id.sendemail);

        String versionName;
        try {
            versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = getResources().getString(R.string.unknown);
        }
        final String version = versionName;

        emailView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                String textFormat = getResources().getString(R.string.emailtemplate);
                String manufacturer = android.os.Build.MANUFACTURER.substring(0, 1).toUpperCase() + android.os.Build.MANUFACTURER.substring(1);
                String deviceString = manufacturer + " " + android.os.Build.MODEL;
                String androidVersion = android.os.Build.VERSION.RELEASE;
                String emailText = String.format(textFormat, version, deviceString, androidVersion);
                intent.putExtra(Intent.EXTRA_TEXT, emailText);
                startActivitySafe(Intent.createChooser(intent, getResources().getString(R.string.sendemail)));
            }
        });
    }

    private void startActivitySafe(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Utils.LogMessage("Can't start activity: " + e.getMessage());
        }
    }
}
