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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import fi.vincit.androidutilslib.context.WorkContext;

/**
 * Task for reliably downloading large files in pieces. To do this the server and the
 * target url must support range requests. The task divides the target file into multiple 
 * smaller requests and attempts to download and combine them into a file. It also 
 * automatically retries individual requests a few times if they fail. This task is
 * relatively strict so if you have problems with some servers it might be possible to 
 * relax the behavior somewhat.
 * <p>
 * If your files are small it is recommended to use DownloadTask which is more general,
 * faster and does not require range support from the server.
 * <p>
 * Note that unlike other tasks calls to onFinish(), onError() and onCancel() should be ignored (same
 * applies to any task listeners). They are called when the first HEAD request finishes
 * or fails but not when the actual background download finishes or fails. Use onFinishDownload(), 
 * onErrorDownload() and onCancelDownload() instead. For the same reason you probably don't want
 * to use built in task UI interaction methods like setProgressDialog() etc.
 */
public abstract class DownloadRangedTask extends NetworkTask {

    private static final int BUFFER_SIZE = 1024 * 8; // In bytes
    private static final int DEFAULT_RANGE_SIZE = 1024 * 256; // In bytes
    
    private File mOutFile;
    private BufferedOutputStream mOutStream;
    
    private String mEtag = "";
    private String mLastModified = "";
    private int mContentLength = -1;
    private long mBytesWritten = 0;
    private int mRangeSize = DEFAULT_RANGE_SIZE;
    private int mMaxRetries = 5;
    private int mRetryCount = 0;
    private RangedDownloadTask mRangeTask;
    
    public DownloadRangedTask(WorkContext workContext, String outputFile) 
    {
        super(workContext);
        initDownloadSettings(getFileFromUrl(outputFile));
    }
    
    public DownloadRangedTask(WorkContext workContext, File outputFile) 
    {
        super(workContext);
        initDownloadSettings(outputFile);
    }
    
    private void initDownloadSettings(File output)
    {
        setHttpMethod(HttpMethod.HEAD);
        setUseCompression(false);
        
        mOutFile = output;
    }
    
    /**
     * Returns a File pointing to the downloaded file. Can be null.
     */
    public File getFile()
    {
        return mOutFile;
    }
    
    /**
     * Set the maximum size of the individual range requests in bytes. It
     * will be stored in memory so don't set it too large.
     */
    public void setRangeSize(int size)
    {
        mRangeSize = size;
    }
    
    public int getRangeSize()
    {
        return mRangeSize;
    }
    
    /**
     * Set how many times range downloads can be retried if they fail.
     * This is the total amount. Don't confuse this with setTryCount().
     */
    public void setMaxRetries(int count)
    {
        mMaxRetries = count;
    }
    
    public int getMaxRetries()
    {
        return mMaxRetries;
    }

    @Override
    public void cancel()
    {
        if (mRangeTask != null) {
            mRangeTask.cancel();
            mRangeTask = null;
        }
        super.cancel();
    }
    
    private static String getResponseHeaderString(HttpResponse response, String name)
    {
        Header header = response.getFirstHeader(name);
        if (header != null) {
            return "" + header.getValue();
        }
        return "";
    }
    
    private int getResponseContentLength() 
    {
        String contentLength = getResponseHeaderString(getHttpResponse(), "Content-Length");
        try {
            return Integer.parseInt(contentLength);
        }
        catch (NumberFormatException e) {
            
        }
        return -1;
    }
    
    @Override
    protected final void onAsyncStream(InputStream stream) throws Exception 
    {
        // We don't get a body in HEAD requests.
    }
    
    @Override
    protected final void onFinish() 
    {
        try {
            createOutputFile();
            
            checkRequestSupport();

            Range range = new Range();
            range.start = 0;
            range.end = mRangeSize;
            range.size = mRangeSize;
            startRangeDownload(range);
        }
        catch (Exception e) {
            handleError(e);
        }
    }
    
    @Override
    protected final void onError() 
    {
        handleError(getError());
    }
    
    @Override
    protected final void onCancel() 
    {
        handleCancel();
    }
    
    private void createOutputFile() throws Exception
    {
        mOutFile.delete();

        File parent = mOutFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        mOutStream = new BufferedOutputStream(new FileOutputStream(mOutFile), BUFFER_SIZE);
    }
    
    private void checkRequestSupport() throws Exception
    {
        HttpResponse response = getHttpResponse();
        mEtag = getResponseHeaderString(response, HeaderConstants.ETAG);
        mLastModified = getResponseHeaderString(response, HeaderConstants.LAST_MODIFIED);
        mContentLength = getResponseContentLength();
        String acceptRanges = getResponseHeaderString(response, "Accept-Ranges");
        
        if (!acceptRanges.toLowerCase(Locale.ENGLISH).equals("bytes")) {
            throw new Exception("Server does not accept byte range request");
        }
        if (mContentLength <= 0) {
            throw new Exception("Invalid content length: " + mContentLength);
        }
    }
    
    private void cleanupFile()
    {
        if (mOutStream != null) {
            try {
                mOutStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            mOutStream = null;
        }
        
        if (mOutFile != null) {
            mOutFile.delete();
        }
    }
    
    private void handleFinish()
    {
        try {
            mOutStream.close();
            
            if (mBytesWritten != mContentLength) {
                throw new IOException("Bytes written " + mBytesWritten + ", should be " + mContentLength);
            }
            
            long fileSize = mOutFile.length();
            if (fileSize != mBytesWritten) {
                throw new IOException("File size " + fileSize + ", should be " + mBytesWritten);
            }

            onFinishDownload();
        } 
        catch (IOException e) {
            handleError(e);
        }
    }
    
    private void handleError(Exception error)
    {
        cleanupFile();
        
        setError(error);
        onErrorDownload();
    }
    
    private void handleCancel()
    {
        cleanupFile();
        
        onCancelDownload();
    }
    
    private static class Range
    {
        public int start;
        public int end;
        public int size;
        
        public int length()
        {
            // Range end is inclusive
            return (end - start) + 1;
        }
        
        public Range next()
        {
            Range r = new Range();
            r.start = start + length();
            r.end = r.start + size;
            r.size = size;
            return r;
        }
        
        public String getRangeHeaderValue()
        {
            return "bytes=" + start + "-" + end;
        }
    }
    
    private void startRangeDownload(Range range) 
    {
        range.end = Math.min(range.end, mContentLength - 1);
        
        if (isCancelled()) {
            handleCancel();
        }
        else if (range.length() < 1) {
            // No more data to download.
            handleFinish();
        }
        else {
            String rangeValue = range.getRangeHeaderValue();
            
            log("Range: " + rangeValue + " of " + mContentLength);
            
            mRangeTask = new RangedDownloadTask(getWorkContext(), range);
            mRangeTask.setBaseUrl(getBaseUrl());
            mRangeTask.setHeader("Range", rangeValue);
            if (mEtag.length() > 0) {
                mRangeTask.setHeader("If-Range", mEtag);
            }
            else if (mLastModified.length() > 0) {
                mRangeTask.setHeader("If-Range", mLastModified);
            }
            mRangeTask.startParallel();
        }
    }
    
    private class RangedDownloadTask extends NetworkTask 
    {
        private Range mRange;
        private boolean mStop; // If we encounter serious issues don't retry.
        
        public RangedDownloadTask(WorkContext workContext, Range range) 
        {
            super(workContext);
            
            mRange = range;
            mStop = false;
        }
        
        private void validateResponse(HttpResponse response) throws Exception 
        {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 206) {
                throw new Exception("Got status code " + statusCode + ", expected 206");
            }
            if (!getResponseHeaderString(response, HeaderConstants.ETAG).equals(mEtag)) {
                throw new Exception("ETag changed during download");
            }
            if (!getResponseHeaderString(response, HeaderConstants.LAST_MODIFIED).equals(mLastModified)) {
                throw new Exception("Last-Modified changed during download");
            }
            String expectedRange = ("bytes " + mRange.start + "-" + mRange.end + "/" + mContentLength).toLowerCase(Locale.ENGLISH);
            String responseRange = getResponseHeaderString(response, "Content-Range").toLowerCase(Locale.ENGLISH);
            if (!responseRange.startsWith(expectedRange)) {
                throw new Exception("Server response range does not match request range");
            }
        }

        @Override
        protected void onAsyncStream(InputStream stream) throws Exception 
        {
            try {
                validateResponse(getHttpResponse());
            }
            catch (Exception e) {
                // Ranged download is not going to work with the server response, give up.
                mStop = true;
                throw e;
            }
            
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(mRangeSize);
            byte[] buffer = new byte[BUFFER_SIZE];
            int total = 0;
            int count = 0;
            while ((count = stream.read(buffer, 0, buffer.length)) >= 0) {
                byteStream.write(buffer, 0, count);
                total += count;
            }
            
            if (total != mRange.length()) {
                throw new Exception("Got " + total + " bytes, expected " + mRange.length());
            }
            
            mBytesWritten += total;
            try {
                byteStream.writeTo(mOutStream);
            }
            catch (Exception e) {
                // Writing to disk failed, give up.
                mStop = true;
                throw e;
            }

            // Forward the progress to the parent task.
            int progress = progressToPercent(mBytesWritten, mContentLength);
            DownloadRangedTask.this.setInternalProgress(progress);
        }
        
        @Override
        protected final void onFinish() 
        {
            startRangeDownload(mRange.next());
        }
        
        @Override
        protected final void onError() 
        {
            if (mRetryCount <= mMaxRetries && !mStop) {
                // Try this range again.
                mRetryCount++;
                startRangeDownload(mRange);
            }
            else {
                handleError(getError()); 
            }
        }
        
        @Override
        protected final void onCancel() 
        {
            handleCancel();
        }
    };

    protected abstract void onFinishDownload();
    
    protected abstract void onErrorDownload();
    
    protected abstract void onCancelDownload();
}
