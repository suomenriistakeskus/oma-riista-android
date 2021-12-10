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

import java.io.InputStream;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.entity.ContentType;
import fi.vincit.androidutilslib.context.WorkContext;

/**
 * Task for loading a text file into a string. The resulting string will
 * be stored in memory.
 */
public abstract class TextTask extends NetworkTask
{
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final String DEFAULT_ENCODING = "UTF-8";
    
    private String mTextEncoding = null;
    private String mHeaderEncoding = null;
    private String mResult = null;
    private long mContentLength = -1;
    
    public TextTask(WorkContext context) 
    {
        this(context, null);
    }
    
    public TextTask(WorkContext context, String url) 
    {
        super(context, url);
    }
    
    /**
     * Set the text encoding for the incoming stream. If set to null
     * (the default value) the encoding is taken from response header.
     * If there is no response header UTF-8 will be used.
     */
    public void setTextEncoding(String encoding)
    {
        mTextEncoding = encoding;
    }
    
    public String getTextEncoding()
    {
        return mTextEncoding;
    }
    
    protected String streamToString(InputStream is) throws Exception
    {
        String encoding = mTextEncoding;
        if (encoding == null) {
            encoding = mHeaderEncoding;
            if (encoding == null) {
                // Use the default encoding. This is not very optimal, but there its not
                // much that can be done about it.
                encoding = DEFAULT_ENCODING;    
            }
        }
        
        // Try to preallocate the string builder. It is ok if this is huge, we will
        // probably get an out of memory exception and fail gracefully.
        int sizeHint = (int)mContentLength;
        if (sizeHint < 0) {
            sizeHint = 1024;
        }
        
        char[] buffer = new char[BUFFER_SIZE];
        InputStreamReader reader = new InputStreamReader(is, encoding);
        StringBuilder result = new StringBuilder(sizeHint);
        
        int count = 0;
        while ((count = reader.read(buffer, 0, buffer.length)) >= 0) {
            result.append(buffer, 0, count);
        }
        return result.toString();
    }

    @Override
    protected void onAsyncResponse(HttpResponse response)
    {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            mContentLength = entity.getContentLength();
            
            ContentType type = ContentType.get(entity);
            if (type != null && type.getCharset() != null) {
                mHeaderEncoding = type.getCharset().name();
            }
        }
    }
    
    @Override
    protected final void onAsyncStream(InputStream stream) throws Exception
    {
        mResult = streamToString(stream);
    }

    @Override
    protected final void onFinish()
    {
        onFinishText(mResult);
        mResult = null;
    }
    
    /**
     * Called in the UI thread if the text content has been successfully read.
     */
    protected abstract void onFinishText(String text);
    
}
