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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.remote.RemoteObject.RemoteObjectListener;
import fi.vincit.androidutilslib.task.JsonListTask;
import fi.vincit.androidutilslib.task.NetworkTask;

/**
 * This class can fetch and manage a collection of RemoteObject's.
 * 
 * All server related methods are asynchronous, use
 * RemoteCollectionListener if you want to track requests.
 */
public class RemoteCollection<T extends RemoteObject<T>> implements Collection<T>
{
    /**
     * Interface for listening collection events.
     */
    public static interface RemoteCollectionListener<T>
    {
        /**
         * Called when the contents of the list have been changed by
         * a remote operation. Note that because the list monitors
         * it's items all remote modifications to them also trigger
         * this. For example if you call get() on a RemoteObject
         * that belongs to a list the list will eventually call
         * this if the operation was successful. Normal "local" add() 
         * etc. operations don't trigger this. 
         * 
         * This is a common place to refresh your list view adapters etc.
         */
        public void onCollectionChanged();
    
        /**
         * Called when the collection's or any of it's items status changes,
         * for example if some item starts or ends an update or the collection
         * fetches or syncs. In short this is a good place to
         * update UI indicators etc. isBusy() is a good way to check
         * if there are any running operations. There can be situations where
         * the collection resets itself and calls this more than once in a
         * row with rapidly changing content.
         */
        public void onStatusChanged();
        
        /**
         * Called when a fetch operation has successfully finished.
         */
        public void onFetchFinished();
        
        /**
         * Called when a fetch operation has failed.
         */
        public void onFetchError();
        
        /**
         * Called when a sync has ended, that is all operations started
         * by it have finished. This does not mean that all operations were
         * successful, only that all operations have ended in one way
         * or another.
         */
        public void onSyncEnd();
        
        /**
         * Called if an object that belongs to this list encounters and
         * error while attempting to sync itself. Note that this can
         * be called even if the list is not explicitly syncing.
         * 
         * @param object The object that encountered an error.
         */
        public void onSyncError(T object);
        
        /**
         * Called when a task has been created for an object. It is possible
         * to set or override task attributes in this callback.
         */
        public void onObjectTask(T object, NetworkTask task);
        
        /**
         * Called when a task has been created for fetching all objects
         * in the collection from the server. It is possible
         * to set or override task attributes in this callback.
         */
        public void onFetchTask(NetworkTask task);
    }
    
    /**
     * Annotate a class for remote object list support.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface RemoteCollectionConfig 
    {
        String url();
    }
    
    private WorkContext mWorkContext;
    private Class<T> mClass;
    private ArrayList<T> mItems = new ArrayList<T>();
    private String mUrl;
    private RemoteCollectionListener<T> mListener;
    private ArrayList<T> mSyncingObjects; //TODO: HashSet for perf
    private FetchTask mFetchTask;
    
    public RemoteCollection(WorkContext workContext, Class<T> klass)
    {
        mWorkContext = workContext;
        mClass = klass;
        
        RemoteCollectionConfig remote = getClass().getAnnotation(RemoteCollectionConfig.class);
        mUrl = remote.url();
    }
    
    /**
     * Set the remote event listener.
     */
    public void setCollectionListener(RemoteCollectionListener<T> listener)
    {
        mListener = listener;
    }
    
    public RemoteCollectionListener<T> getCollectionListener()
    {
        return mListener;
    }
    
    /**
     * Returns a new list containing all items in this collection.
     */
    public List<T> getItems()
    {
        return new ArrayList<T>(mItems);
    }
    
    //Collection methods
    
    /**
     * Adds the item to the list and sets it remote object listener.
     */
    @Override
    public boolean add(T item)
    {
        mItems.add(item);
        item.setRemoteObjectListener(mObjectListener);
        return true;
    }
    
    @Override
    public boolean addAll(Collection items)
    {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /**
     * Clears all items from the list and sets their listeners to null.
     * Also clear sync state and cancels any fetch() in progress.
     */
    @Override
    public void clear()
    {
        stopFetch();
        
        mSyncingObjects = null;
        
        for (T item : mItems) {
            item.setRemoteObjectListener(null);
        }
        mItems.clear();
        
        notifyStatusChanged();
    }
    
    @Override
    public boolean contains(Object obj)
    {
        return mItems.contains(obj);
    }
    
    @Override
    public boolean containsAll(Collection<?> items)
    {
        return mItems.containsAll(items);
    }
    
    //TODO equals()
    
    //TODO hashCode()
    
    @Override
    public boolean isEmpty()
    {
        return mItems.isEmpty();
    }
    
    @Override
    public Iterator<T> iterator()
    {
        return mItems.iterator();
    }
    
    /**
     * Removes the item from the list if it is found and sets it's listener to null.
     * Returns true if item was removed.
     */
    @Override
    public boolean remove(Object item)
    {
        T remote = (T)item;

        boolean removed = false;
        if (mItems.remove(remote)) {
            remote.setRemoteObjectListener(null);
            removed = true;
            
            objectSynced(remote);
            
            notifyStatusChanged();
        }
        return removed;
    }
    
    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public int size()
    {
        return mItems.size();
    }
    
    @Override
    public Object[] toArray()
    {
        return mItems.toArray();
    }
    
    @Override
    public Object[] toArray(Object[] a)
    {
        return mItems.toArray(a);
    }
    
    /**
     * Returns the item at the given index.
     */
    public T get(int index)
    {
        return mItems.get(index);
    }
    
    /**
     * Returns true if the list is currently synchronizing.
     */
    public boolean isSyncing()
    {
        return mSyncingObjects != null;
    }
    
    /**
     * Synchronizes the list with a remote server. All items that are local are send to 
     * the server using post() and any remote but locally modified objects are updated 
     * by calling put(). This does not retrieve any new items from the server, this makes
     * it possible to easily handle subset of items. Use fetch() to get all items.
     * 
     * If there are no objects to sync the onSyncEnd() listener callback is called immediately.
     * If the collection is already syncing 0 is returned and nothing else is done. 
     * 
     * @return The number of items that will be synced. Note that this only
     *      checks current state. If more objects come available during
     *      the sync they will not be synced.
     */
    public int sync()
    {
        if (isSyncing()) {
            return 0;
        }
        
        mSyncingObjects = new ArrayList<T>();
        
        int count = 0;
        
        for (T item : mItems) {
            if (item.isSaveable()) {
                mSyncingObjects.add(item);
                
                item.save(mWorkContext);
                
                count++;
            }
        }
        
        if (mSyncingObjects.isEmpty()) {
            //Nothing to sync.
            mSyncingObjects = null;
            
            if (mListener != null) {
                mListener.onSyncEnd();
            }
        }
        return count;
    }
    
    /**
     * Returns true if the list is currently fetching new items.
     */
    public boolean isFetching()
    {
        return mFetchTask != null;
    }
    
    /**
     * Fetches items from a remote server. If there are any local objects in
     * the collection they are preserved. All new remote objects that were not
     * in the collection are added to it. Any existing objects are updated
     * if possible by using their id and version number if possible.
     * Usually this does not preserve the order of the list items.
     */
    public void fetch()
    {
        if (isFetching()) {
            return;
        }
        mFetchTask = new FetchTask(mWorkContext);
        mFetchTask.start();
    }
    
    /**
     * Stops a fetch() if it is currently running.
     */
    public void stopFetch()
    {
        if (mFetchTask != null) {
            mFetchTask.cancel();
            mFetchTask = null;
        }
    }
    
    /**
     * Returns true if the list is currently fetching or syncing or
     * if any object in the list has active or pending network
     * operations.
     */
    public boolean isBusy()
    {
        if (isFetching() || isSyncing()) {
            return true;
        }
        for (T item : mItems) {
            if (item.getTaskCount() > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Can be called if you want to signal the list that you have
     * made *local* changes to the list. Remote changes from server
     * automatically call this.
     * 
     * This idea is similar to BaseAdapter.notifyDataSetChanged().
     */
    private void notifyCollectionChanged() 
    {
        if (mListener != null) {
            mListener.onCollectionChanged();
        }
    }
    
    private void notifyStatusChanged()
    {
        if (mListener != null) {
            mListener.onStatusChanged();
        }
    }
    
    private void notifyFetchTask(NetworkTask task)
    {
        if (mListener != null) {
            mListener.onFetchTask(task);
        }
    }
    
    protected void log(String text)
    {
        Log.d(getClass().getName(), text);
    }
    
    /**
     * Merges new items with old ones.
     */
    private void updateFetched(List<T> newItems)
    {
        //Preserve the sync state for objects that were syncing and that
        //are still in the list after this update.
        ArrayList<T> syncs = new ArrayList<T>();
        
        //This will contain all items that will be in this collection.
        ArrayList<T> results = new ArrayList<T>();
        
        HashMap<Long, T> oldIds = new HashMap<Long, T>();
        for (T old : mItems) {
            if (old.isRemote()) {
                oldIds.put(old.getObjectId(), old);
            }
            else {
                //Local object.
                results.add(old);
                if (mSyncingObjects != null && mSyncingObjects.contains(old)) {
                    syncs.add(old);
                }
            }
        }

        for (T newItem : newItems) {
            T old = oldIds.get(newItem.getObjectId());
            if (old != null) {
                old.updateFields(newItem);
                old.saveSnapshotState();
                results.add(old);
                
                if (mSyncingObjects != null && mSyncingObjects.contains(old)) {
                    syncs.add(old);
                }
            }
            else {
                newItem.saveSnapshotState();
                results.add(newItem);
            }
        }
        
        clear();

        for (T item : results) {
            item.setRemoteObjectListener(mObjectListener);
        }
        mItems = results;
        
        if (syncs.isEmpty()) {
            mSyncingObjects = null;
        }
        else {
            mSyncingObjects = syncs;
        }
    }
    
    private boolean isObjectSyncing(T object)
    {
        if (mSyncingObjects != null && mSyncingObjects.contains(object)) {
            return true;
        }
        return false;
    }
    
    private boolean objectSynced(T object)
    {
        if (isObjectSyncing(object)) {
            mSyncingObjects.remove(object);
            
            if (mSyncingObjects.isEmpty()) {
                //All objects have been synced.
                mSyncingObjects = null;
                
                if (mListener != null) {
                    mListener.onSyncEnd();
                }
            }
            return true;
        }
        return false;
    }
    
    private RemoteObjectListener<T> mObjectListener = new RemoteObjectListener<T>() 
    {
        @Override
        public void onObjectOperation(T object) 
        {
            notifyStatusChanged();
        }
        
        @Override
        public void onObjectChange(T object) 
        {
            objectSynced(object);
            
            notifyCollectionChanged();
            notifyStatusChanged();
        }

        @Override
        public void onObjectDelete(T object) 
        {
            objectSynced(object);
            
            remove(object); //Calls notifyStatusChanged()
            
            notifyCollectionChanged();
        }

        @Override
        public void onObjectError(T object, NetworkTask.HttpMethod method) 
        {
            //Even if this failed keep going, otherwise the sync
            //will never be completed.
            objectSynced(object);
            
            if (mListener != null) {
                mListener.onSyncError(object);
            }
            notifyStatusChanged();
        }

        @Override
        public void onObjectTask(T object, NetworkTask task) 
        {
            if (mListener != null) {
                mListener.onObjectTask(object, task);
            }
        }
    };
    
    private class FetchTask extends JsonListTask<T>
    {
        public FetchTask(WorkContext context) 
        {
            super(context, mUrl, mClass);
        }
        
        @Override
        public void start()
        {
            notifyFetchTask(this);
            notifyStatusChanged();
            
            super.start();
        }
        
        @Override
        protected void onFinishObjects(List<T> results) 
        {
            mFetchTask = null; //Set to null before callbacks.
            
            updateFetched(results);
            
            notifyCollectionChanged();
            
            if (mListener != null) {
                mListener.onFetchFinished();
            }
        }
        
        @Override
        protected void onError()
        {
            mFetchTask = null; 
            if (mListener != null) {
                mListener.onFetchError();
            }
        }
        
        @Override
        protected void onCancel()
        {
            mFetchTask = null;
        }
        
        @Override
        protected void onEnd()
        {
            notifyStatusChanged();
        }
    }
}
