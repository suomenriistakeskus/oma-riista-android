package fi.riista.mobile.ui;

import android.content.Context;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import fi.riista.mobile.R;

class CalendarMonth extends LinearLayout {

    private Context mContext;
    // Available space for a bar in DP
    private int mAvailableSpace = 65;

    public CalendarMonth(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CalendarMonth(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public CalendarMonth(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.view_calendar_month, this);
    }

    /**
     * Set monthly catches item content.
     * Scales the number of bars based on max level.
     *
     * @param month    Month ordinal
     * @param amount   Amount for this month
     * @param maxLevel Maximum amount for all months
     */
    public void setMonth(int month, int amount, int maxLevel) {
        TextView amountView = (TextView) findViewById(R.id.amount);
        if (amount > 0) {
            amountView.setVisibility(View.VISIBLE);
            amountView.setText(Integer.valueOf(amount).toString());
        } else {
            amountView.setVisibility(View.GONE);
        }
        BitmapDrawable tile;
        tile = (BitmapDrawable) getResources().getDrawable(R.drawable.bar);

        tile.setTileModeY(TileMode.REPEAT);
        int bitmapHeight = tile.getBitmap().getHeight();
        int maxBarsAmount = (int) (mAvailableSpace * getResources().getDisplayMetrics().density) / bitmapHeight;
        int barCount = GetNumberOfBars(amount, maxLevel, maxBarsAmount);
        View bars = findViewById(R.id.bars);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bars.getLayoutParams();
        if (barCount > 0) {
            if (barCount > maxBarsAmount) {
                params.height = maxBarsAmount * bitmapHeight;
            } else {
                params.height = barCount * bitmapHeight;
            }
        } else {
            // 2dp
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        }
        bars.setLayoutParams(params);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            bars.setBackground(tile);
        } else {
            bars.setBackgroundDrawable(tile);
        }

        TextView nameView = (TextView) findViewById(R.id.month_name);
        String shortName = getResources().getStringArray(R.array.months)[month].substring(0, 3).toUpperCase();
        nameView.setText(shortName);
    }

    private int GetNumberOfBars(int amount, int maxLevel, int maxBarCount) {
        if (maxLevel <= maxBarCount) {
            return amount;
        }

        // Number rounded up
        double count = (maxBarCount * (double) amount) / (double) maxLevel;
        return (int) Math.ceil(count);
    }
}
