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

import fi.vincit.androidutilslib.stream.ProgressOutputStream;
import fi.vincit.androidutilslib.stream.ProgressStreamListener;
import fi.vincit.httpclientandroidlib.HttpEntity;
import fi.vincit.httpclientandroidlib.entity.HttpEntityWrapper;

public class ProgressHttpEntityWrapper extends HttpEntityWrapper {

    private ProgressStreamListener mListener;
    
    public ProgressHttpEntityWrapper(HttpEntity wrapped) {
        super(wrapped);
    }
    
    public void setProgressStreamListener(ProgressStreamListener listener) {
        mListener = listener;
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        if (!(out instanceof ProgressOutputStream) && mListener != null) {
            ProgressOutputStream stream = new ProgressOutputStream(out);
            stream.setProgressStreamListener(mListener);
            
            out = stream;
        }
        wrappedEntity.writeTo(out);
    }
}
