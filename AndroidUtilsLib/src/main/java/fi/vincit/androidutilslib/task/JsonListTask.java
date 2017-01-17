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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import fi.vincit.androidutilslib.context.WorkContext;

/**
 * Task for parsing a list of Json-objects into a list of Java-objects.
 */
public abstract class JsonListTask<T> extends JsonTask {
    private Class<T> mClass;
    private List<T> mResults;
    
    public JsonListTask(WorkContext context, Class<T> klass) 
    {
        this(context, null, klass);
    }
    
    public JsonListTask(WorkContext context, String url, Class<T> klass) 
    {
        super(context, url);
        
        mClass = klass;
    }

    @Override
    protected final void onAsyncStream(InputStream stream) throws Exception 
    {
        ObjectMapper mapper = getObjectMapper();
        
        CollectionType ct = mapper.getTypeFactory().constructCollectionType(ArrayList.class, mClass);
        mResults = mapper.readValue(stream, ct);
        if (mResults == null) {
            throw new Exception("Can't parse Json into a list");
        }
    }
    
    @Override
    protected final void onFinish()
    {
        onFinishObjects(mResults);
    }

    /**
     * Called in the UI thread if the objects have been successfully serialized.
     */
    protected abstract void onFinishObjects(List<T> results);
    
}
