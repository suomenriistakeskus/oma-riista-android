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
package fi.vincit.androidutilslib.message;

/**
 * Application exit event.
 * 
 * This event will be sent when all global work
 * contexts have been destroyed. In traditional model this could
 * be understood as "exiting" the application. This can be
 * called multiple times, for example if the user starts an activity
 * and close it. Event is then sent. After this if the
 * user starts an activity again and closes it an event
 * is sent again etc.
 * 
 * This is usually useful for flushing some non-critical pending work,
 * for example sending analytics data to server etc.
 */
public class OnAppExitMessage { }
