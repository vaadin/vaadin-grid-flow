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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.util.Optional;

/**
 * Event fired when a Grid item is clicked.
 *
 * @param <T>
 *            the grid bean type
 *
 * @author Vaadin Ltd
 *
 * @see Grid#addItemClickListener(com.vaadin.flow.component.ComponentEventListener)
 *
 */
@DomEvent("item-click")
public class ItemClickEvent<T> extends ClickEvent<Grid<T>> {

    private final T item;

    private final Grid.Column<T> column;

    /**
     * Creates a new item click event.
     *
     * @param source
     *            the component that fired the event
     * @param fromClient
     *            <code>true</code> if the event was originally fired on the
     *            client, <code>false</code> if the event originates from
     *            server-side logic
     * @param itemKey the item mapper key
     * @param internalColumnId
     *            the internal id of the column associated with
     *            the click event (if present)
     * @param screenX
     *            the x coordinate of the click event, relative to the upper
     *            left corner of the screen, -1 if unknown
     * @param screenY
     *            the y coordinate of the click event, relative to the upper
     *            left corner of the screen, -i if unknown
     * @param clientX
     *            the x coordinate of the click event, relative to the upper
     *            left corner of the browser viewport, -1 if unknown
     * @param clientY
     *            the y coordinate of the click event, relative to the upper
     *            left corner of the browser viewport, -1 if unknown
     * @param clickCount
     *            the number of consecutive clicks recently recorded
     * @param button
     *            the id of the pressed mouse button
     * @param ctrlKey
     *            <code>true</code> if the control key was down when the event
     *            was fired, <code>false</code> otherwise
     * @param shiftKey
     *            <code>true</code> if the shift key was down when the event was
     *            fired, <code>false</code> otherwise
     * @param altKey
     *            <code>true</code> if the alt key was down when the event was
     *            fired, <code>false</code> otherwise
     * @param metaKey
     *            <code>true</code> if the meta key was down when the event was
     *            fired, <code>false</code> otherwise
     *
     */
    public ItemClickEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail.itemKey") String itemKey,
            @EventData("event.detail.internalColumnId") String internalColumnId,
            @EventData("event.detail.screenX") int screenX,
            @EventData("event.detail.screenY") int screenY,
            @EventData("event.detail.clientX") int clientX,
            @EventData("event.detail.clientY") int clientY,
            @EventData("event.detail.detail") int clickCount,
            @EventData("event.detail.button") int button,
            @EventData("event.detail.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.shiftKey") boolean shiftKey,
            @EventData("event.detail.altKey") boolean altKey,
            @EventData("event.detail.metaKey") boolean metaKey) {
        super(source, fromClient, screenX, screenY, clientX, clientY,
                clickCount, button, ctrlKey, shiftKey, altKey, metaKey);
        item = source.getDataCommunicator().getKeyMapper().get(itemKey);
        column = source.getColumnByInternalId(internalColumnId);
    }

    /**
     * Gets the clicked item.
     *
     * @return the clicked item
     */
    public T getItem() {
        return item;
    }

    /**
     * Gets the column that was clicked, if available.
     * @return An optional with column. Optional may be empty if the column could not be figured out or
     * when the details cell has been clicked (that cell spans through the entire grid and thus there
     * is no column information).
     */
    public Optional<Grid.Column<T>> getColumn() {
        return Optional.ofNullable(column);
    }

}
