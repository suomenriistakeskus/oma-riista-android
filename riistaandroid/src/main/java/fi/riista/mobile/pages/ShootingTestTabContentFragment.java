package fi.riista.mobile.pages;

import android.content.Context;
import androidx.fragment.app.Fragment;

import fi.riista.mobile.activity.ShootingTestMainActivity;

public abstract class ShootingTestTabContentFragment extends Fragment {

    protected ShootingTestMainActivity mActivity;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        mActivity = (ShootingTestMainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    public abstract void onTabSelected();

    public void onTabDeselected() {
    }
}
