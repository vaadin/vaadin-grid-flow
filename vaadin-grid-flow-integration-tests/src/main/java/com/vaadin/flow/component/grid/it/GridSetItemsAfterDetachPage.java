package com.vaadin.flow.component.grid.it;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route
public class GridSetItemsAfterDetachPage extends VerticalLayout {


    public GridSetItemsAfterDetachPage() {
        final List<String> items = Arrays.asList("basdlkf", "laksdfh");
        final Grid<String> grid = new Grid<>();
        final Button back = new Button("back");
        back.setId("back");
        grid.addColumn(e -> e).setHeader("bla");
        grid.addSelectionListener(e -> {
            remove(grid);
            add(back);
        });
        grid.setItems(items);
        add(grid);
        back.addClickListener(e -> {
            this.remove(back);
            grid.setItems(items);
            this.add(grid);
        });
    }

}