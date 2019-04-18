/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.UI;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Drag start event on an HTML5 drag source {@link Grid} row.
 *
 * @param <T>
 *            The Grid bean type.
 * @author Vaadin Ltd.
 * @see Grid#addDragStartListener(GridDragStartListener)
 */
@SuppressWarnings("serial")
@DomEvent("grid-dragstart")
public class GridDragStartEvent<T> extends ComponentEvent<Grid<T>> {

    private final List<T> draggedItems;

    public GridDragStartEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail") JsonObject details) {
        super(source, true);
        JsonArray items = details.getArray("items");

        ComponentUtil.setData(UI.getCurrent(), "drag-source", source);

        List<T> draggedItems = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            String itemKey = items.getObject(i).getString("key");
            T item = source.getDataCommunicator().getKeyMapper().get(itemKey);
            draggedItems.add(item);
        }
        this.draggedItems = draggedItems;
    }

    /**
     * Get the dragged row items.
     *
     * @return an unmodifiable list of items that are being dragged.
     */
    public List<T> getDraggedItems() {
        return Collections.unmodifiableList(draggedItems);
    }

}