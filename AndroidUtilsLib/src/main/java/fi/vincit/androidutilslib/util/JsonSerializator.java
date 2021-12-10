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
package fi.vincit.androidutilslib.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper class for serializing objects into JSON and back.
 */
public class JsonSerializator
{
    /**
     * Default ObjectMapper instance needs to be initialized by application module!
     */
    private static ObjectMapper sMapper;

    /**
     * Returns the ObjectMapper that will be used for serialization. This
     * can be modified.
     */
    public static ObjectMapper getDefaultMapper()
    {
        if (sMapper == null) {
            throw new IllegalStateException("Default ObjectMapper instance is not initialized");
        }

        return sMapper;
    }

    public static void setDefaultMapper(final ObjectMapper mapper)
    {
        sMapper = mapper;
    }

    /**
     * Create a Jackson object mapper with reasonable default configuration.
     */
    public static ObjectMapper createDefaultMapper()
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        return mapper;
    }

    private final ObjectMapper mMapper;

    public JsonSerializator(final ObjectMapper mapper)
    {
        mMapper = mapper;
    }

    /**
     * Attempts to serialize an object into a string. Returns null on failure. If the
     * argument is null a string "null" is returned.
     */
    public String toJson(final Object object)
    {
        try {
            return mMapper.writeValueAsString(object);
        }
        catch (final Exception e) {
            return null;
        }
    }

    /**
     * Attempts to serialize an object from a string. Returns null on failure or
     * if the argument string is "null".
     */
    public <T> T fromJson(final String data, final Class<T> klass)
    {
        if (data == null) {
            return null;
        }

        try {
            return mMapper.readValue(data, klass);
        }
        catch (final Exception e) {
            return null;
        }
    }
}
