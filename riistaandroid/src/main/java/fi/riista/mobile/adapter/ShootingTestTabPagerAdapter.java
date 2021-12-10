package fi.riista.mobile.adapter;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import fi.riista.mobile.R;
import fi.riista.mobile.pages.ShootingTestEventFragment;
import fi.riista.mobile.pages.ShootingTestPaymentsFragment;
import fi.riista.mobile.pages.ShootingTestQueueFragment;
import fi.riista.mobile.pages.ShootingTestRegisterFragment;

public class ShootingTestTabPagerAdapter extends FragmentStatePagerAdapter {

    public static final int TAB_INDEX_EVENT = 0;
    public static final int TAB_INDEX_REGISTER = 1;
    public static final int TAB_INDEX_QUEUE = 2;
    public static final int TAB_INDEX_PAYMENTS = 3;

    private static final int TAB_COUNT = 4;

    private Context mContext;

    public ShootingTestTabPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TAB_INDEX_EVENT:
                return ShootingTestEventFragment.newInstance();
            case TAB_INDEX_REGISTER:
                return ShootingTestRegisterFragment.newInstance();
            case TAB_INDEX_QUEUE:
                return ShootingTestQueueFragment.newInstance();
            case TAB_INDEX_PAYMENTS:
                return ShootingTestPaymentsFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case TAB_INDEX_EVENT:
                return mContext.getString(R.string.shooting_test_tab_event);
            case TAB_INDEX_REGISTER:
                return mContext.getString(R.string.shooting_test_tab_register);
            case TAB_INDEX_QUEUE:
                return mContext.getString(R.string.shooting_test_tab_queue);
            case TAB_INDEX_PAYMENTS:
                return mContext.getString(R.string.shooting_test_tab_payments);
        }
        return super.getPageTitle(position);
    }
}
