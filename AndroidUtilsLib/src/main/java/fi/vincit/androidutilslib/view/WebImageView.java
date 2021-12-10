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

import java.util.concurrent.Executor;

import fi.vincit.androidutilslib.R;
import fi.vincit.androidutilslib.config.AndroidUtilsLibConfig;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.context.WorkContextProvider;
import fi.vincit.androidutilslib.network.SynchronizedCookieStore;
import fi.vincit.androidutilslib.task.ImageTask;
import fi.vincit.androidutilslib.util.StopWatch;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.collection.LruCache;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListAdapter;

/**
 * An image view which can load images asynchronously. It uses
 * NetworkTask behind the scenes and supports same special
 * schemas. It also automatically caches bitmaps.
 * <p>
 * Note that the Activity this view belongs to must
 * implement WorkContextProvider.
 */
public class WebImageView extends ImageView
{
    /**
     * LRU cache for bitmaps. These are never freed, but if the configuration is sane
     * it should not take that much space.
     */
    private static LruCache<String, Bitmap> sBitmapCache = null;
    
    private static LruCache<String, Bitmap> createBitmapCache() 
    {
        int maxSize = AndroidUtilsLibConfig.Cache.Bitmap.DEFAULT_BITMAP_CACHE_SIZE;
        if (maxSize > 0) {
            return new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return getBitmapSizeBytes(value);
               }
            };
        }
        return null;
    }
    
    private static int getBitmapSizeBytes(Bitmap bitmap) 
    {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
    
    private static boolean canBeCached(Bitmap bitmap) 
    {
        return getBitmapSizeBytes(bitmap) < AndroidUtilsLibConfig.Cache.Bitmap.DEFAULT_MAX_BITMAP_SIZE;
    }
    
    /**
     * Clears the internal bitmap cache. Usually there is no need
     * to call this unless you really want to clear it.
     */
    public static void clearBitmapCache() 
    {
        if (sBitmapCache != null) {
            sBitmapCache.evictAll();
        }
    }
    
    private ImageTask mImageTask;
    private boolean mAnimateFadeIn = false;
    private int mLoadingResource = 0;
    private int mTargetImageSize = -1;
    
    private Animation mFadeInAnimation;
    private String mLoadingUri;
    private String mLoadedUri;
    private long taskCounter = 0;

    private SynchronizedCookieStore mCookieStore = null;
    
    public WebImageView(Context context) 
    {
        super(context);
        init(null, 0);
    }
    
    public WebImageView(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
        init(attrs, 0);
    }
    
    public WebImageView(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    
    private void init(AttributeSet attrs, int defStyle)
    {
        if (sBitmapCache == null) {
            sBitmapCache = createBitmapCache();
        }
        
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WebImageView, defStyle, 0);
            boolean animateFadeIn = a.getBoolean(R.styleable.WebImageView_animateFadeIn, mAnimateFadeIn);
            mLoadingResource = a.getResourceId(R.styleable.WebImageView_loadingResource, 0);
            String uri = a.getString(R.styleable.WebImageView_imageUri);
            mTargetImageSize = a.getDimensionPixelSize(R.styleable.WebImageView_targetImageSize, -1);
            a.recycle();
            
            setAnimateFadeIn(animateFadeIn);
            
            if (uri != null) {
                setImageURI(uri);
            } 
        }
    }
    
    /**
     * Enables the fade-in when image has been loaded.
     */
    public void setAnimateFadeIn(boolean animateFadeIn) 
    {
        mAnimateFadeIn = animateFadeIn;
        
        if (mAnimateFadeIn && mFadeInAnimation == null) {
            mFadeInAnimation = AnimationUtils.loadAnimation(
                    getContext().getApplicationContext(), 
                    R.anim.fade_in); 
        }
    }
    
    public boolean isAnimateFadeIn() 
    {
        return mAnimateFadeIn;
    }
    
    /**
     * Set the drawable resource which will be set to the
     * image when image loading starts. If loading succeeds it
     * will then be replaced with the loaded image.
     */
    public void setLoadingResource(int resId)
    {
        mLoadingResource = resId;
    }
    
    public int getLoadingResource() 
    {
        return mLoadingResource;
    }
    
    /**
     * Set the target image size. If you use bitmap cache
     * the first WebImageView that loads the bitmap will scale it
     * for others to use according to it's settings. So if you use
     * the same image in a thumbnail and some larger detail image
     * the one that loads it first will determine it's size which
     * might cause some surprises.
     * 
     * @see ImageTask#setTargetImageSize
     */
    public void setTargetImageSize(int targetImageSize) {
        mTargetImageSize = targetImageSize;
    }
    
    public int getTargetImageSize() {
        return mTargetImageSize;
    }

    private void cancelTask()
    {
        mLoadingUri = null;
        if (mImageTask != null) {
            mImageTask.cancel();
            mImageTask = null;
        }
        clearAnimation();
    }
    
    @Override
    public void setImageResource(int resourceId)
    {
        mLoadedUri = null;
        cancelTask();
        super.setImageResource(resourceId);
    }
    
    @Override
    public void setImageBitmap(Bitmap bitmap)
    {
        mLoadedUri = null;
        cancelTask();
        super.setImageBitmap(bitmap);
    }
    
    private void setImageBitmapInternal(Bitmap bitmap)
    {
        super.setImageBitmap(bitmap);
        
        setVisibility(View.VISIBLE);
        
        clearAnimation();
        if (mAnimateFadeIn && mFadeInAnimation != null) {
            startAnimation(mFadeInAnimation);
        }
    }
    
    @Override
    public void setImageDrawable(Drawable drawable)
    {
        mLoadedUri = null;
        cancelTask();
        super.setImageDrawable(drawable);
    }
    
    @Override
    public void setVisibility(int visibility) 
    {
        super.setVisibility(visibility);
        
        if (visibility == View.INVISIBLE || visibility == View.GONE) {
            cancelTask();
        }
    }
    
    private boolean trySetBitmapFromCache(String uri)
    {
        if (sBitmapCache != null) {
            Bitmap bitmap = sBitmapCache.get(uri);
            if (bitmap != null) {
                //Got it from cache.
                taskCounter++;
                setImageBitmapInternal(bitmap);
                mLoadingUri = null;
                mLoadedUri = uri;
                return true;
            } 
        }
        return false;
    }
    
    /**
     * Sets the content of this ImageView to the specified Uri. 
     * This starts an asynchronous task which will load the image.
     * All special schemas used by NetworkTask are supported.
     */
    @Override
    public void setImageURI(Uri uri)
    {
        String uriStr = uri.toString();
        
        if (trySetBitmapFromCache(uriStr)) {
            return;
        }

        if (uriStr.equals(mLoadingUri) || uriStr.equals(mLoadedUri)) {
            //Already being loaded or set, don't do anything.
            return;
        }
        
        cancelTask();
        
        if (mLoadingResource != 0) {
            setImageResource(mLoadingResource);
            setVisibility(View.VISIBLE);
        }
        else {
            //Stays hidden if image task fails.
            setVisibility(View.INVISIBLE);
        }
        
        mLoadingUri = uriStr;
        taskCounter++;
        
        mImageTask = createImageTask(uriStr, taskCounter);
        mImageTask.setTargetImageSize(mTargetImageSize);
        mImageTask.start();
    }
    
    public void setImageURI(String uri)
    {
        setImageURI(Uri.parse(uri));
    }
    
    private ImageTask createImageTask(final String url, final long counter)
    {
        WorkContext workContext = ((WorkContextProvider)getContext()).getWorkContext();
        
        ImageTask task = new ImageTask(workContext, url) {
            @Override
            protected void onFinishImage(Bitmap image)
            {
                if (sBitmapCache != null && canBeCached(image)) {
                    sBitmapCache.put(url, image);
                }
                
                if (counter != taskCounter) {
                    //We are late, some other task has been started after us.
                    return;
                }
                mLoadedUri = url;
                
                setImageBitmapInternal(image);
            }
            
            @Override
            protected void onEnd()
            {
                if (counter == taskCounter) {
                    mLoadingUri = null; 
                }
            }
        };
        if (mCookieStore != null)
            task.setCookieStore(mCookieStore);
        task.setCacheEnabled(true);
        return task;
    }

    public void setCookieStore(SynchronizedCookieStore store) {
        mCookieStore = store;
    }
}
