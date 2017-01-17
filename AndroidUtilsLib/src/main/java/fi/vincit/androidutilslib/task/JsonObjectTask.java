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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vincit.androidutilslib.context.WorkContext;

/**
 * Task for parsing a single Json-object into a Java-object.
 */
public abstract class JsonObjectTask<T> extends JsonTask {
    private Class<T> mClass;
    private T mResult;
    
    public JsonObjectTask(WorkContext context, Class<T> klass) 
    {
        this(context, null, klass);
    }
    
    public JsonObjectTask(WorkContext context, String url, Class<T> klass) 
    {
        super(context, url);
        
        mClass = klass;
    }

    @Override
    protected final void onAsyncStream(InputStream stream) throws Exception
    {
        mResult = getObjectMapper().readValue(stream, mClass);
        if (mResult == null) {
            throw new Exception("Can't parse Json into an object");
        }
    }
    
    @Override
    protected final void onFinish()
    {
        onFinishObject(mResult);
    }

    /**
     * Called in the UI thread if the object has been successfully serialized.
     */
    protected abstract void onFinishObject(T result);
}
