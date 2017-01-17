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
package fi.vincit.androidutilslib.view;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * ViewPager that automatically adds child views as pages when inflated. This view should only
 * used inside .xml layout files as there is not much point in creating this in code. All
 * direct child views that should be added to the pager should have a string tag. 
 * The tag will be also set as the page title if the pager has a indicator.
 */
public class LayoutViewPager extends ViewPager {
    
    private static class PageItem {
        public View view;
        public String title;
    }
    
    private ArrayList<PageItem> mPages = new ArrayList<PageItem>();
    private LayoutPagerAdapter mAdapter = new LayoutPagerAdapter();
    
    public LayoutViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setOffscreenPageLimit(100); //Not very relevant in our use case.
        setAdapter(mAdapter);
    }

    /**
     * Returns how many pages (or views) the pager contains.
     */
    public int getPageCount() {
        return mPages.size();
    }
    
    /**
     * Returns the page view at the given index.
     */
    public View getPageAt(int index) {
        return mPages.get(index).view;
    }

    /**
     * Returns the page title at the given index.
     */
    public String getPageTitleAt(int index) {
        return mPages.get(index).title;
    }
    
    /**
     * Change the page title for the page at the given index.
     */
    public void setPageTitleAt(int index, String title) {
        mPages.get(index).title = title;
        
        mAdapter.notifyDataSetChanged();
    }
   
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mPages.clear();
        
        int count = getChildCount();
        for (int i=0; i < count; ++i) {
            View child = getChildAt(i);
            Object tag = child.getTag();
            if (tag != null) {
                PageItem item = new PageItem();
                item.view = child;
                item.title = "" + tag;
                mPages.add(item);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private class LayoutPagerAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(View collection, int position) {
            return mPages.get(position).view;
        }
        
        @Override
        public void destroyItem(ViewGroup group, int position, Object obj) {
            //Do nothing.
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPages.get(position).title;
        }
        
        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View)arg1);
        }
    }
    
}
