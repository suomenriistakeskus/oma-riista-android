/*
 * Copyright (C) 2017 Vincit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.vincit.androidutilslib.ui;

import fi.vincit.androidutilslib.graphics.UnitConverter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * A menu that slides from the left side of the screen and moves other
 * views to to right until the menu is visible.
 */
public class SlideMenu {

    public interface OnSlideMenuItemClickListener {
        public void onMenuItemClick(int position, Object item);
    }
    
    public interface OnSlideMenuVisibilityListener {
        public void onMenuVisible(boolean visible);
    }
    
    private Activity mActivity;
    private ViewPager mPager;
    private SlideMenuView mMenuView;
    private View mContentView;
    private SlideAdapter mPagerAdapter;
    private int mDragAreaWidth;
    private int mRightMargin;
    private int mMenuWidth;
    private int mMenuMaxWidth;
    private boolean mDragEnabled = true;
    
    private OnSlideMenuItemClickListener mItemListener;
    private OnSlideMenuVisibilityListener mMenuVisibilityListener;

    public SlideMenu(Activity activity) {
        mActivity = activity;
        
        mPager = new MenuPager(mActivity);
        
        mDragAreaWidth = (int)UnitConverter.dipToPx(mActivity, 25);
        mRightMargin = (int)UnitConverter.dipToPx(mActivity, 50);
        mMenuMaxWidth = (int)UnitConverter.dipToPx(mActivity, 350);
        mMenuWidth = 0;
        
        mMenuView = new SlideMenuView(mActivity);
        mPager.addView(mMenuView);
        
        mContentView = null;
    }

    /**
     * Set the menu width explicitly overriding any limits. 
     * By default this is 0 which means that the right margin 
     * width is used to compute menu size.
     */
    public void setMenuWidth(int width) {
        checkNotAttached();
        mMenuWidth = width;
    }
    
    /**
     * Set the maximum width of the menu. Set to 0 to allow any size.
     */
    public void setMenuMaxWidth(int width) {
        checkNotAttached();
        mMenuMaxWidth = width;
    }
    
    /**
     * Set how much margin should be left at the right side of the
     * sliding menu in pixels. By default the margin is 50dp.
     */
    public void setMenuRightMargin(int margin) {
        checkNotAttached();
        mRightMargin = margin;
    }
    
    /**
     * If dragging is enabled this controls the width of the user touchable drag area
     * when the menu is not visible. This area is on the left side of the normal
     * content view and user can drag the menu out by touching this area and dragging
     * to right. 
     */
    public void setDragAreaWidth(int width) {
        mDragAreaWidth = width;
    }
    
    /**
     * Set if the user should be able to drag the menu
     * into a visible state or hide it. By default this is true.
     */
    public void setDragEnable(boolean enable) {
        mDragEnabled = enable;
    }
    
    /**
     * Returns the menu view.
     */
    public View getMenuView() {
        return mMenuView;
    }
    
    /**
     * Returns the internal menu list view.
     */
    public ListView getMenuListView() {
        return mMenuView.mListView;
    }
    
    /**
     * Set the adapter that will be used to populate the menu list view.
     */
    public void setMenuAdapter(BaseAdapter adapter) {
        mMenuView.mListView.setAdapter(adapter);
    }
    
    public void setOnSlideMenuItemClickListener(OnSlideMenuItemClickListener listener) {
        mItemListener = listener;
    }
    
    public void setOnSlideMenuVisibilityListener(OnSlideMenuVisibilityListener listener) {
        mMenuVisibilityListener = listener;
    }
    
    /**
     * Toggles the menu visibility.
     */
    public void toggleMenu() {
        int nextPosition = 0;
        if (mPager.getCurrentItem() == 0) {
            nextPosition = 1;
        }
        mPager.setCurrentItem(nextPosition, true);
    }
    
    /**
     * Shows the menu if it is not visible.
     */
    public void showMenu() {
        mPager.setCurrentItem(0, true);
    }
    
    /**
     * Hides the menu if it is visible.
     */
    public void hideMenu() {
        mPager.setCurrentItem(1, true);
    }
    
    /**
     * Returns true if the slide menu is currently visible. If the user is 
     * slowly sliding the menu into visible position this does not return true
     * until the menu has snapped in place.
     */
    public boolean isMenuVisible() {
        return mPager.getCurrentItem() == 0;
    }
    
    /**
     * Inflates an layout and attaches the menu and layout to the activity.
     */
    public void inflate(int layoutId) {
        checkNotAttached();
        
        View content = LayoutInflater.from(mActivity).inflate(layoutId, null);
        setContentView(content);

        mActivity.setContentView(mPager);
    }
    
    /**
     * Attached the menu to an activity.
     */
    public void attach() {
        checkNotAttached();
        
        View contentChild = ((ViewGroup)mActivity.findViewById(android.R.id.content)).getChildAt(0);
        ((ViewGroup)contentChild.getParent()).removeView(contentChild);
        setContentView(contentChild);

        mActivity.setContentView(mPager);
    }
    
    /**
     * Attached the menu into an existing view. The target view will be removed
     * from it's parent and moved inside the sliding menu. The sliding menu
     * will then replace the view inside the original parent. This makes it
     * possible to slide only certain parts of the activity vies, for example
     * if you have a custom toolbar etc. that you don't want to be moved when
     * the menu is visible.
     */
    public void attachToView(View view) {
        checkNotAttached();
        
        ViewGroup parent = (ViewGroup)view.getParent();
        int index = parent.indexOfChild(view);
        parent.removeViewAt(index);
 
        setContentView(view);
        
        parent.addView(mPager, index);
    }
    
    private void setContentView(View contentView) {
        mContentView = contentView;
        mPager.addView(mContentView);
        
        mPagerAdapter = new SlideAdapter();
        mPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();
        
        mPager.setCurrentItem(1, false);
    }
    
    private void checkNotAttached() {
        if (mContentView != null) {
            throw new RuntimeException("SliderMenu should not be configured after attach()");
        }
    }
    
    private boolean isMenuDragEvent(MotionEvent event) {
        if (mPager.getCurrentItem() == 0) {
            int x = (int)event.getX();
            int width = mPager.getWidth(); 
            
            int left = width - (width - mMenuView.getWidth());
            int right = width;
            if (x < left || x > right) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isContentTouchEvent(MotionEvent event) {
        if (mPager.getCurrentItem() == 1) {
            int x = (int)event.getX();
            int left = 0;
            int right = mDragAreaWidth;
            if (x < left || x > right) {
                return true;
            }
        }
        return false;
    }
    
    private boolean ignoreEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {  
            if (isMenuDragEvent(event)) {
                return true;
            }
            if (isContentTouchEvent(event)) {
                return true;
            }
        }
        return false;
    }
    
    private int getDisplayWidth() {
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        return display.getWidth();
    }
    
    private class MenuPager extends ViewPager {
        public MenuPager(Context context) {
            super(context);
            
            setOnPageChangeListener(onPageChangeListener);
        }
        
        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (mDragEnabled) {
                if (ignoreEvent(event)) {
                    return false;
                }
                return super.onInterceptTouchEvent(event);
            }
            else {
                return false;
            }
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mDragEnabled) {
                if (ignoreEvent(event)) {
                    return false;
                }
                //Remove edge flags so that we handle touches near edges.
                event.setEdgeFlags(0);
                
                return super.onTouchEvent(event);
            }
            else {
                return false;
            }
        }
        
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    
        @Override
        protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
            return super.canScroll(v, checkV, dx, x, y);
        }
        
        private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mMenuVisibilityListener != null) {
                    mMenuVisibilityListener.onMenuVisible(position == 0);
                }
            }
        };
    }
    
    private class SlideAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(View collection, int position) {
            if (position == 0) {
                return mMenuView;
            }
            else if (position == 1) {
                return mContentView;  
            }
            else {
                throw new RuntimeException("Invalid position");
            }
        }
        
        @Override
        public float getPageWidth(int position) {
            if (position == 0) {
                int displayWidth = getDisplayWidth();
                
                int totalWidth = mMenuWidth;
                if (totalWidth == 0) {
                    totalWidth = displayWidth;
                    if (mMenuMaxWidth != 0) {
                        totalWidth = Math.min(totalWidth, mMenuMaxWidth);
                    }
                }
                int viewWidth = Math.min(totalWidth, displayWidth - mRightMargin);
                return viewWidth / (float)displayWidth;
            }
            else {
                return 1.0f; 
            }
        }
        
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View)obj);
        }
    }
    
    private class SlideMenuView extends LinearLayout {
        private ListView mListView;

        public SlideMenuView(Context context) {
            super(context);
            
            mListView = new ListView(context);
            mListView.setCacheColorHint(0);
            mListView.setDividerHeight(0);
            mListView.setDivider(new ColorDrawable(Color.TRANSPARENT));
            mListView.setVerticalScrollBarEnabled(false);
            mListView.setOnItemClickListener(mListItemClickListener);
            addView(mListView);
        }
        
        private OnItemClickListener mListItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                if (mItemListener != null) {
                    Object item = adapter.getItemAtPosition(position);
                    mItemListener.onMenuItemClick(position, item);
                }
            }
        };
    }

}
