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
package fi.vincit.androidutilslib.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fi.vincit.androidutilslib.message.OnAppExitMessage;
import fi.vincit.androidutilslib.message.OnPauseMessage;
import fi.vincit.androidutilslib.message.OnResumeMessage;
import fi.vincit.androidutilslib.message.WorkMessageHandler;
import fi.vincit.androidutilslib.task.WorkAsyncTask;
import fi.vincit.androidutilslib.util.WorkTimer;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

/**
 * WorkContext is used to control the lifetime and state of different resources
 * like asynchronous tasks or timers. This tracks those resources and makes
 * sure that they are freed when required and that they receive proper
 * events when needed.
 * 
 * In order to do use this class you must call appropriate callbacks from your
 * activity or service. See WorkActivity for an example.
 */
public class WorkContext 
{
    /**
     * Create a free local context that is not associated with any
     * Activity or Service. You can use this to manage tasks and
     * other work context related resources. The created WorkContext
     * will always use an Android application context. Usually there is not
     * much need to use this, make sure you know what you are doing
     * before using this. 
     */
    public static WorkContext createFreeLocalWorkContext(Context context) {
        WorkContext workContext = new WorkContext();
        workContext.onCreateLocal(context.getApplicationContext());
        return workContext;
    }
    
    private static final int TYPE_LOCAL = 1;
    private static final int TYPE_GLOBAL = 2;
    private static final int TYPE_OBSERVER = 3;
    
    private static ArrayList<WorkContext> mWorkContexts = new ArrayList<WorkContext>();
    private static HashMap<Class<?>, Boolean> mClassListeners = new HashMap<Class<?>, Boolean>();
    
    private Context mContext;
    private ArrayList<WorkAsyncTask> mTasks; //Task handling must be thread safe.
    private ArrayList<WorkTimer> mTimers;
    private Handler mHandler;
    private int mType;
    private boolean mDestroyed = true;
    
    /**
     * Creates a new local WorkContext. Local contexts are not visible to
     * other work contexts, can't receive global events and are not observed
     * when determining if an OnAppExitEvent should be sent.
     * 
     * @param context Android context this WorkContext will be associated with.
     */
    public void onCreateLocal(Context context) 
    {
        if (mContext != null) {
            throw new RuntimeException("onCreateLocal() called again for WorkContext");
        }
        init(context);
        mType = TYPE_LOCAL;
    }
    
    /**
     * Creates a new global WorkContext. The work context will be added to global 
     * tracking list so that it will receive global events and if all global work
     * contexts are destroyed an OnAppExitEvent will be sent.
     * 
     * @param context Android context this WorkContext will be associated with.
     */
    public void onCreateGlobal(Context context) 
    {
        if (mContext != null) {
            throw new RuntimeException("onCreateGlobal() called again for WorkContext");
        }
        init(context);
        mType = TYPE_GLOBAL;
        
        mWorkContexts.add(this);
    }
    
    /**
     * Creates an observer work context. This is similar to a global context except
     * these contexts are not counted when trying to determine if an activity or
     * service onDestroy() should cause an OnAppExitEvent to be sent. For example
     * the Application singleton or generic library components can use this type
     * to observe and control things but not affect other parts of the application
     * too much.
     * 
     * @param context Android context this WorkContext will be associated with.
     */
    public void onCreateObserver(Context context)
    {
        if (mContext != null) {
            throw new RuntimeException("onCreateObserver() called again for WorkContext");
        }
        init(context);
        mType = TYPE_OBSERVER;
        
        mWorkContexts.add(this);
    }
    
    private void init(Context context)
    {
        mContext = context;
        mTasks = new ArrayList<WorkAsyncTask>();
        mTimers = new ArrayList<WorkTimer>();
        mHandler = new Handler(Looper.getMainLooper());
        mDestroyed = false;
    }
    
    /**
     * Must be called from your Activity's onResume() if this WorkContext is 
     * associated with one.
     */
    public void onResume()
    {
        checkContext("onResume()");
        
        pauseTimers(false);
        
        sendLocalMessage(new OnResumeMessage());
    }
    
    /**
     * Must be called from your Activity's onPause() if this WorkContext is 
     * associated with one.
     */
    public void onPause()
    {
        checkContext("onPause()");
        
        boolean finishing = false;
        if (mContext instanceof Activity) {
            finishing = ((Activity)mContext).isFinishing();
        }
        sendLocalMessage(new OnPauseMessage(finishing));
        
        pauseTimers(true);
    }
    
    /**
     * Must be called from your Activity's or Service's onDestroy() if this 
     * WorkContext is associated with one. After calling this the work context
     * will be invalid and should not be used.
     */
    public void onDestroy()
    {
        checkContext("onDestroy()");
        
        if (mDestroyed) {
            throw new RuntimeException("Destroying already destroyed or not created WorkContext");
        }
        mDestroyed = true;
        
        if (mType != TYPE_LOCAL) {
            int oldCount = getContextTypeCount(TYPE_GLOBAL);
            if (mWorkContexts.remove(this) == false) {
                log("Error: can't remove a work context from the global list");
            }
            if (oldCount == 1 && getContextTypeCount(TYPE_GLOBAL) == 0) {
                //Last global activity / service / whatever got destroyed.
                sendGlobalMessage(new OnAppExitMessage());
            }
        }
        
        cancelAllTasks();
        
        cancelAllTimers();
    }
    
    private static int getContextTypeCount(int type)
    {
        int counter = 0;
        for (WorkContext context : mWorkContexts) {
            if (context.mType == type) {
                counter++;
            }
        }
        return counter;
    }
    
    /**
     * Returns the Android context this WorkContext is associated with.
     */
    public Context getContext() 
    {
        return mContext;
    }
    
    /**
     * Returns true if the WorkContext has been destroyed (it's onDestroy() has
     * been called) or if it has not yet been created.
     */
    public boolean isDestroyed()
    {
        return mDestroyed;
    }
    
    /**
     * Sends a local message to this activity or service and to all views
     * that it contains.
     */
    public void sendLocalMessage(Object message) 
    {
        sendMessagesOnCallback(mContext, message);
    }
    
    /**
     * Sends a global event to all global work contexts and to
     * all views they contain.
     */
    public void sendGlobalMessage(Object message) 
    {
        for (WorkContext workContext : copyList(mWorkContexts)) {
            sendMessagesOnCallback(workContext.getContext(), message);
        }
    }

    private static void sendMessagesOnCallback(Context context, Object event) 
    {
        if (context instanceof Activity) {
            //Enumerate activity's views
            Activity activity = (Activity)context;
            
            Window win = activity.getWindow();
            if (win != null) {
                View decor = win.getDecorView();
                enumViewsOnCallback(decor, event);
            }
            if (activity instanceof ActionBarActivity) {
                ActionBarActivity barActivity = (ActionBarActivity)activity;
                
                FragmentManager manager = barActivity.getSupportFragmentManager();
                if (manager != null) {
                    List<Fragment> fragments = manager.getFragments();
                    if (fragments != null) {
                        for (Fragment frag : new ArrayList<Fragment>(fragments)) {
                            if (frag != null) {
                                callIfMatches(frag, event);
                            }
                        }
                    }
                }
            }
            
            //Call activity callback after view callbacks
        }
        callIfMatches(context, event);
    }
    
    private static void enumViewsOnCallback(View view, Object event) 
    {
        if (view != null) {
            callIfMatches(view, event);
            
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup)view;
                for (int i=0; i < group.getChildCount(); ++i) {
                    enumViewsOnCallback(group.getChildAt(i), event);
                }
            }
        }
    }
    
    private static void callIfMatches(Object obj, Object event)
    {
        Class<?> targetClass = obj.getClass();
        
        Boolean hasEvents = mClassListeners.get(targetClass);
        if (hasEvents != null) {
            if (hasEvents.booleanValue() == false) {
                //We have checked this class before and it does not
                //have any event listeners.
                return;
            }
        }
        
        boolean foundListeners = false;
        Class<?> eventClass = event.getClass();
        
        for (Method m : obj.getClass().getDeclaredMethods()) {
            WorkMessageHandler listenerAnnotation = m.getAnnotation(WorkMessageHandler.class);
            if (listenerAnnotation != null) {
                foundListeners = true;
                
                for (Class<?> type : listenerAnnotation.value()) {
                    if (eventClass.equals(type)) {
                        //This method is a matching listener.
                        if (!m.isAccessible()) {
                            m.setAccessible(true);
                        }
                        
                        try {
                            m.invoke(obj, event);
                            break;
                        } 
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }   
                }
            }
        }
        
        if (hasEvents == null) {
            //Save if we found listeners from this class or not.
            mClassListeners.put(targetClass, foundListeners);
        }
    }
    
    public void log(String text)
    {
        String className = mContext.getClass().getName();
        int dot = className.lastIndexOf(".");
        if (dot != -1) {
            className = className.substring(dot + 1);
        }
        Log.d(className, text);
    }
    
    private void checkContext(String method)
    {
        if (mContext == null) {
            throw new RuntimeException(method + " called but internal context is null");
        }
    }
    
    private <T> ArrayList<T> copyList(List<T> list)
    {
        return new ArrayList<T>(list);
    }
    
    private synchronized void cancelAllTasks()
    {
        for (WorkAsyncTask task : copyList(mTasks)) {
            task.cancel();
        }
        mTasks.clear();
    }
    
    private void cancelAllTimers()
    {
        for (WorkTimer timer : copyList(mTimers)) {
            removeTimer(timer);
        }
        if (mTimers.size() != 0) {
            log("Error: some timers still exist after canceling!");
        }
    }
    
    /**
     * For internal use only.
     */
    public void addTimer(WorkTimer timer, boolean trigger)
    {
        mTimers.add(timer);
        mHandler.postDelayed(timer, trigger ? 0 : timer.getDelay());
    }
    
    /**
     * For internal use only.
     */
    public void removeTimer(WorkTimer timer)
    {
        mTimers.remove(timer);
        mHandler.removeCallbacks(timer);
    }
    
    private void pauseTimers(boolean pause)
    {
        for (WorkTimer timer : copyList(mTimers)) {
            if (timer.getAutopause()) {
                if (pause) {
                    mHandler.removeCallbacks(timer);
                }
                else {
                    mHandler.postDelayed(timer, timer.getTriggerOnStart() ? 0 : timer.getDelay());
                }
            }
        }
    }
    
    public synchronized void dumpStats()
    {
        String separator = "-----------------------------";
        
        log(separator);
        log("Dumping WorkContext stats:");
        log("Global contexts: " + mWorkContexts.size());
        
        int tasks = 0;
        int timers = 0;
        for (WorkContext workContext : mWorkContexts) {
            tasks += workContext.mTasks.size();
            timers += workContext.mTimers.size();
        }
        log("Tracked tasks: " + tasks);
        log("Tracked timers: " + timers);
        
        log(separator);
    }
    
    /**
     * For internal use only.
     */
    public synchronized void addTask(WorkAsyncTask task)
    {
        mTasks.add(task);
    }
    
    /**
     * For internal use only.
     */
    public synchronized void removeTask(WorkAsyncTask task)
    {
        mTasks.remove(task);
    }
    
    public Handler getHandler()
    {
        return mHandler;
    }
    
}
