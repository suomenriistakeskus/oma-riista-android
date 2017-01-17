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

import fi.vincit.androidutilslib.adapter.AttributeAdapter;

/**
 * Helper class that implements the AttributeAdapter item listener with empty methods.
 */
public class BaseAttributeAdapterItemListener<T> implements AttributeAdapter.ItemListener<T> {
    @Override
    public void onViewClicked(int position, T item) {

    }

    @Override
    public void onViewChecked(int position, T item, boolean checked) {

    }
}
