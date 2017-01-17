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

import fi.vincit.androidutilslib.task.NetworkTask;
import fi.vincit.httpclientandroidlib.conn.ClientConnectionManager;
import fi.vincit.httpclientandroidlib.impl.client.AbstractHttpClient;
import fi.vincit.httpclientandroidlib.impl.client.DefaultHttpClient;
import fi.vincit.httpclientandroidlib.impl.client.HttpClientBuilder;

/**
 * Factory class for creating HTTP client's for a NetworkTask. 
 * If you implement your own factory it must be thread safe.
 */
public class HttpClientFactory
{
    /**
     * Called when a HTTP client is required for making a connection.
     * The task parameter is the NetworkTask for which the HttpClient
     * will be used with. The builder argument is a builder that has been
     * created for with reasonable default values. You can either return
     * that or create a completely new builder. You should not configure
     * the builder here, do that in configureClient().
     */
    public HttpClientBuilder newClient(NetworkTask task, HttpClientBuilder builder)
    {
        //Use defaults
        return builder;
    }
    
    /**
     * Called when all default configuration has been done for a builder. You
     * can override any configuration settings in here.
     */
    public void configureClient(HttpClientBuilder builder)
    {
        //Use defaults
    }
}
