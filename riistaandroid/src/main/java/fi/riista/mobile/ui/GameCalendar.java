package fi.riista.mobile.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.SpeciesCategory;

/**
 * UI element representing Game calendar for a year
 * Contains calendar month elements for each month in year
 */
public class GameCalendar extends LinearLayout {

    private static int DISPLAY_CATEGORIES = 3;

    private Context mContext = null;
    private int mStartMonth = Calendar.JANUARY;
    // data stored in order from January to December
    private ArrayList<Integer> mCalendarData;
    private ArrayList<CalendarMonth> mCalendarMonthViews = new ArrayList<CalendarMonth>();

    public GameCalendar(Context context) {
        super(context);
        inflate(context, R.layout.view_gamecalendar, this);
        mContext = context;
    }

    public GameCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_gamecalendar, this);
        mContext = context;
    }

    public GameCalendar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.view_gamecalendar, this);
        mContext = context;
    }

    /**
     * Initializes the calendar
     *
     * @param startMonth First month displayed in the calendar
     */
    public void initCalendar(int startMonth) {
        mStartMonth = startMonth;
        setWeightSum(12);
        mCalendarMonthViews.clear();
        int currentMonth = mStartMonth;
        LinearLayout monthsView = (LinearLayout) findViewById(R.id.months);
        for (int i = 0; i < 12; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
            CalendarMonth calendarMonth = new CalendarMonth(mContext);
            calendarMonth.setMonth(currentMonth, 0, 0);
            mCalendarMonthViews.add(calendarMonth);
            monthsView.addView(calendarMonth, params);
            currentMonth++;
            if (currentMonth == 12) {
                currentMonth = 0;
            }
        }
    }

    /**
     * Sets data for calendar months
     *
     * @param calendarData array of data containing values from January to December
     */
    public void setData(ArrayList<Integer> calendarData) {
        mCalendarData = calendarData;

        int currentMonth = mStartMonth;
        int monthOrdinal = 0;
        int maxLevel = Collections.max(calendarData);
        while (monthOrdinal < 12 && monthOrdinal < mCalendarMonthViews.size()) {
            CalendarMonth calendarMonth = mCalendarMonthViews.get(monthOrdinal);
            if (mCalendarData.size() > currentMonth) {
                calendarMonth.setMonth(currentMonth, mCalendarData.get(currentMonth), maxLevel);
            } else {
                calendarMonth.setMonth(currentMonth, 0, 0);
            }
            currentMonth++;
            if (currentMonth == 12) {
                currentMonth = 0;
            }
            monthOrdinal++;
        }
        mCalendarData = calendarData;
    }

    public void setCategories(SparseArray<SpeciesCategory> categories, SparseIntArray categoryData) {
        LinearLayout categoriesView = (LinearLayout) findViewById(R.id.categories);
        categoriesView.removeAllViews();
        for (int i = 0; i < SpeciesInformation.getSpeciesCategories().size() && i < DISPLAY_CATEGORIES; i++) {
            CatchCategoryItem item = new CatchCategoryItem(mContext);
            SpeciesCategory speciesCategory = categories.get(categories.keyAt(i));
            item.setContents(categoryData.get(speciesCategory.mId), speciesCategory.mName);
            item.setLayoutParams(new CatchCategoryItem.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
            if (i < DISPLAY_CATEGORIES - 1) {
                View separator = item.findViewById(R.id.divider);
                separator.setVisibility(View.VISIBLE);
            }
            categoriesView.addView(item);
        }
    }

    public void showCategories(boolean show) {
        LinearLayout categoriesView = (LinearLayout) findViewById(R.id.categories);
        categoriesView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
