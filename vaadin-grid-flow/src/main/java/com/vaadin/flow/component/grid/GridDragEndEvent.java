package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;

@SuppressWarnings("serial")
@DomEvent("grid-dragend")
public class GridDragEndEvent<T> extends ComponentEvent<Grid<T>> {

    public GridDragEndEvent(Grid<T> source, boolean fromClient) {
        super(source, true);
    }

}