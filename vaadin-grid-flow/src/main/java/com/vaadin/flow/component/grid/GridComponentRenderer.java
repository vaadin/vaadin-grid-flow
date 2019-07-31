/*
 * Copyright 2000-$today.yearVaadin Ltd.
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
 *
 */
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializableSupplier;

/**
 *
 * @author Vaadin Ltd.
 *
 * @param <SOURCE>
 *            the model type
 */
public class GridComponentRenderer<COMPONENT extends Component, SOURCE>
        extends ComponentRenderer<COMPONENT, SOURCE> {

    private Grid grid;

    public GridComponentRenderer(
            SerializableSupplier<COMPONENT> componentSupplier,
            SerializableBiConsumer<COMPONENT, SOURCE> itemConsumer, Grid grid) {
        super(componentSupplier, itemConsumer);
        this.grid = grid;
    }

    public GridComponentRenderer(
            SerializableSupplier<COMPONENT> componentSupplier, Grid grid) {
        this(componentSupplier, null, grid);
    }

    public GridComponentRenderer(
            SerializableFunction<SOURCE, COMPONENT> componentFunction, Grid grid) {
        this(componentFunction, null, grid);
    }

    public GridComponentRenderer(
            SerializableFunction<SOURCE, COMPONENT> componentFunction,
            SerializableBiFunction<Component, SOURCE, Component> componentUpdateFunction, Grid grid) {
        super(componentFunction, componentUpdateFunction);
        this.grid = grid;
    }

    public COMPONENT createComponent(SOURCE item) {
        COMPONENT component = super.createComponent(item);
        component.onEnabledStateChanged(grid.isEnabled());
        return component;
    }

}
