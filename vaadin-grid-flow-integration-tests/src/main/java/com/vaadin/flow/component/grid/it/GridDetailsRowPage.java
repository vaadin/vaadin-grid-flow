package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("grid-details-row")
public class GridDetailsRowPage extends Div {

    private Grid<Person> grid = new Grid<>();
    private List<Person> items = new ArrayList<>();

    public void setFirstAndSecondItemsVisible() {
        grid.setDetailsVisible(items.get(0), true);
        grid.setDetailsVisible(items.get(1), true);
    }
    public GridDetailsRowPage() {
        items.add(new Person("Person 1", 99));
        items.add(new Person("Person 2", 1));
        items.add(new Person("Person 3", 2));
        items.add(new Person("Person 4", 1111));

        ListDataProvider<Person> ldp = new ListDataProvider<>(items);
        grid.setDataProvider(ldp);

        Grid.Column<Person> nameColumn = grid.addColumn(bean -> bean.getFirstName()).setHeader("name");
        grid.setItemDetailsRenderer(new ComponentRenderer<>(item -> new Button(item.getFirstName())));
        grid.setDetailsVisibleOnClick(false);
        add(grid,
                new Button("click to open details", e -> {
                    setFirstAndSecondItemsVisible(); // same method call as above
                })
        );
        grid.getElement().getNode()
                .runWhenAttached(ui -> ui.getInternals().getStateTree()
                        .beforeClientResponse(grid.getElement().getNode(), e -> setFirstAndSecondItemsVisible()));
    }

}
