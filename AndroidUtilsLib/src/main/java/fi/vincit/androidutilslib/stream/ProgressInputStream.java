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
package fi.vincit.androidutilslib.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends FilterInputStream  {

    private ProgressStreamListener mListener;
    private long mBytesRead = 0;
    private int mRecursion = 0;

    protected ProgressInputStream(InputStream in) {
        super(in);
    }
    
    public void setProgressStreamListener(ProgressStreamListener listener) {
        mListener = listener;
    }
    
    public long getBytesRead() {
        return mBytesRead;
    }
    
    private void updateProgress(long bytesRead) throws IOException {
        if (mRecursion != 0) {
            return;
        }
        
        if (bytesRead > 0) {
            mBytesRead += bytesRead;
            
            if (mListener != null) {
                mListener.onProgress(mBytesRead);
            }
        }
    }
    
    @Override
    public int read() throws IOException {
        mRecursion++;
        int value = super.read();
        mRecursion--;
        if (value != -1) {
            updateProgress(1);
        }
        return value;
    }

    @Override
    public int read(byte[] b) throws IOException {
        mRecursion++;
        int count = super.read(b);
        mRecursion--;
        updateProgress(count);
        return count;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        mRecursion++;
        int count = super.read(b, off, len);
        mRecursion--;
        updateProgress(count);
        return count;
    }

    @Override
    public long skip(long n) throws IOException {
        mRecursion++;
        long count = super.skip(n);
        mRecursion--;
        updateProgress(count);
        return count;
    }

    @Override
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
