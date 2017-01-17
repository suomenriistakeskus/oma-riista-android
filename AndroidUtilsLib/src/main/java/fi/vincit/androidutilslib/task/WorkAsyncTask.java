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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import fi.vincit.androidutilslib.android.AndroidAsyncTask;
import fi.vincit.androidutilslib.config.AndroidUtilsLibConfig;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.factory.TaskDialogFactory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;
import android.widget.Toast;

/**
 * This class provides some common functionality for all other
 * tasks. Tasks are associated with a WorkContext and share
 * it's lifetime. If the WorkContext is destroyed all it's
 * tasks are automatically canceled.
 * <p>
 * Tasks are fire-and-forget objects, don't re-use them (that
 * is, call start() multiple times).
 * <p>
 * As a rule of thumb all method names that have "Async" in them
 * are called from the the background processing thread.
 * <p>
 * The lifetime of a typical asynchronous task goes like this:
 * <p>
 * -task.start() and other similar methods queue the task for execution.<br/>
 * -task.onStart() is called before the task starts running.<br/>
 * -task.onAsyncRun() is called in the background thread.<br/>
 * -task.onProgress() is called if the task configuration supports progress notification.<br/>
 * -task.onFinish() is called if the task has succeeded<br/>
 * -task.onError() is called if the task has failed. This means that onAsyncRun() has thrown<br/>
 *  an exception. onFinish() will not be called in this case.<br/>
 * -task.onEnd() is called in all situations when the task has finished running.<br/>
 * -task.onRetry() is called each time the task retries.<br/>
 * -task.onCancel() will be called if the task has been cancelled, either by calling task.cancel() 
 *  or if the WorkContext the task is associated with is destroyed.
 */
public abstract class WorkAsyncTask
{
    /**
     * Interface for listening task events. Also see BaseWorkAsyncTaskListener.
     */
    public static interface TaskListener
    {
        public void onStart(WorkAsyncTask task);
        public void onRetry(WorkAsyncTask task, int count);
        public void onProgress(WorkAsyncTask task, int progress);
        public void onFinish(WorkAsyncTask task);
        public void onError(WorkAsyncTask task);
        public void onEnd(WorkAsyncTask task);
        public void onCancel(WorkAsyncTask task);
    }
    
    private ArrayList<TaskListener> mListeners = new ArrayList<TaskListener>();
    private InternalTask mInternalTask;
    private WorkContext mWorkContext = null;
    private Exception mError = null;
    private long mDebugSleepTime = 0;
    private int mCurrentProgress = -1;
    private int mTryCount = 1;
    private long mTryWaitMs = 0;
    private TaskDialogFactory mDialogFactory = new TaskDialogFactory();
    
    //Progress dialog
    private String mProgressDialogText;
    private boolean mProgressDialogCancelable = false;
    private AlertDialog mProgressDialog = null; 
    
    //Toast messages
    private String mErrorToastText;
    private String mFinishToastText;
    
    //Error dialog
    private String mErrorDialogText;
    private AlertDialog mErrorDialog;
    
    public WorkAsyncTask(WorkContext context)
    {
        mWorkContext = context;
        mInternalTask = new InternalTask();
    }

    /**
     * Add a task listener that will be notified when about the
     * changes in the task state.
     */
    public void addTaskListener(TaskListener listener)
    {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    
    public void removeTaskListener(TaskListener listener) 
    {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }
    
    public void setTaskDialogFactory(TaskDialogFactory factory)
    {
        mDialogFactory = factory;
    }
    
    /**
     * Call this if this task should show a progress dialog during
     * it's run. If the dialog is set to cancellable user can cancel
     * the dialog with device back button and it will also cancel
     * the task.
     */
    public void setProgressDialog(int stringId, boolean cancellable)
    {
        if (stringId != 0) {
            setProgressDialog(mWorkContext.getContext().getString(stringId), cancellable);  
        }
        else {
            setProgressDialog(null, false);
        }
    }
    
    public void setProgressDialog(String message, boolean cancelable)
    {
        mProgressDialogText = message;
        mProgressDialogCancelable = cancelable;
    }
    
    /**
     * Sets the toast error message that is shown to user
     * if the task fails.
     */
    public void setErrorToast(int stringId)
    {
        if (stringId != 0) {
            setErrorToast(mWorkContext.getContext().getString(stringId));
        }
        else {
            setErrorToast(null);
        }
    }
    
    public void setErrorToast(String text)
    {
        mErrorToastText = text;
    }
    
    /**
     * Sets the toast error message that is shown to user
     * if the task finishes successfully.
     */
    public void setFinishToast(int stringId)
    {
        if (stringId != 0) {
            setFinishToast(mWorkContext.getContext().getString(stringId));
        }
        else {
            setFinishToast(null);
        }
    }
    
    public void setFinishToast(String text)
    {
        mFinishToastText = text;
    }
    
    /**
     * Sets the dialog error message that is shown to user
     * if the task fails.
     */
    public void setErrorDialog(int stringId) 
    {
        setErrorDialog(mWorkContext.getContext().getString(stringId));
    }
    
    public void setErrorDialog(String message) 
    {
        mErrorDialogText = message;
    }
    
    /**
     * This can be used to set a initial sleep time for the task.
     * Can be useful in testing to make sure that long running
     * tasks do not expose any bugs or other issues.
     * 
     * USE ONLY WHEN TESTING.
     */
    public void setDebugSleep(long timeMs) 
    {
        mDebugSleepTime = timeMs;
    }
    
    /**
     * Set the try count for this task. The task will try to run this many times
     * until it finishes successfully (onAsyncRun() succeeds) or is canceled. By default 
     * this count is 1. If count is greater than one and the task retires onRetry() is 
     * also called for each retry. Use some judgment when setting the count, many tasks
     * don't really need this.
     * 
     * Also if you are using NetworkTask or a task that inherits from it make sure that you know 
     * how long the timeout is. For example if it is 8 seconds and you set the try count 
     * to 3 at worst the task will report failure after 24 seconds. Something to keep in mind
     * if you are showing progress to the user.
     */
    public void setTryCount(int count)
    {
        if (count > 0) {
            mTryCount = count;
        }
    }
    
    public int getTryCount()
    {
        return mTryCount;
    }
    
    /**
     * If setTryCount() is greater than one and the task retries the background
     * thread will first wait this amount of time before retrying. By default
     * this is 0 which means no wait between retries.
     */
    public void setTryWait(long milliseconds)
    {
        mTryWaitMs = milliseconds;
    }
    
    public long getTryWait()
    {
        return mTryWaitMs;
    }
    
    /**
     * Starts to execute the task. After this the task will be running
     * in a different thread so most task methods should not be called after
     * this. One exception to this is cancel().
     */
    public void start()
    {
        startParallel();
    }
    
    /**
     * Starts to execute the task serially using an internal thread pool.
     */
    public void startSerial() 
    {
        mWorkContext.addTask(this);
        try {
            mInternalTask.execute();
        }
        catch (RejectedExecutionException e) {
            handleRejected(e);
        }
    }
    
    /**
     * Starts to execute the task using an internal thread pool. Depending on 
     * your use case you might want to use serial execution in cases where it is 
     * nice to know that the tasks are run in a predictable order.
     */
    public void startParallel() 
    {
        startOnExecutor(AndroidAsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    /**
     * Starts to execute the task using a custom executor.
     */
    public void startOnExecutor(Executor executor) 
    {
        mWorkContext.addTask(this);
        try {
            mInternalTask.executeOnExecutor(executor);
        }
        catch (RejectedExecutionException e) {
            handleRejected(e);
        }
    }
    
    private void handleRejected(RejectedExecutionException e) 
    {
        handleEndTask();
        
        //We can't even start this so "emulate" the behavior.
        //This is tricky because they will come immediately
        //in the user code so all states might not be ready for them...

        onStart();
        for (TaskListener listener : copyListeners()) {
            listener.onStart(this);
        }
        
        setError(e);
        
        onError();
        for (TaskListener listener : copyListeners()) {
            listener.onError(this);
        }

        onEnd();
        for (TaskListener listener : copyListeners()) {
            listener.onEnd(this);
        }
    }
    
    /**
     * Attempts to wait until the task has ended (failed, cancelled
     * or finished). This blocks the calling thread so this is only
     * useful if you want to use the task in a thread that is already
     * separate from the UI thread. If the timeout (in milliseconds) 
     * is 0 or negative waits until the task ends, otherwise waits for 
     * the given amount and then gives up. Returns true if the wait succeeded.
     */
    public boolean waitForEnd(long timeoutMs) 
    {
        boolean ok = false;
        try {
            if (timeoutMs > 0) {
                mInternalTask.get(timeoutMs, TimeUnit.MILLISECONDS);
            } 
            else {
                mInternalTask.get();
            }
            ok = true;
        }
        catch (Exception e) {

        }
        return ok;
    }

    /**
     * Cancels the task. This means that no callbacks will be
     * called except for onCancel() and onEnd(). Asynchronous processing
     * may continue in the background thread but it's results will
     * be discarded when it finishes. Many tasks also support
     * early cancellation (for example NetworkTask and most of
     * it's subclasses).
     */
    public void cancel() 
    {
        mInternalTask.cancel(false);
    }
    
    /**
     * Returns true if the task has been canceled. Asynchronous processing
     * might still be running in a background thread even if the task
     * has been cancelled.
     */
    public boolean isCancelled()
    {
        return mInternalTask.isCancelled();
    }
    
    /**
     * Returns true if the task is currently running and has not been cancelled. 
     * Because tasks are queued this can return false after calling start().
     */
    public boolean isRunning()
    {
        return (mInternalTask.getStatus() == AndroidAsyncTask.Status.RUNNING) && !isCancelled();
    }
    
    /**
     * Returns true if the task is currently waiting for execution.
     */
    public boolean isPending()
    {
        return mInternalTask.getStatus() == AndroidAsyncTask.Status.PENDING;
    }
    
    /**
     * Returns the work context that this task is associated with.
     */
    public WorkContext getWorkContext()
    {
        return mWorkContext;
    }
    
    /**
     * Returns the exception that caused the task to fail. Useful in
     * onError() or onEnd() to log or show to the user what has gone
     * wrong. Other task may offer more detailed reporting methods.
     * Returns null if the task has not failed. If the task retries
     * each retry clears the previous error.
     */
    public Exception getError()
    {
        return mError;
    }
    
    protected void setError(Exception error)
    {
        mError = error;
    }
    
    protected void log(String text)
    {
        if (AndroidUtilsLibConfig.Task.ENABLE_LOGGING) {
            String logTag = AndroidUtilsLibConfig.Task.DEFAULT_LOG_TAG;
            if (logTag != null) {
                Log.d(logTag, text);
            }
            else {
                String className = getClass().getName();
                int dot = className.lastIndexOf(".");
                if (dot != -1) {
                    className = className.substring(dot + 1);
                }
                Log.d(className, text);
            }
        }
    }
    
    private ArrayList<TaskListener> copyListeners() 
    {
        return new ArrayList<TaskListener>(mListeners);
    }
    
    private void handleEndTask()
    {
        mWorkContext.removeTask(this);
        
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
            mProgressDialog = null;
        }
        
        if (mErrorDialog != null) {
            mErrorDialog.cancel();
            mErrorDialog = null;
        }
    }
    
    private AlertDialog createProgressDialog(String text)
    {
        AlertDialog dialog = mDialogFactory.newProgressDialog(this, text);
        dialog.setCancelable(mProgressDialogCancelable);
        if (mProgressDialogCancelable) {
            dialog.setOnDismissListener(new OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog) 
                {
                    cancel();
                }
            });
        }
        return dialog;
    }
    
    private AlertDialog createErrorDialog(String text)
    {
        return mDialogFactory.newDialog(this, text);
    }
    
    private Toast createToast(String text)
    {
        return mDialogFactory.newToast(this, text);
    }
    
    protected void sleepMs(long time)
    {
        try {
            Thread.sleep(time);
        } 
        catch (InterruptedException e1) {
        }
    }
    
    private void handleRetry(final int retry)
    {
        mCurrentProgress = -1;
        setError(null);
        
        getWorkContext().getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (isRunning()) {
                    onRetry(retry);
                    for (TaskListener listener : copyListeners()) {
                        listener.onRetry(WorkAsyncTask.this, retry);
                    }
                }
            }
        });
        
        if (mTryWaitMs > 0) {
            sleepMs(mTryWaitMs);
        }
    }
    
    private class InternalTask extends AndroidAsyncTask<Void, Integer, Void> 
    {
        @Override
        final protected Void doInBackground(Void... arg0) 
        {
            if (mDebugSleepTime > 0) {
                log("Debug sleep: " + mDebugSleepTime);
                sleepMs(mDebugSleepTime);
            }
            
            for (int i=0; i < mTryCount; ++i) {
                if (i > 0) {
                    handleRetry(i);
                }
                
                try {
                    onAsyncRun();
                    break; //Succeeded
                }
                catch (Exception e) {
                    if (isCancelled()) {
                        //Cancelled, do not retry.
                        break;
                    }
                    else {
                        log("onAsyncRun() error: " + e.getMessage());
                        
                        setError(e);
                    }
                }
            }
            return null;
        }
        
        @Override
        final protected void onPreExecute()
        {
            if (mProgressDialogText != null) {
                mProgressDialog = createProgressDialog(mProgressDialogText);
                mProgressDialog.show();
            }
            onStart();
            for (TaskListener listener : copyListeners()) {
                listener.onStart(WorkAsyncTask.this);
            }
        }

        @Override
        final protected void onPostExecute(Void result)
        {
            handleEndTask();
            
            if (mError != null) {
                if (mErrorToastText != null) {
                    createToast(mErrorToastText).show();
                }
                
                if (mErrorDialogText != null) {
                    mErrorDialog = createErrorDialog(mErrorDialogText);
                    mErrorDialog.show();
                }
                
                onError();
                for (TaskListener listener : copyListeners()) {
                    listener.onError(WorkAsyncTask.this);
                }
            }
            else {
                if (mFinishToastText != null) {
                    createToast(mFinishToastText).show();
                }
                
                onFinish();
                for (TaskListener listener : copyListeners()) {
                    listener.onFinish(WorkAsyncTask.this);
                }
            }
            onEnd();
            for (TaskListener listener : copyListeners()) {
                listener.onEnd(WorkAsyncTask.this);
            }
            mListeners.clear();
        }
        
        @Override
        final protected void onCancelled()
        {
            handleEndTask();
            
            onCancel();
            for (TaskListener listener : copyListeners()) {
                listener.onCancel(WorkAsyncTask.this);
            }
            
            onEnd();
            for (TaskListener listener : copyListeners()) {
                listener.onEnd(WorkAsyncTask.this);
            }
            mListeners.clear();
        }
        
        @Override
        final protected void onProgressUpdate(Integer... values)
        {
            if (isCancelled()) {
                return;
            }
            int progress = values[0];
            
            onProgress(progress);
            for (TaskListener listener : copyListeners()) {
                listener.onProgress(WorkAsyncTask.this, progress);
            }
        }
        
        public void publishProgressInternal(int value)
        {
            publishProgress(value);
        }
    }
    
    protected void setInternalProgress(int value)
    {
        if (value < 0) {
            value = 0;
        }
        else if (value > 100) {
            value = 100;
        }
        if (value > mCurrentProgress) {
            mCurrentProgress = value;
            mInternalTask.publishProgressInternal(mCurrentProgress);
        }
    }
    
    /**
     * Called in the UI thread before the asynchronous processing is started.
     */
    protected void onStart()
    {
    }
    
    /**
     * Called in the UI thread if the task has encountered an error and
     * has been retried. This will only be called if setTryCount() has
     * been called with a count greater than 1 and the task retries. The
     * first run is not considered a retry. Do not use this method
     * to reset your data structures etc. as onAsyncRun() might have
     * already been retried before this gets called.
     * 
     * @param count Current retry count. Starts at 1.
     */
    protected void onRetry(int count)
    {
    }
    
    /**
     * Called in the worker thread. All slow processing should be
     * performed in this callback. If an exception is raised
     * it is assumed that the processing failed and onError()
     * will be called later unless the task retries. Implementations
     * should be prepared to the fact that this method can be called multiple
     * times.
     */
    protected abstract void onAsyncRun() throws Exception;
    
    /**
     * Called in the UI thread during the onAsyncRun(). Some task implementations
     * implement this, but not all. For example NetworkTask and many of it's
     * subclasses support this, but only if the server reports the content
     * length. This method will be called at most 100 times unless the task is
     * retried in which case every retry resets the progress back to 0.
     * 
     * @param progress Value from 0 to 100.
     */
    protected void onProgress(int progress)
    {
    }
    
    /**
     * Called in the UI thread if onAsyncRun() has successfully completed
     * and has not been canceled.
     */
    protected void onFinish()
    {
    }

    /**
     * Called in the UI thread if onAsyncRun() has failed.
     */
    protected void onError()
    {
    }
    
    /**
     * Called in the UI thread when the task has been canceled.
     */
    protected void onCancel()
    {
    }
    
    /**
     * Called in the UI thread when the task has performed it's actions
     * after onFinish(), onError() or onCancel(). This is a
     * place for operations that should be always done when
     * the task has been completed in one way or another.
     */
    protected void onEnd()
    {
    }
}
