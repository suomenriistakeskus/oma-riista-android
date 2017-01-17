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

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.util.XmlInfo;

import fi.vincit.androidutilslib.context.WorkContext;

/**
 * Task for parsing XML files into Java-objects.
 */
public abstract class XmlObjectTask<T> extends NetworkTask {

    public static XmlMapper createDefaultMapper() {
        XmlFactory f = new XmlFactory(new InputFactoryImpl(), new OutputFactoryImpl());
        
        XmlMapper mapper = new XmlMapper(f);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        return mapper;
    }
    
    private Class<T> mClass;
    private XmlMapper mMapper = createDefaultMapper();
    private T mResult;
    
    public XmlObjectTask(WorkContext workContext, Class<T> klass) {
        this(workContext, null, klass);
    }
    
    public XmlObjectTask(WorkContext workContext, String baseUrl, Class<T> klass) {
        super(workContext, baseUrl);
        
        mClass = klass;
    }
    
    public void setXmlMapper(XmlMapper mapper) {
        mMapper = mapper;
    }
    
    public XmlMapper getXmlMapper() {
        return mMapper;
    }

    @Override
    protected final void onAsyncStream(InputStream stream) throws Exception {
        mResult = mMapper.readValue(stream, mClass);
        if (mResult == null) {
            throw new Exception("Could not parse XML into an object");
        }
    }
    
    @Override
    protected final void onFinish() {
        onFinishObject(mResult);
    }
    
    protected abstract void onFinishObject(T result);

}
