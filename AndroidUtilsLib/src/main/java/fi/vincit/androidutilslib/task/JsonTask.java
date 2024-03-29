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

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.util.JsonSerializator;

public abstract class JsonTask extends NetworkTask {

    private ObjectMapper mObjectMapper = JsonSerializator.getDefaultMapper();
    
    public JsonTask(WorkContext workContext) 
    {
        this(workContext, null);
    }
    
    public JsonTask(WorkContext workContext, String baseUrl) 
    {
        super(workContext, baseUrl);
    }

    /**
     * Returns the Jackson object mapper that will be used to deserialize Json-objects.
     */
    public ObjectMapper getObjectMapper()
    {
        return mObjectMapper;
    }
    
    public void setObjectMapper(ObjectMapper mapper)
    {
        mObjectMapper = mapper;
    }
}
