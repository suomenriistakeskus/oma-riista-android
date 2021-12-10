package fi.riista.mobile.pages;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fi.riista.mobile.R;

// TODO: Delete this and move to activity

public class ShootingTestEditPaymentActivityFragment extends Fragment {

    public ShootingTestEditPaymentActivityFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_shooting_test_edit_payment, container, false);
    }
}
