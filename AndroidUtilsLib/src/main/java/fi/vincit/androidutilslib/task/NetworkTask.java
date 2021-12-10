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

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import cz.msebera.android.httpclient.auth.UsernamePasswordCredentials;
import cz.msebera.android.httpclient.client.CredentialsProvider;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.cache.CacheResponseStatus;
import cz.msebera.android.httpclient.client.cache.HttpCacheContext;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpEntityEnclosingRequestBase;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpHead;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.config.ConnectionConfig;
import cz.msebera.android.httpclient.config.SocketConfig;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.auth.BasicScheme;
import cz.msebera.android.httpclient.impl.client.BasicCredentialsProvider;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.client.LaxRedirectStrategy;
import cz.msebera.android.httpclient.impl.client.cache.CachingHttpClients;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.util.EntityUtils;
import fi.vincit.androidutilslib.config.AndroidUtilsLibConfig;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.factory.HttpClientFactory;
import fi.vincit.androidutilslib.network.ForceCacheInterceptor;
import fi.vincit.androidutilslib.network.HttpLruCacheStorage;
import fi.vincit.androidutilslib.network.ProgressHttpEntityWrapper;
import fi.vincit.androidutilslib.network.SynchronizedCookieStore;
import fi.vincit.androidutilslib.stream.ProgressInputStream;
import fi.vincit.androidutilslib.stream.ProgressStreamListener;
import fi.vincit.androidutilslib.util.JsonSerializator;

/**
 * Base implementation for all network related tasks. This class
 * provides some common functionality for all other tasks
 * that need to stream data from external sources.
 * <p>
 * Although the common usage is to read data from network this
 * task also supports reading local files and resources 
 * using special schemes like res://, assets://, external://
 * and internal://
 *
 * @see fi.vincit.androidutilslib.task.WorkAsyncTask
 */
public abstract class NetworkTask extends WorkAsyncTask
{
    public static final int DEFAULT_TIMEOUT_MS = 10 * 1000; // ten seconds
    
    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        HEAD
    }
    
    public enum PreAuth {
        None,
        Basic
    }
    
    public static final String SCHEME_ASSETS = "assets://";
    public static final String SCHEME_RES = "res://";
    public static final String SCHEME_EXTERNAL = "external://";
    public static final String SCHEME_INTERNAL = "internal://";
    
    private static final Pattern URL_PATH_REGEXP = Pattern.compile("\\{([^}]*)\\}");
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 1024 * 8;
    
    // Connection manager shared by all tasks.
    private static PoolingHttpClientConnectionManager sConnectionManager = null;
    
    static {
        sConnectionManager = new PoolingHttpClientConnectionManager();
        // Make the defaults explicit.
        sConnectionManager.setDefaultMaxPerRoute(2);
        sConnectionManager.setMaxTotal(20);
    }
    
    // Cache shared by all tasks, initialized on first use.
    private static HttpLruCacheStorage sDefaultCacheStorage;
    
    private String mUrl = null;
    private HttpMethod mHttpMethod = HttpMethod.GET;
    private boolean mUseCompression = true;
    private boolean mCacheEnabled = false;
    private long mCacheLifetimeMs = 0;
    private int mTimeout = DEFAULT_TIMEOUT_MS;
    private ArrayList<NameValuePair> mParameters = new ArrayList<>();
    private HashMap<String, String> mHeaders = new HashMap<>();
    private HttpEntity mHttpEntity = null;
    private CredentialsProvider mCredentialsProvider = null;
    private String mContentEncoding = DEFAULT_ENCODING;
    private SynchronizedCookieStore mCookieStore = null;
    private PreAuth mPreAuth = null;
    private int mHttpStatusCode = -1;
    private HttpResponse mHttpResponse = null;
    private HttpRequestBase mHttpRequest = null;
    private HttpLruCacheStorage mCacheStorage = null;
    private HttpClientFactory mHttpClientFactory = new HttpClientFactory();

    /**
     * Creates a new NetworkTask.
     * 
     * @param workContext WorkContext that this task will be associated with.
     * @param baseUrl Base url for the task. Can be null or empty if it will be set later
     *      by calling setBaseUrl() or setBaseUrlPath().
     *  
     */
    public NetworkTask(WorkContext workContext, String baseUrl) 
    {
        super(workContext);
        
        setBaseUrl(baseUrl);
        
        if (sDefaultCacheStorage == null) {
            sDefaultCacheStorage = createDefaultCache();
        }
        mCacheStorage = sDefaultCacheStorage;
    }
    
    public NetworkTask(WorkContext workContext) 
    {
        this(workContext, null);
    }

    private HttpLruCacheStorage createDefaultCache() {
        File cacheDir = new File(getWorkContext().getContext().getCacheDir(), 
                AndroidUtilsLibConfig.Cache.DEFAULT_CACHE_DIR_NAME);

        HttpLruCacheStorage cache = new HttpLruCacheStorage(null);
        cache.createMemoryCache(AndroidUtilsLibConfig.Cache.DEFAULT_MEMORY_CACHE_SIZE);
        
        try {
            cache.createDiskCache(cacheDir, AndroidUtilsLibConfig.Cache.DEFAULT_DISK_CACHE_SIZE);
            log("Using memory and disk cache: " + cacheDir.toString());
        } 
        catch (IOException e) {
            log("Using only memory cache");
        }
        return cache;
    }
    
    @Override
    public void cancel() {
        HttpRequestBase request = mHttpRequest;
        if (request != null) {
            // This is asynchronous, but abort() is synchronized.
            try {
                request.abort();
            }
            catch (Exception e) {
                // No need to do anything.
            }
        }
        super.cancel();
    }
    
    public void setHttpClientFactory(HttpClientFactory factory)
    {
        mHttpClientFactory = factory;
    }
    
    public HttpClientFactory getHttpClientFactory()
    {
        return mHttpClientFactory;
    }
    
    /**
     * Set the cookie store that will be used for request.
     */
    public void setCookieStore(SynchronizedCookieStore store)
    {
        mCookieStore = store;
    }
    
    public SynchronizedCookieStore getCookieStore()
    {
        return mCookieStore;
    }
    
    /**
     * Adds an Url parameter into the base url that was given in
     * the constructor. 
     * 
     * With GET, HEAD and DELETE request the values are added into the url
     * as query string arguments. 
     * 
     * If you use POST or PUT the arguments will be added to the request 
     * body entity, so if you set the request entity by hand these 
     * arguments will be ignored.
     */
    public void addParameter(String key, String value)
    {
        mParameters.add(new BasicNameValuePair(key, value));
    }
    
    /**
     * Sets a header for request that will be send to the server.
     */
    public void setHeader(String key, String value)
    {
        mHeaders.put(key, value);
    }

    /**
     * Removes a header that would have been sent to the server. This only
     * affects headers added by using setHeader(), many headers are
     * automatically added to the request. If you want to manipulate
     * or inspect those implement onAsyncRequest().
     */
    public void removeHeader(String key)
    {
        mHeaders.remove(key);
    }
    
    /**
     * Set an Http entity that will be sent when using POST or PUT.
     * If you create the entity yourself make sure that is has
     * correct encoding set.
     */
    public void setHttpEntity(HttpEntity entity)
    {
        mHttpEntity = entity;
    }
    
    /**
     * Helper method for directly serializing an object
     * into a Json-string entity using the set encoding.
     * Only used in POST and PUT requests.
     */
    public void setJsonEntity(Object obj)
    {
        ObjectMapper mapper = JsonSerializator.getDefaultMapper();
        try {
            String json = mapper.writeValueAsString(obj);
            StringEntity entity = new StringEntity(json, mContentEncoding);
            entity.setContentType("application/json; charset=" + mContentEncoding);
            setHttpEntity(entity);
        } 
        catch (Exception e) {
            // Can fail later.
        }
    }
    
    /**
     * Set the encoding that will be used to encode any textual data
     * which will be sent to the server.
     */
    public void setContentEncoding(String encoding)
    {
        mContentEncoding = encoding;
    }
    
    public String getContentEncoding()
    {
        return mContentEncoding;
    }
    
    /**
     * Set the connection and socket timeout in milliseconds.
     */
    public void setTimeout(int milliseconds)
    {
        mTimeout = milliseconds;
    }
    
    public int getTimeout()
    {
        return mTimeout;
    }
    
    /**
     * Enable compression for data from the server. This has no
     * effect if the url specifies a local file using a special 
     * scheme.
     */
    public void setUseCompression(boolean compression)
    {
        mUseCompression = compression;
    }
    
    public boolean isUseCompression()
    {
        return mUseCompression;
    }

    /**
     * Set the http method type. HTTP_GET is used by default.
     */
    public void setHttpMethod(HttpMethod method)
    {
        mHttpMethod = method;
    }
    
    public HttpMethod getHttpMethod()
    {
        return mHttpMethod;
    }
    
    /**
     * Enabled or disabled network request caching for this task.
     * 
     * The caching has relatively complex behavior and how long
     * and much data is cached depends from the headers sent
     * by the server and space used locally in the client. By
     * default LruCacheStorage is used to store requests. 
     * 
     * If you want to force a request to be cached (for example
     * you know that this is ok) you must call setCacheLifeTime().
     */
    public void setCacheEnabled(boolean enabled)
    {
        mCacheEnabled = enabled;
    }
    
    public boolean isCacheEnabled()
    {
        return mCacheEnabled;
    }
    
    /**
     * Forces the response to be cached according to the
     * given duration in milliseconds. This is not an
     * absolute promise, but if the response is relatively
     * normal this should usually work. You must also enable
     * caching by calling setCacheEnabled().
     */
    public void setCacheLifeTime(long milliseconds)
    {
        mCacheLifetimeMs = milliseconds;
    }
    
    public long getCacheLifetime(){
        return mCacheLifetimeMs;
    }
    
    /**
     * Set the cache storage that will be used instead of the
     * default storage if caching is enabled.
     */
    public void setCacheStorage(HttpLruCacheStorage storage)
    {
        mCacheStorage = storage;
    }
    
    /**
     * Returns the cache storage of this task.
     */
    public HttpLruCacheStorage getCacheStorage()
    {
        return mCacheStorage;
    }
    
    /**
     * Returns the default cache storage or null if it has not
     * been created yet.
     */
    public static HttpLruCacheStorage getDefaultCacheStorage()
    {
        return sDefaultCacheStorage;
    }
    
    /**
     * Returns the default connection manager used by all NetworkTask's.
     */
    public static PoolingHttpClientConnectionManager getDefaultConnectionManager()
    {
        return sConnectionManager;
    }
    
    /**
     * Set the base url of the task. If you use addParameter() those
     * will be added to the base url. Use getFullUrl() to get
     * the full url that will be used when executing the request.
     */
    public void setBaseUrl(String url)
    {
        mUrl = url;
    }
    
    /**
     * Set the base url of the task by replacing {} items in the
     * arguments url by the path arguments. For example:
     * 
     * setBaseUrlPath("http://www.example.com/{user_id}/info/{attribute}", 5, "age");
     * 
     * Would set the base url to "http://www.example/5/info/age". The content
     * between {} does not matter, they are just helpful clarifications.
     * The items are placed into the url in given order.
     */
    public void setBaseUrlPath(String url, Object ... paths)
    {
        Matcher matcher = URL_PATH_REGEXP.matcher(url);
        
        for (Object path : paths) {
            String stringPath = "" + path;
            
            if (matcher.find()) {
                String group = matcher.group();
                url = url.replace(group, stringPath);
            }
            else {
                log("setBaseUrlPath() got unused path element: " + stringPath);
            }
        }
        setBaseUrl(url);
    }
    
    /**
     * Returns the base url of the task.
     */
    public String getBaseUrl()
    {
        return mUrl;
    }
    
    /**
     * Returns the full url of the task. This consists from the 
     * base url and any query string parameters added by using addParameter()
     */
    public String getFullUrl()
    {
        if (mParameters.size() > 0) {
            return mUrl + "?" + URLEncodedUtils.format(mParameters, mContentEncoding);
        }
        return mUrl;
    }
    
    /**
     * Returns the HTTP status code that was sent by the server 
     * or -1 if there has been no response.
     */
    public int getHttpStatusCode()
    {
        return mHttpStatusCode;
    }
    
    /**
     * Returns the HTTP response that the server sent or null if there
     * is no response available.
     */
    public HttpResponse getHttpResponse()
    {
        return mHttpResponse;
    }
    
    public void setHttpStatusCode(int status)
    {
        mHttpStatusCode = status;
    }
    
    /**
     * Set credentials that will be used for HTTP authentication (basic or digest).
     */
    public void setHttpAuth(String userName, String password, PreAuth preAuth) 
    {
        mCredentialsProvider = new BasicCredentialsProvider();
        mCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
        mPreAuth = preAuth;
    }
    
    protected boolean shouldUseCache()
    {
        return mCacheEnabled && (mCacheStorage != null) && (mHttpMethod == HttpMethod.GET);
    }
      
    private HttpClient createHttpClient()
    {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (shouldUseCache()) {
            builder = CachingHttpClients.custom()
                    .setHttpCacheStorage(mCacheStorage)
                    .setCacheConfig(mCacheStorage.getConfig());
        }
        builder = mHttpClientFactory.newClient(this, builder);
        
        // Required for example with Vincit demo server which uses proxying.
        builder.setRedirectStrategy(new LaxRedirectStrategy());
        
        builder.setConnectionManager(sConnectionManager);
        
        builder.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setBufferSize(8192)
                .build());
        
        builder.setDefaultSocketConfig(SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .setSoTimeout(mTimeout)
                .build());
        
        boolean useExpectContinue = (mHttpMethod == HttpMethod.POST) || (mHttpMethod == HttpMethod.PUT);
        builder.setDefaultRequestConfig(RequestConfig.custom()
                .setExpectContinueEnabled(useExpectContinue)
                .setStaleConnectionCheckEnabled(true)
                .setConnectionRequestTimeout(mTimeout)
                .setConnectTimeout(mTimeout)
                .setSocketTimeout(mTimeout)
                .build());
        
        builder.setDefaultCookieStore(mCookieStore);
        builder.setDefaultCredentialsProvider(mCredentialsProvider);
        
        if (shouldUseCache() && mCacheLifetimeMs > 0) {
            builder.addInterceptorFirst(new ForceCacheInterceptor(mCacheLifetimeMs));
        }
        
        if (!mUseCompression) {
            builder.disableContentCompression();
        }

        mHttpClientFactory.configureClient(builder);
        
        return builder.build();
    }
    
    private HttpRequestBase createHttpRequest(String fullUrl) throws Exception
    {
        HttpRequestBase request = null;
        
        if (mHttpMethod == HttpMethod.GET) {
            request = new HttpGet(fullUrl);
        }
        else if (mHttpMethod == HttpMethod.POST || mHttpMethod == HttpMethod.PUT) {
            HttpEntityEnclosingRequestBase entityRequest = null;
            if (mHttpMethod  == HttpMethod.POST) {
                entityRequest = new HttpPost(mUrl);
            }
            else {
                entityRequest = new HttpPut(mUrl);
            }
            if (mHttpEntity != null) {
                entityRequest.setEntity(new ProgressOutEntityWrapper(mHttpEntity));
            }
            else if (mParameters.size() > 0) {
                entityRequest.setEntity(new ProgressOutEntityWrapper(
                        new UrlEncodedFormEntity(mParameters, mContentEncoding)));
            }
            request = entityRequest;
        }
        else if (mHttpMethod == HttpMethod.DELETE) {
            request = new HttpDelete(fullUrl);
        }
        else if (mHttpMethod == HttpMethod.HEAD) {
            request = new HttpHead(fullUrl);
        }
        else {
            throw new Exception("Unknown HTTP method: " + mHttpMethod);
        }
        
        request.setHeader("User-Agent", AndroidUtilsLibConfig.Task.DEFAULT_USER_AGENT);

        // Set user given headers
        for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
        return request;
    }
    
    private void handlePreAuth(HttpRequestBase request, BasicHttpContext httpContext) throws AuthenticationException
    {
        if (mCredentialsProvider == null || mPreAuth == null || mPreAuth == PreAuth.None) {
            return;
        }
        
        if (mPreAuth == PreAuth.Basic) {
            BasicScheme basic = new BasicScheme();
            Header authHeader = basic.authenticate(
                    mCredentialsProvider.getCredentials(AuthScope.ANY), request, httpContext);
            request.setHeader(authHeader);
        }
    }
    
    protected File getFileFromUrl(String path)
    {
        File file = null;
        
        if (path.startsWith(SCHEME_EXTERNAL)) {
            String filePath = path.substring(SCHEME_EXTERNAL.length());
            file = new File(Environment.getExternalStorageDirectory(), filePath);
        }
        else if (path.startsWith(SCHEME_INTERNAL)) {
            String filePath = path.substring(SCHEME_INTERNAL.length());
            file = new File(getWorkContext().getContext().getFilesDir(), filePath);
        }
        return file;
    }
    
    private InputStream tryLocalFiles() throws IOException {
        InputStream localFile = null;
        long contentSize = -1;
        Context context = getWorkContext().getContext();
        
        if (mUrl.startsWith(SCHEME_RES)) {
            String[] parts = mUrl.split("/");
            String folder = parts[2];
            String file = parts[3];
            
            Resources resources = context.getResources();
            int resId = resources.getIdentifier(file, folder, context.getPackageName());
            log("Opening resource: " + mUrl + ", id: " + resId);
            localFile = resources.openRawResource(resId);
        }
        else if (mUrl.startsWith(SCHEME_ASSETS)) {
            String assetPath = mUrl.substring(SCHEME_ASSETS.length());
            log("Opening asset: " + mUrl);
            localFile = getWorkContext().getContext().getAssets().open(assetPath);
        }
        else {
            File file = getFileFromUrl(mUrl);
            if (file != null) {
                localFile = new FileInputStream(file);
                contentSize = file.length();
            }
        }

        if (localFile != null) {
            return new ProgressStream(new BufferedInputStream(localFile, BUFFER_SIZE), contentSize);
        }
        return null;
    }

    @Override
    protected void onAsyncRun() throws Exception
    {
        mHttpRequest = null;
        mHttpResponse = null;
        mHttpStatusCode = -1;

        final InputStream localFile = tryLocalFiles();

        if (localFile != null) {
            try {
                onAsyncStream(localFile);
            }
            finally {
                localFile.close();
            }
            return;
        }

        final String fullUrl = getFullUrl();
        final HttpClient client = createHttpClient();
        final BasicHttpContext httpContext = new BasicHttpContext();

        mHttpRequest = createHttpRequest(fullUrl);

        handlePreAuth(mHttpRequest, httpContext);

        HttpResponse response = null;

        try {
            // Last chance to modify the request.
            onAsyncRequest(mHttpRequest);

            response = client.execute(mHttpRequest, httpContext);
            if (response == null) {
                throw new RuntimeException("No HTTP response from: " + fullUrl);
            }
            logResponseStatus(httpContext, fullUrl);

            mHttpResponse = response;

            if (response.getStatusLine() == null) {
                throw new RuntimeException("No HTTP status line from: " + fullUrl);
            }
            mHttpStatusCode = response.getStatusLine().getStatusCode();

            // First chance to mess with the response.
            onAsyncResponse(response);

            // Process the stream
            handleAsyncResponse(response, fullUrl);
        }
        catch (final Exception e) {
            mHttpRequest.abort();

            throw e;
        }
        finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
            mHttpRequest.releaseConnection();
        }
    }

    private void handleAsyncResponse(HttpResponse response, String fullUrl) throws Exception
    {
        boolean errorStream = false;
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode > 299) {
            errorStream = true;
        }
        
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            if ((mHttpMethod == HttpMethod.HEAD || mHttpMethod == HttpMethod.POST || mHttpMethod == HttpMethod.PUT || mHttpMethod == HttpMethod.DELETE)  && !errorStream) {
                // OK for a these responses to have no response entity body.
                return;
            }
            else {
                throw new Exception("No response body from: " + fullUrl + ", status " + statusCode);
            }
        }
        
        long contentLength = entity.getContentLength();
        ProgressStream in = new ProgressStream(new BufferedInputStream(entity.getContent(), BUFFER_SIZE), contentLength);
        
        try {
            if (errorStream) {
                onAsyncStreamError(in);
                throw new Exception("HTTP status code for " + fullUrl + " was " + statusCode);
            }
            else {
                onAsyncStream(in);
            }
        }
        finally {
            in.close();
        }
    }
    
    private void logResponseStatus(BasicHttpContext httpContext, String fullUrl)
    {
        CacheResponseStatus cacheStatus = (CacheResponseStatus)httpContext.getAttribute(HttpCacheContext.CACHE_RESPONSE_STATUS);
        if (cacheStatus != null) {
            log(cacheStatus.name() + ": " + fullUrl);
        }
        else {
            log("Not cached: " + fullUrl);
        }
    }
    
    protected static int progressToPercent(long current, long total)
    {
        int percetage = (int)((current / (double)total) * 100.0);
        return Math.min(Math.max(percetage, 0), 100);
    }
    
    /**
     * Wraps an input stream so that progress callbacks and IO cancellation can be implemented.
     */
    private class ProgressStream extends ProgressInputStream
    {
        private long mContentLength = -1;
        
        protected ProgressStream(InputStream in, long contentLen) 
        {
            super(in);
            
            mContentLength = contentLen;
            
            setProgressStreamListener(new ProgressStreamListener() 
            {
                @Override
                public void onProgress(long bytes) throws IOException
                {
                    if (isCancelled()) {
                        throw new IOException("NetworkTask IO read canceled");
                    }
                    
                    if (mContentLength > 0) {
                        setInternalProgress(progressToPercent(bytes, mContentLength));
                    }
                }
            });
        }
    }
    
    /**
     * Wrap a POST or PUT entity so that upload progress callback and upload
     * cancellation can be implemented.
     */
    private class ProgressOutEntityWrapper extends ProgressHttpEntityWrapper
    {
        private long mEntityLength = -1;
        private int mProgress = -1;
        
        public ProgressOutEntityWrapper(HttpEntity wrapped) 
        {
            super(wrapped);
            
            mEntityLength = wrapped.getContentLength();
            
            setProgressStreamListener(new ProgressStreamListener() 
            {
                @Override
                public void onProgress(long bytes) throws IOException
                {
                    if (isCancelled()) {
                        throw new IOException("NetworkTask IO write canceled");
                    }
                    
                    if (mEntityLength > 0) {
                        int progress = progressToPercent(bytes, mEntityLength);
                        if (progress > mProgress) {
                            mProgress = progress;
                            setInternalUploadProgress(mProgress);
                        }
                    }
                }
            });
        }
    }
    
    private void setInternalUploadProgress(final int progress)
    {
        getWorkContext().getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (isRunning()) {
                    onProgressUpload(progress);
                }
            }
        });
    }
    
    /**
     * This method is called with the stream containing the request data.
     * The stream argument will be buffered or otherwise efficient to read, there is
     * not need to wrap it in another buffered stream.
     */
    protected abstract void onAsyncStream(InputStream stream) throws Exception;

    /**
     * This method is pretty much the same as onAsyncStream() except that this
     * is only called if the HTTP response code is not in 2xx range. Also in this
     * case onAsyncStream() will not be called.
     */
    protected void onAsyncStreamError(InputStream stream) throws Exception
    {
    }
    
    /**
     * This method is called with the request just before the request
     * is actually executed. This is the last chance to modify or
     * check the request.
     */
    protected void onAsyncRequest(HttpRequestBase request)
    {
    }
    
    /**
     * This method is called right after the request has been executed
     * and a response has been received from the server.
     */
    protected void onAsyncResponse(HttpResponse response)
    {
    }
    
    /**
     * Similar to onProgress(), but this is called when uploading entities with
     * POST or PUT. This is usually only useful with entities that are streamed
     * from a file etc., otherwise you usually get only one progress notification
     * when the write has finished.
     */
    protected void onProgressUpload(int progress)
    {
    }
}
