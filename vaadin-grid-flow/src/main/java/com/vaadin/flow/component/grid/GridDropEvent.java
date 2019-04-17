package com.vaadin.flow.component.grid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.UI;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

@SuppressWarnings("serial")
@DomEvent("grid-drop")
public class GridDropEvent<T> extends DropEvent<Grid<T>> {

    // TODO: Use Optional
    private final T dropTargetRow;
    private final DropLocation dropLocation;

    public GridDropEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail.item") JsonObject item,
            @EventData("event.detail.dropLocation") String dropLocation,
            @EventData("event.detail.dragData") JsonArray dragData) {
        super(source, new HashMap<String, String>(), (Component) ComponentUtil
                .getData(UI.getCurrent(), "drag-source"));

        IntStream.range(0, dragData.length()).forEach(i -> {
            JsonObject data = dragData.getObject(i);
            getDataTransferData().put(data.getString("type"),
                    data.getString("data"));
        });

        if (item != null) {
            this.dropTargetRow = source.getDataCommunicator().getKeyMapper()
                    .get(item.getString("key"));
        } else {
            this.dropTargetRow = null;
        }

        this.dropLocation = Arrays.asList(DropLocation.values()).stream()
                .filter(dl -> dl.getClientName().equals(dropLocation))
                .findFirst().get();
    }

    public Optional<T> getDropTargetRow() {
        return Optional.ofNullable(dropTargetRow);
    }

    public DropLocation getDropLocation() {
        return dropLocation;
    }

}