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
package fi.vincit.androidutilslib.listener;

import fi.vincit.androidutilslib.task.WorkAsyncTask;

/**
 * Helper class that implements the work task listener with
 * empty methods so that you don't have to implement all
 * methods if you only want to handle one event.
 */
public class BaseWorkAsyncTaskListener implements WorkAsyncTask.TaskListener {
    @Override
    public void onStart(WorkAsyncTask task) { 
    }

    @Override
    public void onRetry(WorkAsyncTask task, int count) {
    }

    @Override
    public void onProgress(WorkAsyncTask task, int progress) {
    }

    @Override
    public void onFinish(WorkAsyncTask task) {
    }

    @Override
    public void onError(WorkAsyncTask task) {
    }

    @Override
    public void onEnd(WorkAsyncTask task) {
    }

    @Override
    public void onCancel(WorkAsyncTask task) {
    }
}
