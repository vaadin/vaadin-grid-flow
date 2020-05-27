package com.vaadin.flow.component.dx;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.demo.GridDemo;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("dx-test1-task1")
public class Task1 extends DemoView {

    @Override
    protected void initView() {
        // Show the provided list of data in the grid
        List<GridDemo.Person> persons = getPersons();

        Grid<GridDemo.Person> grid = new Grid<>();

        /* TODO: Add data binding code here */

        grid.addColumn(GridDemo.Person::getFirstName).setHeader("First Name");
        grid.addColumn(GridDemo.Person::getLastName).setHeader("Last Name");
        grid.addColumn(GridDemo.Person::getAge).setHeader("Age");

        // Add a label that shows the number of rows in the grid
        Label size = new Label("Total size: " /* TODO: Add data size code here */);

        // Make the text field value change event filter the rows in the grid
        TextField filter = new TextField("Filter",
                event -> {
                    /* TODO: Add data filtering code here */
                });

        Checkbox sortByLastName = new Checkbox("Sort By Last Name",
                event -> {
                    /* TODO: Add data sorting code here */
                });

        // Make the number of rows label update when the data in the grid
        // changes due to filtering

        /* TODO: Add code here */

        // Add a button that triggers the exportGrid(List) method with all
        // the rows from the grid with current filtering and sorting applied
        Button export = new Button("Export", event -> {
            Stream<GridDemo.Person> personsStream = Stream.empty();

            /* TODO: Replace Stream.empty() with your code */

            exportGrid(personsStream);
        });

        // Add another button that triggers the exportAll(List) method
        // with all rows without sorting and filtering applied
        Button exportAll = new Button("Export All", event -> {
            Stream<GridDemo.Person> personsStream = Stream.empty();

            /* TODO: Replace Stream.empty() with your code */

            exportAll(personsStream);
        });

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setDefaultHorizontalComponentAlignment(
                FlexComponent.Alignment.START);

        verticalLayout.add(grid, filter, sortByLastName, size);

        addCard("Task1", verticalLayout);
    }

    private List<GridDemo.Person> getPersons() {
        GridDemo.PersonService personService = new GridDemo.PersonService();
        return personService.fetchAll().subList(0, 10);
    }

    private void exportGrid(Stream<GridDemo.Person> persons) {
        Div exportedView = new Div();
        String exported = persons.map(GridDemo.Person::toString)
                .collect(Collectors.joining("; "));
        exportedView.setText(exported);
        add(exportedView);
    }

    private void exportAll(Stream<GridDemo.Person> persons) {
        exportGrid(persons);
    }
}
