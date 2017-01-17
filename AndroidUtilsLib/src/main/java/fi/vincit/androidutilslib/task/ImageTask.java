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
package fi.vincit.androidutilslib.task;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import fi.vincit.androidutilslib.config.AndroidUtilsLibConfig;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.network.HttpLruCacheStorage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Task for loading a bitmap image.
 */
public abstract class ImageTask extends NetworkTask
{
    private static final int BUFFER_SIZE = 1024 * 8;
    
    private Bitmap mBitmap;
    private BitmapFactory.Options mOptions = createDefaultBitmapOptions();
    private int mTargetImageWidth = -1;
    private int mTargetImageHeight = -1;
    
    public static BitmapFactory.Options createDefaultBitmapOptions()
    {
        BitmapFactory.Options options = new BitmapFactory.Options(); 
        //Assume high density images. Providing alternative url-images for
        //different densities is quite rare.
        options.inDensity = DisplayMetrics.DENSITY_HIGH;
        options.inScaled = false;
        return options;
    }
  
    public ImageTask(WorkContext context) 
    {
        this(context, null);
    }
    
    public ImageTask(WorkContext context, String url) 
    {
        super(context, url);

        //Most image formats are already compressed.
        setUseCompression(false);
        
        //Cache small images. Thumbnails in a ListView if a very common case.
        setCacheEnabled(true);
        setCacheLifeTime(AndroidUtilsLibConfig.Cache.DEFAULT_CACHE_LIFETIME_SECONDS * 1000);
        
        //Because we read the data to an array we might as well make it sharable as
        //we are just going to throw it away otherwise.
        mOptions.inInputShareable = true;
        mOptions.inPurgeable = true;
    }
    
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) 
    {
        //Raw width and height of image
        final int width = options.outWidth;
        final int height = options.outHeight;

        if (reqWidth == -1) {
            reqWidth = width;
        }
        if (reqHeight == -1) {
            reqHeight = height;
        }
        
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            //Calculate ratios of width and height to requested width and height
            final int widthRatio = Math.round((float)width / (float)reqWidth);
            final int heightRatio = Math.round((float)height / (float)reqHeight);

            //Choose the smallest ratio as inSampleSize value, this will guarantee
            //a final image with both dimensions larger than or equal to the
            //requested height and width.
            inSampleSize = (heightRatio < widthRatio) ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
    
    /**
     * Returns the options used to decode the image. This object can
     * be modified before calling start().
     */
    public BitmapFactory.Options getOptions()
    {
        return mOptions;
    }
    
    /**
     * Set the target minimum width and height of the result bitmap with one call.
     * If the width or height of the bitmap is larger than the set target dimension
     * the bitmap will be scaled down near the target size if possible. It is also
     * possible that the bitmap will not be scaled at all if it is near enough
     * the target size (scaling steps are power of two's). Bitmaps are never scaled up.
     * Use -1 to allow any size and disable all scaling (this is the default).
     */
    public void setTargetImageSize(int targetSize)
    {
        setTargetImageWidth(targetSize);
        setTargetImageHeight(targetSize);
    }

    /**
     * @see #setTargetImageSize
     */
    public void setTargetImageWidth(int targetWidth) 
    {
        mTargetImageWidth = targetWidth;
    }

    public int getTargetImageWidth() 
    {
        return mTargetImageWidth;
    }
    
    /**
     * @see #setTargetImageSize
     */
    public void setTargetImageHeight(int targetHeight) 
    {
        mTargetImageHeight = targetHeight;
    }
    
    public int getTargetImageHeight() 
    {
        return mTargetImageHeight;
    }

    @Override
    public void cancel()
    {
        mOptions.requestCancelDecode();
        
        super.cancel();
    }

    private byte[] readStream(InputStream stream) throws Exception
    {
        int sizeHint = 0;
        if (getHttpResponse() != null) {
            sizeHint = (int)(getHttpResponse().getEntity().getContentLength());
        }
        sizeHint = Math.max(sizeHint, BUFFER_SIZE);

        ByteArrayOutputStream output = new ByteArrayOutputStream(sizeHint);
        byte[] buffer = new byte[BUFFER_SIZE];
        int count = 0;
        while ((count = stream.read(buffer, 0, buffer.length)) >= 0) {
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }
    
    private Bitmap decodeImageData(byte[] imageData) throws Exception
    {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bounds);
        if (bounds.outWidth == -1 || bounds.outHeight == -1) {
            throw new Exception("Can't decode image data bounds");
        }
        mOptions.inSampleSize = calculateInSampleSize(bounds, mTargetImageWidth, mTargetImageHeight);
        
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, mOptions);
    }
    
    @Override
    protected final void onAsyncStream(InputStream stream) throws Exception 
    {
        try {
            byte[] imageData = readStream(stream);
            mBitmap = decodeImageData(imageData);
        }
        catch (OutOfMemoryError e) {
            throw new Exception("Out of memory while loading a bitmap from: " + getFullUrl(), e);
        }
        
        if (mBitmap == null) {
            if (mOptions.mCancel || isCancelled()) {
                throw new Exception("Cancelled bitmap decode from: " + getFullUrl());
            }
            else {
                throw new Exception("Can't decode bitmap from: " + getFullUrl());
            }
        }
    }
    
    @Override
    protected final void onFinish()
    {
        onFinishImage(mBitmap);
        mBitmap = null;
    }

    /**
     * Called in the UI thread if the bitmap has been successfully decoded.
     */
    protected abstract void onFinishImage(Bitmap image);
    
}
