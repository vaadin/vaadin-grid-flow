/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. 
 */
package com.vaadin.data.provider;

import java.io.Serializable;

import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.internal.JsonCodec;

public interface TreeUpdate extends Update {

    /**
     * Commits changes given with {@link #enqueue(String, Serializable...)}
     */
    void commit();

    /**
     * Enqueue function call with the given arguments.
     * 
     * @see JsonCodec JsonCodec for supported argument types
     * @param name
     *            the name of the function to call, may contain dots to indicate
     *            a function on a property.
     * @param arguments
     *            the arguments to pass to the function. Must be of a type
     *            supported by the communication mechanism, as defined by
     *            {@link JsonCodec}
     */
    void enqueue(String name, Serializable... arguments);
}
