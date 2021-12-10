package fi.riista.mobile.ui;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ShootingTestTabViewPager extends ViewPager {

    private boolean enableSwipe;

    public ShootingTestTabViewPager(Context context) {
        super(context);
        init();
    }

    public ShootingTestTabViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        enableSwipe = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return enableSwipe && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return enableSwipe && super.onTouchEvent(event);

    }

    public void setEnableSwipe(boolean enableSwipe) {
        this.enableSwipe = enableSwipe;
    }
}