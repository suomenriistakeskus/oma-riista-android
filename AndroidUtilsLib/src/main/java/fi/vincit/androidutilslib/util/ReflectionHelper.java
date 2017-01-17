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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionHelper 
{
    private static Map<Class<?>, Class<?>> mPrimitiveMap = new HashMap<Class<?>, Class<?>>();
    
    static 
    {
        mPrimitiveMap.put(boolean.class, Boolean.class);
        mPrimitiveMap.put(byte.class, Byte.class);
        mPrimitiveMap.put(char.class, Character.class);
        mPrimitiveMap.put(short.class, Short.class);
        mPrimitiveMap.put(int.class, Integer.class);
        mPrimitiveMap.put(long.class, Long.class);
        mPrimitiveMap.put(float.class, Float.class);
        mPrimitiveMap.put(double.class, Double.class);
    }
    
    /**
     * Set object's field into the given value. This can only be used
     * if the field is a primitive type (or their object version) or a String.
     */
    public static void setField(Object target, String name, String value) throws Exception 
    {
        Field f = target.getClass().getDeclaredField(name);
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
        
        Class<?> fieldClass = f.getType();
        if (fieldClass.isAssignableFrom(String.class)) {
            f.set(target, value);
        }
        else {
            if (fieldClass.isPrimitive()) {
                fieldClass = mPrimitiveMap.get(fieldClass);
            }
            Object converted = fieldClass.getMethod("valueOf", String.class).invoke(null, value);
            f.set(target, converted);
        }
    }
    
    /**
     * Gets an attribute from an object using reflection. If the attribute string
     * ends in "()" it is assumed that the attribute is a public method with
     * no arguments. Otherwise direct field access is used.
     */
    public static Object getAttribute(Object item, Class<?> klass, String attribute)
    {
        if (klass == null) {
            klass = item.getClass();
        }

        Class<?> argTypes[] = null;
        Object args[] =  null;
        
        try {
            if (attribute.endsWith("()")) {
                Method m = klass.getMethod(attribute.substring(0, attribute.length() - 2), argTypes);
                return m.invoke(item, args);
            }
            else {
                Field f = klass.getDeclaredField(attribute);
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                return f.get(item);
            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Invokes a method using the given arguments.
     */
    public static Object invokeMethod(Object target, String method, Class<?>[] argTypes, Object ... args) 
    {
        try {
            Method m = target.getClass().getDeclaredMethod(method, argTypes);
            if (!m.isAccessible()) {
                m.setAccessible(true);
            }
            return m.invoke(target, args);
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
