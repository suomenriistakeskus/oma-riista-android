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
package fi.vincit.androidutilslib.remote;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Annotation;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;
import fi.vincit.androidutilslib.task.NetworkTask;
import fi.vincit.androidutilslib.util.ReflectionHelper;

/**
 * Remote object can be used to "connect" classes to
 * servers which provide a REST+CRUD functionality. This implementation
 * uses JSON for transferring data. You can use Jackson annotations to
 * control what attributes are serialized. All server related methods are asynchronous, 
 * use RemoteObjectListener if you want to track requests.
 * 
 * A remote object is identified by it's id (which is specified by the annotation).
 * Object version numbers are also supported.
 * 
 * It is recommended that all attributes of any subclasses are primitive, immutable or implement
 * a proper equals() method.
 */
public class RemoteObject<T> 
{
    /**
     * This listener will be called every time the object receives
     * an update or is deleted from server.
     */
    public static interface RemoteObjectListener<T> 
    {
        /**
         * Called when a object starts a remote operation.
         */
        public void onObjectOperation(T object);
        
        /**
         * Called when a remote operation changed or updated object's data.
         */
        public void onObjectChange(T object);
        
        /**
         * Called when a object has been deleted. The object can be remote
         * or local.
         */
        public void onObjectDelete(T object);
        
        /**
         * Called when a remote operation has encountered an error.
         */
        public void onObjectError(T object, NetworkTask.HttpMethod method);
        
        /**
         * Called when a task has been created for an object. This task will
         * be used in a remote operation. It is possible
         * to set or override task attributes in this callback.
         */
        public void onObjectTask(T object, NetworkTask task);
    }
    
    /**
     * This annotation specifies the remote id of an object. It should
     * have the type int or long.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface RemoteId
    {
    }
    
    /**
     * This annotation specifies the remote version of an object. It should
     * have the type of int or long.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface RemoteVersion
    {
    }
    
    /**
     * Annotate a class for remote object support.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface RemoteObjectConfig 
    {
        /**
         * Default url for Http methods. If a specific method
         * url is not set this will be used.
         */
        String url();
        
        /**
         * Url used for GET
         */
        String get() default "";
        
        /**
         * Url used for POST
         */
        String post() default "";
        
        /**
         * Url used for PUT
         */
        String put() default "";
        
        /**
         * Url used for DELETE
         */
        String delete() default "";
    }
    
    private static final Pattern ATTRIBUTE_REGEXP = Pattern.compile("\\{([^}]*)\\}");
    
    //Free, local object id's.
    private static long mLocalIdCounter = 0;
    
    private RemoteObjectListener<T> mListener;
    private Field mIdField;
    private Field mVersionField;
    private long mLocalId = -1;
    private boolean mAlive = true;
    private ArrayList<Object> mSyncedState = new ArrayList<Object>();
    private int mTaskCount = 0;
    private ArrayList<Field> mFields = new ArrayList<Field>();
    
    public RemoteObject() 
    {
        mFields = getValidFields();
        
        try {
            mIdField = findField(RemoteId.class);
            if (mIdField == null) {
                throw new Exception("No RemoteId annotation found");
            }
            mIdField.setAccessible(true);
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            mVersionField = findField(RemoteVersion.class);
            if (mVersionField != null) {
                mVersionField.setAccessible(true);
            }
        } 
        catch (Exception e) {
            //Optional
        }
    }
    
    private ArrayList<Field> getValidFields() {
        ArrayList<Field> fields = new ArrayList<Field>();
        
        for (Field f : getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(JsonIgnore.class) == false) {
                fields.add(f);
            }
        }
        return fields;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Field findField(Class annotation)
    {
        for (Field f : mFields) {
            if (f.getAnnotation(annotation) != null) {
                return f;
            }
        }
        return null;
    }
    
    protected RemoteObjectConfig getRemoteConfig()
    {
        return getClass().getAnnotation(RemoteObjectConfig.class);
    }
    
    protected String getMethodUrl(NetworkTask.HttpMethod method)
    {
        RemoteObjectConfig config = getRemoteConfig();
        
        String url = null;
        if (method == NetworkTask.HttpMethod.GET) {
            url = config.get();
        }
        else if (method == NetworkTask.HttpMethod.POST) {
            url = config.post();
        }
        else if (method == NetworkTask.HttpMethod.PUT) {
            url = config.put();
        }
        else if (method == NetworkTask.HttpMethod.DELETE) {
            url = config.delete();
        }
        
        if (url == null || url.equals("")) {
            //Default fallback.
            url = config.url();
        }
        return url;
    }
    
    /**
     * Returns how many network tasks this object has currently
     * running or waiting for a run.
     */
    @JsonIgnore
    public int getTaskCount()
    {
        return mTaskCount;
    }
    
    /**
     * Returns true if the object is considered alive. Object is considered dead
     * when a delete() call is made and it succeeds.
     */
    @JsonIgnore
    public boolean isAlive()
    {
        return mAlive;
    }
    
    /**
     * Returns true if the object has remote backing.
     * This is determined by checking object's id: if it is greater
     * than 0 object is assumed to have remote backing.
     */
    @JsonIgnore
    public boolean isRemote()
    {
        return getObjectId() > 0;
    }
    
    /**
     * Sets a listener that will be notified when the object receives
     * a remote update.
     */
    public void setRemoteObjectListener(RemoteObjectListener<T> listener)
    {
        mListener = listener;
    }
    
    /**
     * Sends a GET request to the server.
     */
    public void get(WorkContext workContext) 
    {
        if (mAlive) {
            ObjectTask task = new ObjectTask(workContext, ObjectTask.HttpMethod.GET);
            task.start();
        }
    }
    
    /**
     * Sends a POST request to the server.
     */
    public void post(WorkContext workContext) 
    {
        if (mAlive) {
            ObjectTask task = new ObjectTask(workContext, ObjectTask.HttpMethod.POST);
            task.setJsonEntity(this);
            task.start();
        }
    }
    
    /**
     * Sends a PUT request to the server.
     */
    public void put(WorkContext workContext) 
    {
        if (mAlive) {
            ObjectTask task = new ObjectTask(workContext, ObjectTask.HttpMethod.PUT);
            task.setJsonEntity(this);
            task.start();
        }
    }
    
    /**
     * Sends a DELETE request to the server. When and if the delete request
     * succeeds the object is marked as invalid and no longer accepts
     * any remote requests.
     */
    public void delete(WorkContext workContext) 
    {
        if (mAlive) {
            if (isRemote()) {
                ObjectTask task = new ObjectTask(workContext, ObjectTask.HttpMethod.DELETE);
                task.start();
            }
            else {
                mAlive = false;
                
                callRemoteListenerDeleted();
            }
        }
    }
    
    /**
     * Saves the object to the remote server. If the model does not
     * exist in the server post() is used. If the model exists
     * but is dirty put() is used. Returns true if a request
     * was sent to the server.
     */
    public boolean save(WorkContext workContext)
    {
        if (mAlive) {
            if (!isRemote()) {
                //This object is not on the server yet.
                post(workContext);
                return true;
            }
            else if (isDirty()) {
                //On the server but local copy is dirty.
                put(workContext);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if the object should be saved. This means that object
     * is alive but does not have remote backing or is dirty.
     */
    @JsonIgnore
    public boolean isSaveable()
    {
        if (mAlive) {
            if (!isRemote() || isDirty()) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * If the object has been loaded from a remote server this will reset (or undo)
     * object's attributes to those which it had when it was previously
     * loaded from the server. In the case of primitive and immutable
     * types this works well but if the object has mutable attributes that have
     * been changed this will restore those objects but their state and mutated 
     * attributes are not restored.
     * 
     * @return True if the undo succeeded.
     */
    @JsonIgnore
    public boolean undo()
    {
        if (mSyncedState.isEmpty()) {
            return false;
        }
        
        int counter = 0;
        for (Field f : mFields) {
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            try {
                f.set(this, mSyncedState.get(counter));
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            counter++;
        }
        return true;
    }
    
    /**
     * Implements an equality check. Note that both this and hashCode()
     * are final. If you want to override this in your inherited classes
     * you are free to remove the final as long as you understand what
     * you are doing. For example using the object id seems like a good
     * idea but there are subtle issues. For example when a local object
     * get's an id from the server: this would change it's identity. 
     * For example if you have stored the object into a list of some 
     * other container you can suddenly notice that contains() etc.
     * no longer find the object. There are other similar issues
     * so make sure to understand them before implementing these methods.
     * These methods should only rely on immutable attributes and
     * id is not always one. If you only use objects that have a
     * valid id that never changes these issues no longer apply.
     */
    @Override
    public final boolean equals(Object obj)
    {
        return this == obj;
    }
    
    @Override
    public final int hashCode()
    {
        return super.hashCode();
    }
    
    private void callRemoteListenerOperation()
    {
        if (mListener != null) {
            mListener.onObjectOperation((T)this);
        }
    }
    
    private void callRemoteListenerChanged()
    {
        if (mListener != null) {
            mListener.onObjectChange((T)this);
        }
    }
    
    private void callRemoteListenerDeleted()
    {
        if (mListener != null) {
            mListener.onObjectDelete((T)this);
        }
    }
    
    private void callRemoteListenerError(NetworkTask.HttpMethod httpMethod)
    {
        if (mListener != null) {
            mListener.onObjectError((T)this, httpMethod);
        }
    }
    
    private void callRemoteListenerTask(NetworkTask task)
    {
        if (mListener != null) {
            mListener.onObjectTask((T)this, task);
        }
    }
    
    /**
     * Returns the id of the object. If the object has remote
     * backing this will be greater than 0. Otherwise it can
     * be 0 or some negative value.
     */
    @JsonIgnore
    public long getObjectId()
    {
        try {
            return mIdField.getLong(this);
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }
    
    protected void setObjectId(long id)
    {
        try {
            mIdField.setLong(this, id);
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the object's version number or -1 if not available.
     */
    @JsonIgnore
    public long getObjectVersion()
    {
        if (mVersionField == null) {
            return -1;
        }
        
        try {
            return mVersionField.getLong(this);
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }
    
    /**
     * Attempts to set the object's version number. Usually there is no need
     * to call this manually. Returns false if the object does not
     * support version numbers.
     */
    public boolean setObjectVersion(long version)
    {
        if (mVersionField == null) {
            return false;
        }
        try {
            mVersionField.setLong(this, version);
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    
    /**
     * Returns a local id for the object. All objects both remote
     * and local have unique local id's. They are not persistent
     * and are reset when the process exits.
     */
    @JsonIgnore
    public long getLocalId()
    {
        if (mLocalId == -1) {
            mLocalId = getNextFreeLocalId();
        }
        return mLocalId;
    }
    
    private static synchronized long getNextFreeLocalId()
    {
        mLocalIdCounter++;
        return mLocalIdCounter;
    }
    
    private static boolean areObjectsEqual(Object a, Object b)
    {
        return a == b || (a != null && a.equals(b));
    }
    
    /**
     * Returns true if the object has been modified since it's
     * last remote update.
     */
    @JsonIgnore
    public boolean isDirty()
    {
        if (mSyncedState.isEmpty()) {
            return false;
        }
        
        int counter = 0;
        for (Field f : mFields) {
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            try {
                Object currentValue = f.get(this);
                Object oldValue = mSyncedState.get(counter);
                if (!areObjectsEqual(currentValue, oldValue)) {
                    //Not equal, must be dirty.
                    return true;
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            counter++;
        }
        return false;
    }
    
    /**
     * This method is for internal user only.
     */
    public void saveSnapshotState()
    {
        mSyncedState.clear();
        
        for (Field f : mFields) {
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            try {
                mSyncedState.add(f.get(this));
            } 
            catch (Exception e) {
                throw new RuntimeException(e);
            } 
        }
    }
    
    /**
     * This method is for internal user only.
     */
    public void updateFields(T object)
    {
        long version = getObjectVersion();
        RemoteObject<?> other = (RemoteObject<?>)object;
        if (version < 0 || other.getObjectVersion() > version) {
            //We have invalid version value or the other one is newer.
            
            for (Field f : mFields) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                try {
                    Object fieldValue = f.get(object);
                    f.set(this, fieldValue);
                } 
                catch (Exception e) {
                    throw new RuntimeException(e);
                } 
            }
            saveSnapshotState();
        }
    }
    
    /**
     * Creates a full url from the class annotation url and object attributes.
     */
    protected String onCreateObjectUrl(String url, NetworkTask.HttpMethod method)
    {
        String result = url;
        while (true) {
            Matcher regexMatcher = ATTRIBUTE_REGEXP.matcher(result);
            if (regexMatcher.find()) {
                String attributeName = regexMatcher.group();
                Object value = ReflectionHelper.getAttribute(this, null, attributeName.substring(1, attributeName.length() - 1));
                result = regexMatcher.replaceFirst(value.toString());
            }
            else {
                break;
            }
        }
        return result;
    }
    
    private class ObjectTask extends JsonObjectTask<T> 
    {
        public ObjectTask(WorkContext context, NetworkTask.HttpMethod method) 
        {
            super(context, onCreateObjectUrl(getMethodUrl(method), method), (Class<T>)RemoteObject.this.getClass());
            
            setHttpMethod(method);
        }
        
        @Override
        public void start()
        {
            callRemoteListenerTask(this);
            
            mTaskCount++;
            
            callRemoteListenerOperation();
            
            super.start();
        }

        @Override
        protected void onFinishObject(T result) 
        {
            mTaskCount--;
            
            if (mAlive) {
                if (getHttpMethod() == HttpMethod.DELETE) {
                    //Delete must always go through.
                    mAlive = false;
                    
                    //Delete should theoretically always be the last possible
                    //request. If it is not it might be because of network delay, caching, 
                    //or some other server issue, but update fields here last time.
                    updateFields(result);
                    
                    callRemoteListenerDeleted();
                }
                else {
                    updateFields(result);
                    
                    callRemoteListenerChanged();
                }
            }
        }
        
        @Override
        protected void onError()
        {
            mTaskCount--;
            if (mAlive) {
                callRemoteListenerError(getHttpMethod());
            }
        }
        
        @Override
        protected void onCancel()
        {
            mTaskCount--;
        }
    }
}
