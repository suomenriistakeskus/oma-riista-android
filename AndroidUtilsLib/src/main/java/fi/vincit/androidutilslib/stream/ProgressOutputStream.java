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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProgressOutputStream extends FilterOutputStream {
    
    private ProgressStreamListener mListener;
    private long mBytesWritten = 0;
    private int mRecursion = 0;
    
    public ProgressOutputStream(OutputStream out) {
        super(out);
    }
    
    public void setProgressStreamListener(ProgressStreamListener listener) {
        mListener = listener;
    }
    
    public long getBytesWritten() {
        return mBytesWritten;
    }
    
    private void updateProgress(long bytesWritten) throws IOException {
        if (mRecursion != 0) {
            return;
        }
        
        if (bytesWritten > 0) {
            mBytesWritten += bytesWritten;
            
            if (mListener != null) {
                mListener.onProgress(mBytesWritten);
            }
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        mRecursion++;
        super.write(oneByte);
        mRecursion--;
        updateProgress(1);
    }
    
    @Override
    public void write(byte[] buffer) throws IOException {
        mRecursion++;
        super.write(buffer);
        mRecursion--;
        updateProgress(buffer.length);
    }
    
    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        mRecursion++;
        super.write(buffer, offset, length);
        mRecursion--;
        updateProgress(length);
    }
}

