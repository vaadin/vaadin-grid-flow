package com.vaadin.flow.component.grid;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.UI;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

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

    public List<T> getDraggedItems() {
        return draggedItems;
    }

}