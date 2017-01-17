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

import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fi.vincit.androidutilslib.context.WorkContext;

import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

/**
 * Task for parsing XML-files with a SAX-parser. This just helps in parsing elements,
 * if you want to serialize objects automatically use SaxObjectTask.
 */
public class XmlSaxTask extends NetworkTask 
{
    /**
     * Annotate your methods with this to receive asynchronous calls
     * every time a new element with the given value has been encountered. 
     * The method should accept one argument: 
     * 
     * -Map<String, String>. These will be the immediate attributes of the tag.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface AsyncStartElementHandler {
        String value();
    }
    
    /**
     * Annotate your methods with this to receive asynchronous calls
     * every time a element end tag has been encountered. The method
     * should accept two arguments: 
     * 
     * -Map<String, String>: these will be the immediate attributes of the tag.
     * -String: the text content between the start and end-tag.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface AsyncEndElementHandler {
        String value();
    }
    
    private String mEncoding = null;
    private HashMap<String, Method> mStartHandlers = new HashMap<String, Method>();
    private HashMap<String, Method> mEndHandlers = new HashMap<String, Method>();

    public XmlSaxTask(WorkContext context) 
    {
        this(context, null);
    }
    
    public XmlSaxTask(WorkContext context, String url) 
    {
        super(context, url);
        
        scanHandlers();
    }
    
    private void scanHandlers() 
    {
        for (Method m : getClass().getDeclaredMethods()) {
            if (!m.isAccessible()) {
                m.setAccessible(true);
            }
            
            if (m.isAnnotationPresent(AsyncStartElementHandler.class)) {
                AsyncStartElementHandler handler = m.getAnnotation(AsyncStartElementHandler.class);
                mStartHandlers.put(handler.value(), m);
            }
            
            if (m.isAnnotationPresent(AsyncEndElementHandler.class)) {
                AsyncEndElementHandler handler = m.getAnnotation(AsyncEndElementHandler.class);
                mEndHandlers.put(handler.value(), m);
            }
        }
    }
    
    /**
     * Set the encoding that the the SAX-parser should use. If set to
     * null (which is the default) the parser will try to autodetect
     * the encoding.
     */
    public void setSaxEncoding(String encoding)
    {
        mEncoding = encoding;
    }
    
    public String getSaxEncoding()
    {
        return mEncoding;
    }

    @Override
    protected final void onAsyncStream(InputStream stream) throws Exception 
    {
        TaskXMLHandler handler = new TaskXMLHandler();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        InputSource is = new InputSource(stream);
        if (mEncoding != null) {
            is.setEncoding(mEncoding);
        }
        parser.parse(is, handler);
    }
    
    private class TaskXMLHandler extends DefaultHandler
    {
        private StringBuilder mContentBuilder = new StringBuilder();
        private StringBuilder mPathBuilder = new StringBuilder();
        private ArrayList<String> mTagStack = new ArrayList<String>();
        private HashMap<String, String> mAttributes = new HashMap<String, String>(); 
        
        private String getXMLPath()
        {
            mPathBuilder.setLength(0);
            
            final int stackSize = mTagStack.size();
            for (int i=0; i < stackSize; ++i) {
                mPathBuilder.append(mTagStack.get(i));
                if (i < stackSize - 1) {
                    mPathBuilder.append('/');
                }
            }
            return mPathBuilder.toString();
        }
        
        @Override
        public void startDocument()
        {
            onAsyncDocumentStart();
        }
        
        @Override
        public void endDocument()
        {
            onAsyncDocumentEnd();
        }
        
        @Override
        public void startElement(String uri, String name, String qName, Attributes atts) throws SAXException 
        {
            mAttributes.clear();
            for (int i=0; i < atts.getLength(); ++i) {
                String attName = atts.getLocalName(i);
                mAttributes.put(attName, atts.getValue(i));
            }
            
            mContentBuilder.setLength(0);
            
            mTagStack.add(name);

            String xmlPath = getXMLPath();
            
            onAsyncElementStart(xmlPath, name, mAttributes);
            
            Method method = mStartHandlers.get(xmlPath);
            if (method != null) {
                try {
                    method.invoke(XmlSaxTask.this, mAttributes);
                } 
                catch (Exception e) {
                    throw new RuntimeException(e);
                } 
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int end)
        {
            mContentBuilder.append(ch, start, end - start);
        }
        
        @Override
        public void endElement(String uri, String name, String qName) throws SAXException
        {
            String xmlPath = getXMLPath();
            String content = mContentBuilder.toString();
            onAsyncElementEnd(xmlPath, name, mAttributes, content);
            
            Method method = mEndHandlers.get(xmlPath);
            if (method != null) {
                try {
                    method.invoke(XmlSaxTask.this, mAttributes, content);
                } 
                catch (Exception e) {
                    throw new RuntimeException(e);
                } 
            }
            
            mContentBuilder.setLength(0);
            
            if (mTagStack.size() > 0) {
                mTagStack.remove(mTagStack.size() - 1); 
            }
        }
    }
    
    /**
     * Called when the parses starts to parse a document. Called once.
     */
    protected void onAsyncDocumentStart()
    {
    }
    
    /**
     * Called when the parser has finished parsing a document. Called once if
     * the parsing has not encountered any errors.
     */
    protected void onAsyncDocumentEnd()
    {
    }
    
    /**
     * Called when a new element is encountered.
     * 
     * @param element Full "path" of the element, for example "menu/food/name"
     * @param name Name of the element. Same as the last section of element.
     * @param attributes Attributes for this element.
     */
    protected void onAsyncElementStart(String element, String name, HashMap<String, String> attributes)
    {
    }
    
    /**
     * Called when an element end tag is encountered.
     * 
     * @param element Full "path" of the element, for example "menu/food/name"
     * @param name Name of the element. Same as the last section of element.
     * @param attributes Attributes for this element.
     * @param content Text content that was between the start and end-tags.
     */
    protected void onAsyncElementEnd(String element, String name, HashMap<String, String> attributes, String content)
    {
    }
}
