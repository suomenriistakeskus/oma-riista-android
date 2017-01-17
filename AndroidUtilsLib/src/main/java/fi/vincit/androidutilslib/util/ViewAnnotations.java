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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Helper class for avoiding tedious findViewById() calls and casting by
 * automatically matching annotated fields to their live views. All
 * annotated view id's must be found in the view hierarchy when apply()
 * is called, otherwise an exception will be raised unless the
 * annotated target is marked as optional.
 */
public class ViewAnnotations {

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ViewId {
        int[] value();
        boolean optional() default false;
    }
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ViewOnClick {
        int[] value();
        boolean optional() default false;
    }
    
    /**
     * Apply Activity annotations using it root decor view.
     */
    public static void apply(Activity activity) {
        apply(activity, activity.getWindow().getDecorView());
    }
    
    /**
     * Apply View annotations the view itself as target.
     */
    public static void apply(View view) {
        apply(view, view);
    }
    
    /**
     * Apply object annotations and looks for specific view annotations.
     */
    public static void apply(Object target, View view) {
        for (Field f : target.getClass().getDeclaredFields()) {
            scanIdAnnotations(target, f, view);
        }
        
        for (Method m : target.getClass().getDeclaredMethods()) {
            scanClickAnnotations(target, m, view);
        }
    }
    
    private static void scanIdAnnotations(Object target,  Field f, View view) {
        ViewId viewIds = f.getAnnotation(ViewId.class);
        if (viewIds != null) {
            View targetView = findViewByIds(view, viewIds.value());
            
            if (targetView == null && viewIds.optional()) {
                return;
            }
            else if (!viewIds.optional() && targetView == null) {
                throw new RuntimeException("Mandatory view id not found");
            }
            
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            
            try {
                f.set(target, targetView);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static void scanClickAnnotations(final Object target, final Method m, View view) {
        ViewOnClick click = m.getAnnotation(ViewOnClick.class);
        if (click != null) {
            final View targetView = findViewByIds(view, click.value());
            
            if (targetView == null && click.optional()) {
                return;
            }
            else if (!click.optional() && targetView == null) {
                throw new RuntimeException("Mandatory click view id not found");
            }
            
            if (!m.isAccessible()) {
                m.setAccessible(true);
            }
            
            try {
                targetView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            m.invoke(target, targetView);
                        } 
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Helper method for finding views in deeper hierarchies.
     */
    public static View findViewByIds(View root, int ... idPath) {
        View targetView = null;
        for (int viewId : idPath) {
            if (targetView == null) {
                targetView = root.findViewById(viewId);
            }
            else {
                targetView = targetView.findViewById(viewId);
            }
        }
        return targetView;
    }
}
