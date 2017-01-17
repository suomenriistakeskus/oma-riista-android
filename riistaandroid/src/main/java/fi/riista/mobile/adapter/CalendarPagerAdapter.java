package fi.riista.mobile.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import fi.riista.mobile.ui.GameCalendar;

public class CalendarPagerAdapter extends PagerAdapter {

    private CalendarSource mSource = null;
    private Context mContext = null;
    private boolean mUpdate = false;

    public CalendarPagerAdapter(CalendarSource source, Context context) {
        mSource = source;
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {
        GameCalendar calendar = new GameCalendar(mContext);
        calendar.initCalendar(mSource.getStartMonth());
        mSource.setupCalendar(calendar, position);
        viewGroup.addView(calendar);
        return calendar;
    }

    @Override
    public int getItemPosition(Object object) {
        if (mUpdate) {
            return POSITION_NONE;
        }

        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(ViewGroup viewGroup, int position, Object object) {
        viewGroup.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mSource.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void forceUpdate() {
        mUpdate = true;
        notifyDataSetChanged();
        mUpdate = false;
    }

    public interface CalendarSource {
        void setupCalendar(GameCalendar calendar, int position);

        int getCount();

        int getStartMonth();
    }
}
