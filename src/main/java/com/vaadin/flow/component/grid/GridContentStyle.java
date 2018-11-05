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
package com.vaadin.flow.component.grid;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.dom.StyleUtil;
import com.vaadin.flow.function.SerializableBiConsumer;

/**
 * Based on {@link com.vaadin.flow.dom.impl.BasicElementStyle}. The purpose of
 * this class is to provide similar API and behavior for generating Grid
 * cell/row/column styles as we have for setting styles on components.
 * 
 * @author Vaadin Ltd
 */
class GridContentStyle implements Style {

    private Map<String, String> stylePropertyMap = new HashMap<>();

    @Override
    public String get(String name) {
        ElementUtil.validateStylePropertyName(name);

        return (String) stylePropertyMap
                .get(StyleUtil.stylePropertyToAttribute(name));
    }

    @Override
    public Style set(String name, String value) {
        ElementUtil.validateStylePropertyName(name);
        if (value == null) {
            return this.remove(name);
        }
        String trimmedValue = value.trim();
        ElementUtil.validateStylePropertyValue(trimmedValue);

        stylePropertyMap.put(StyleUtil.stylePropertyToAttribute(name),
                trimmedValue);
        return this;
    }

    @Override
    public Style remove(String name) {
        ElementUtil.validateStylePropertyName(name);

        stylePropertyMap.remove(StyleUtil.stylePropertyToAttribute(name));
        return this;
    }

    @Override
    public Style clear() {
        stylePropertyMap.clear();
        return this;
    }

    @Override
    public boolean has(String name) {
        return stylePropertyMap
                .containsKey(StyleUtil.stylePropertyToAttribute(name));
    }

    @Override
    public Stream<String> getNames() {
        return stylePropertyMap.keySet().stream();
    }

    void forEach(SerializableBiConsumer<String, String> action) {
        stylePropertyMap.forEach(action);
    }
}
