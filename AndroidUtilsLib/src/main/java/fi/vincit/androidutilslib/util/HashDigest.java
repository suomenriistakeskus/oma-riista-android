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
package fi.vincit.androidutilslib.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Locale;

/**
 * Class for generating cryptographic hashes.
 */
public class HashDigest 
{
    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";
    public static final String SHA512 = "SHA-512";
    
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    private MessageDigest mDigest;

    /**
     * Creates a new HashDigest that uses the given hash algorithm.
     */
    public HashDigest(String algorithm)
    {
        try {
            mDigest = MessageDigest.getInstance(algorithm);
        } 
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Creates a new HashDigest that uses an existing MessageDigest.
     */
    public HashDigest(MessageDigest digest)
    {
        mDigest = digest;
    }
    
    public MessageDigest getDigest()
    {
        return mDigest;
    }
    
    /**
     * Resets the internal MessageDigest object.
     */
    public void reset() 
    {
        mDigest.reset();
    }
    
    /**
     * Updates the digest using the string argument. The string data
     * is decoded using DEFAULT_ENCODING.
     */
    public void update(String text)
    {
        try {
            mDigest.update(text.getBytes(DEFAULT_ENCODING));
        } 
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Updates the digest using the stream argument.
     */
    public void update(InputStream stream) throws IOException
    {
        byte[] buffer = new byte[1024 * 8];
        int count;
        while ((count = stream.read(buffer)) != -1) {
            mDigest.update(buffer, 0, count);
        }
    }
    
    /**
     * Digests all data and returns a base64 encoded string value. Do not use
     * the object after calling this.
     */
    public String base64Digest()
    {
        byte[] result = mDigest.digest();
        return Base64Converter.encodeBytes(result);
    }
    
    /**
     * Digests all data and returns a hex encoded string value. Do not use
     * the object after calling this.
     */
    public String hexDigest()
    {
        byte[] result = mDigest.digest();

        Formatter formatter = new Formatter(Locale.ENGLISH);
        for (byte b : result) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
    
}
