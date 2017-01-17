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
package fi.vincit.androidutilslib.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.vincit.androidutilslib.util.ReflectionHelper;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This adapter can be used to populate list views by specifying
 * class and attribute mappings. It also helps in view recycling.
 * 
 * If your objects have stable id's you should should call
 * setHasStableIds(true).
 * 
 * In most situations default mappings between data and views
 * work quite well but if you have complex views it is often useful 
 * to extend your own adapter from this and override the getView() method. 
 * Then call super.getView() and then perform any required
 * operations on the returned view and then return it. This 
 * lets the AttributeAdapter to perform all heavy lifting.
 */
public class AttributeAdapter<T> extends BaseAdapter
{
    /**
     * Item event listener used with mapClickToListener().
     */
    public static interface ItemListener<T>
    {
        public void onViewClicked(int position, T item);
        public void onViewChecked(int position, T item, boolean checked);
    }
    
    /**
     * Binders can be used instead if directly mapping item attributes.
     */
    public static interface ItemBinder<T>
    {
        public void onBind(T item, View view);
    }
    
    private ArrayList<T> mItems = new ArrayList<T>();
    private HashMap<Class<?>, DataMapping<T>> mClassMapping = new HashMap<Class<?>, DataMapping<T>>();
    private boolean mHasStableIds = false;
    private boolean mBlockClicks = false; //Internal use only
    
    private static class AttributeMapping<T>
    {
        public int viewIdPath[];
        public int visibilityIfNoData;
        public Object fallback;
        public Object invalid;
        public ItemBinder<T> binder;
    }
    
    private static class ClickMapping<T>
    {
        public int viewIdPath[];
        public ItemListener<T> listener;
    }
    
    private static class DataMapping<T>
    {
        public Class<?> realClass = null;
        public int viewType;
        public int layoutId;
        public boolean isEnabled = false;
        public String idAttribute = null;
        public HashMap<String, AttributeMapping<T>> attributeMapping = new HashMap<String, AttributeMapping<T>>();
        public ArrayList<ClickMapping<T>> clickMappings = new ArrayList<ClickMapping<T>>();
    }

    /**
     * Maps a named attribute to a view.
     * 
     * @param klass The class which attribute will be mapped.
     * @param name Name of the class attribute to be mapped. If this ends with () is is assumed that
     *      it is a method name, otherwise value is read directly from the field.
     * @param viewIdPath Android view id's (eg. R.id.some_view). The id's will be searched recursively and 
     *      the attribute will be bound to the final view.
     * @param visibilityIfNoData If the attribute has no data (is null or invalid) 
     *      the view visibility will be set to this.
     * @param fallback If the attribute is null this will be used instead. Can be null.
     * @param invalid If the attribute is not null but is equal to this object the
     *      attribute is handled as containing no data. Can be null.
     */
    public void mapAttributeToView(Class<?> klass, String name, int viewIdPath[], int visibilityIfNoData, Object fallback, Object invalid)
    {
        AttributeMapping<T> mapping = new AttributeMapping<T>();
        mapping.viewIdPath = viewIdPath;
        mapping.visibilityIfNoData = visibilityIfNoData;
        mapping.fallback = fallback;
        mapping.invalid = invalid;
        mapping.binder = null;
        mClassMapping.get(klass).attributeMapping.put(name, mapping);
    }
    
    /**
     * See mapAttributeToView()
     */
    public void mapAttributeToView(Class<?> klass, String name, int ... viewIdPath)
    {
        mapAttributeToView(klass, name, viewIdPath, View.INVISIBLE, null, null);
    }

    /**
     * Maps a named attribute to a view. User defined binder callback interface
     * should set all view attributes according to the given item.
     */
    public void mapAttributeToBinder(Class<?> klass, String name, ItemBinder<T> binder, int ... viewIdPath)
    {
        AttributeMapping<T> mapping = new AttributeMapping<T>();
        mapping.viewIdPath = viewIdPath;
        mapping.visibilityIfNoData = View.INVISIBLE;
        mapping.fallback = null;
        mapping.invalid = null;
        mapping.binder = binder;
        mClassMapping.get(klass).attributeMapping.put(name, mapping);
    }
    
    /**
     * Map a view click event to a listener. If the view is a CompoundButton listener.onViewChecked() will be
     * called when the user checks or unchecks the view. In other cases listener.onViewClicked() is called.
     */
    public void mapClickToListener(Class<?> klass, ItemListener<T> listener, int ... viewIdPath)
    {
        ClickMapping<T> mapping = new ClickMapping<T>();
        mapping.viewIdPath = viewIdPath;
        mapping.listener = listener;
        mClassMapping.get(klass).clickMappings.add(mapping);
    }
    
    /**
     * Map a class into a layout file.
     * 
     * @param klass The class which will mapped into an Android layout.
     * @param layoutId Android layout id (eg. R.layout.some_layout)
     * @param idAttribute This attribute will be used to get the item id in the list. Can be null.
     * @param isEnabled If true the user can interact with the views.
     */
    public void mapClassToLayout(Class<?> klass, int layoutId, String idAttribute, boolean isEnabled)
    {
        DataMapping<T> mapping = new DataMapping<T>();
        mapping.realClass = klass;
        mapping.viewType = mClassMapping.size();
        mapping.layoutId = layoutId;
        mapping.isEnabled = isEnabled;
        mapping.idAttribute = idAttribute;
        mClassMapping.put(klass, mapping);
    }
    
    /**
     * See the overloaded method.
     */
    public void mapClassToLayout(Class<?> klass, int layoutId, String idAttribute)
    {
        mapClassToLayout(klass, layoutId, idAttribute, true);
    }
    
    /**
     * See the overloaded method.
     */
    public void mapClassToLayout(Class<?> klass, int layoutId)
    {
        mapClassToLayout(klass, layoutId, null, true);
    }
    
    /**
     * Clears all items from the adapter and adds the new items.
     */
    public void reset(Collection<T> items)
    {
        mItems.clear();
        mItems.addAll(items);

        notifyDataSetChanged();
    }
    
    /**
     * Clears all items from the adapter.
     */
    public void clear()
    {
        mItems.clear();
        
        notifyDataSetChanged();
    }
    
    /**
     * Adds a single item.
     */
    public void add(T item)
    {
        mItems.add(item);
        
        notifyDataSetChanged();
    }
    
    /**
     * Adds all items from the array.
     */
    public void addAll(T[] items)
    {
        for (T item : items) {
            mItems.add(item);
        }
        notifyDataSetChanged();
    }
    
    /**
     * Adds all items from the list.
     */
    public void addAll(Collection<T> items) 
    {
        mItems.addAll(items);
        
        notifyDataSetChanged();
    }
    
    /**
     * Moves an item inside the adapter.
     * 
     * @param from Start position index.
     * @param to End position index.
     */
    public void move(int from, int to)
    {
        T item = mItems.get(from);
        mItems.remove(from);
        
        if (from > to) {
            mItems.add(to, item);
        }
        else {
            mItems.add(to - 1, item);
        }

        notifyDataSetChanged();
    }
    
    /**
     * Sorts the items using the given attribute name. The attribute
     * must implement Comparable. This is also relatively slow.
     */
    public void sort(final String attributeName, final boolean ascending)
    {
        Collections.sort(mItems, new Comparator<T>() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public int compare(T object1, T object2) {
                Comparable a = (Comparable)ReflectionHelper.getAttribute(object1, 
                        getClassMapping((T)object1).realClass, attributeName);
                
                Comparable b = (Comparable)ReflectionHelper.getAttribute(object2, 
                        getClassMapping((T)object2).realClass, attributeName);
                
                int comparison = a.compareTo(b);
                if (!ascending) {
                    comparison = -comparison;
                }
                return comparison;
            }
        });
        notifyDataSetChanged();
    }
    
    @Override
    public int getViewTypeCount()
    {
        return mClassMapping.size();
    }
    
    @Override
    public int getItemViewType(int position)
    {
        return getClassMapping(mItems.get(position)).viewType;
    }
    
    @Override
    public boolean isEnabled(int position) 
    {
        return getClassMapping(mItems.get(position)).isEnabled;
    }
    
    @Override
    public int getCount() 
    {
        return mItems.size();
    }

    @Override
    public T getItem(int position) 
    {
        return mItems.get(position);
    }

    /**
     * Sets if the objects in the adapter have stable id's.
     */
    public void setHasStableIds(boolean stable)
    {
        mHasStableIds = stable;
    }
    
    @Override
    public boolean hasStableIds()
    {
        return mHasStableIds;
    }
    
    @Override
    public long getItemId(int position) 
    {
        T item = mItems.get(position);
        
        DataMapping<T> mapping = getClassMapping(item);
        if (mapping.idAttribute != null) {
            return (Long)(ReflectionHelper.getAttribute(item, mapping.realClass, mapping.idAttribute));
        }
        return -1;
    }
    
    /**
     * Recursively walk the inheritance hierarchy upwards until we find
     * a class that has been mapped to this adapter or we reach the
     * topmost superclass.
     */
    private DataMapping<T> getClassMappingForClass(Class<?> klass) 
    {
        DataMapping<T> mapping = mClassMapping.get(klass);
        if (mapping == null) {
            //Look from superclasses.
            Class<?> superClass = klass.getSuperclass();
            if (superClass != null) {
                mapping = getClassMappingForClass(superClass);
            }
        }
        return mapping;
    }

    private DataMapping<T> getClassMapping(T item)
    {
        Class<?> klass = item.getClass();
        DataMapping<T> mapping = getClassMappingForClass(klass);
        if (mapping == null) {
            throw new RuntimeException("AttributeAdapter has an object with unknown class: " + klass.getName());
        }
        return mapping;
    }
    
    private View findViewByIdPath(View root, int[] idPath)
    {
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
    
    private void mapClickEvents(final int position, final T item, DataMapping<T> mapping, View root)
    {
        for (final ClickMapping<T> click : mapping.clickMappings) {
            View clickView = findViewByIdPath(root, click.viewIdPath);
            
            if (clickView instanceof CompoundButton) {
                CompoundButton button = (CompoundButton)clickView;
                button.setOnCheckedChangeListener(new OnCheckedChangeListener() 
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
                    {
                        if (!mBlockClicks) {
                            click.listener.onViewChecked(position, item, isChecked); 
                        }
                    }
                });
            }
            else {
                clickView.setOnClickListener(new OnClickListener() 
                {
                    @Override
                    public void onClick(View view) 
                    {
                        if (!mBlockClicks) {
                            click.listener.onViewClicked(position, item); 
                        }
                    }
                });
            }
        }
    }
    
    private void mapReflectedData(String key, T item, DataMapping<T> mapping, AttributeMapping<T> viewMapping, View targetView)
    {
        targetView.setVisibility(View.VISIBLE);

        Object result = null;
        
        if (key == null) {
            result = item; //Item is itself's value.
        }
        else {
            result = ReflectionHelper.getAttribute(item, mapping.realClass,  key);
        }
        
        if (result == null) {
            result = viewMapping.fallback;
        }
        
        if (result == null || (result != null && result.equals(viewMapping.invalid))) {
            //Item and it's fallback are null or the item is invalid.
            targetView.setVisibility(viewMapping.visibilityIfNoData);
        }
        else if (targetView instanceof CompoundButton) {
            CompoundButton checkable = (CompoundButton)targetView;
            checkable.setChecked((Boolean)result);
        }
        else if (targetView instanceof TextView) {
            ((TextView)targetView).setText(result.toString());
        }
        else if (targetView instanceof ImageView) {
            if (result instanceof CharSequence) {
                ((ImageView)targetView).setImageURI(Uri.parse(result.toString()));
            }
            else {
                ((ImageView)targetView).setImageResource((Integer)result);
            }
        }
        else {
            throw new RuntimeException("Unknown view type for key: " + key);
        }
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        T item = getItem(position);

        DataMapping<T> mapping = getClassMapping(item);
        
        mBlockClicks = true;
        
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(mapping.layoutId, null);
        }
        
        mapClickEvents(position, item, mapping, view);
        
        for (Map.Entry<String, AttributeMapping<T>> entry : mapping.attributeMapping.entrySet()) {
            String key = entry.getKey();
            AttributeMapping<T> viewMapping = entry.getValue();
            
            View targetView = findViewByIdPath(view, viewMapping.viewIdPath);

            if (viewMapping.binder != null) {
                viewMapping.binder.onBind(item, targetView);
            }
            else {
                mapReflectedData(key, item, mapping, viewMapping, targetView);
            }
        }
        
        mBlockClicks = false;
        
        return view;
    }
    
}
