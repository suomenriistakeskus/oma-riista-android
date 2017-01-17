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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import android.os.Environment;

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.httpclientandroidlib.HttpResponse;

/**
 * Task for downloading (possibly large) files into internal or external storage.
 * Both the source and output url's support some special schemas provided
 * by the NetworkTask, for example internal:// and external://
 * <p>
 * If your files are very large or you have a bad or slow connection you
 * can use DownloadRangedTask. It is more resource intensive and slower but
 * much more reliable.
 * <p>
 * Using these schemas it is possible to actually copy local files and in fact this
 * a good way to do it as it also gives good asynchronous support, progress notifications etc.
 * <p>
 * For example DownloadTask(context, "internal://stuff/text.txt", "external://myapp/text.txt") copies
 * a file from application's internal storage to the external storage. If the target directory or
 * file does not exist it will be created. Existing files will be overwritten.
 */
public class DownloadTask extends NetworkTask
{
    private static final int BUFFER_SIZE = 1024 * 8;
    
    private File mPath;
    
    public DownloadTask(WorkContext workContext, String outputFile) 
    {
        this(workContext, null, outputFile);
    }
    
    public DownloadTask(WorkContext workContext, String baseUrl, String outputFile) 
    {
        super(workContext, baseUrl);
        mPath = getFileFromUrl(outputFile);
    }

    public DownloadTask(WorkContext workContext, File outputFile) 
    {
        this(workContext, null, outputFile);
    }
    
    public DownloadTask(WorkContext workContext, String baseUrl, File outputFile) 
    {
        super(workContext, baseUrl);
        mPath = outputFile;
    }
    
    /**
     * Returns a File pointing to the downloaded file. Can be null.
     */
    public File getFile()
    {
        return mPath;
    }
    
    /**
     * Attempts to delete the downloaded file. This can be called after the task has
     * ended or in the notification callbacks. Returns true if successful.
     */
    public boolean deleteFile()
    {
        if (mPath != null) {
            return mPath.delete();
        }
        return false;
    }
    
    @Override
    protected final void onAsyncStream(InputStream stream) throws Exception 
    {
        if (mPath == null) {
            throw new Exception("Invalid output path");
        }
        mPath.delete();
        
        File parent = mPath.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        
        BufferedOutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(new FileOutputStream(mPath), BUFFER_SIZE);
            byte[] buffer = new byte[BUFFER_SIZE];
            long count = IOUtils.copyLarge(stream, outStream, buffer);
            
            long contentSize = getHttpResponse().getEntity().getContentLength();
            if (contentSize > 0 && count != contentSize) {
                throw new IOException("Got " + count + " bytes, expected " + contentSize);
            }
        }
        catch (Exception e) {
            //Delete the file, it might have corrupt data.
            mPath.delete();

            throw e;
        }
        finally {
            IOUtils.closeQuietly(outStream);
        }
    }
}
