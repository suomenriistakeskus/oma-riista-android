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
package fi.vincit.androidutilslib.factory;

import fi.vincit.androidutilslib.task.WorkAsyncTask;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.widget.Toast;

/**
 * Factory for creating task UI notifications. Note that all methods should only
 * create the dialogs and toasts, not show them. The task will manage showing
 * and canceling the dialogs.
 */
public class TaskDialogFactory 
{
    /**
     * Called when a generic message dialog should be created with the given message text.
     */
    public AlertDialog newDialog(WorkAsyncTask task, String message) 
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(task.getWorkContext().getContext());
        builder.setMessage(message);
        builder.setNeutralButton("OK", null);
        return builder.create();
    }
    
    /**
     * Called when a progress dialog should be created with the given message text.
     * 
     * The returned dialog can be further configured by the task, for example by
     * adding a dismiss listener if the task is user cancellable.
     */
    public AlertDialog newProgressDialog(WorkAsyncTask task, String message) 
    {
        ProgressDialog dialog = new ProgressDialog(task.getWorkContext().getContext());
        dialog.setMessage(message);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
    
    /**
     * Called when a toast notifications should be created with the given message text.
     */
    public Toast newToast(WorkAsyncTask task, String message) 
    {
        return Toast.makeText(task.getWorkContext().getContext(), message, Toast.LENGTH_LONG);
    }
}
