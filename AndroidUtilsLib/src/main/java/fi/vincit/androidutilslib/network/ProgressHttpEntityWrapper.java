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
package fi.vincit.androidutilslib.network;

import java.io.IOException;
import java.io.OutputStream;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.HttpEntityWrapper;
import fi.vincit.androidutilslib.stream.ProgressOutputStream;
import fi.vincit.androidutilslib.stream.ProgressStreamListener;

public class ProgressHttpEntityWrapper extends HttpEntityWrapper {

    private ProgressStreamListener mListener;
    
    public ProgressHttpEntityWrapper(final HttpEntity wrapped) {
        super(wrapped);
    }
    
    public void setProgressStreamListener(final ProgressStreamListener listener) {
        mListener = listener;
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        if (!(out instanceof ProgressOutputStream) && mListener != null) {
            final ProgressOutputStream stream = new ProgressOutputStream(out);
            stream.setProgressStreamListener(mListener);
            
            out = stream;
        }

        super.writeTo(out);
    }
}
