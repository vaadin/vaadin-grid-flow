package com.vaadin.flow.component.dx;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.demo.GridDemo;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.router.Route;

import java.util.Collections;
import java.util.List;

@Route("dx-test2-task1")
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

        // Add a listeners to text fields below and make them filter
        // the rows in the grid
        TextField filterByFirstName = new TextField("Filter By First Name",
                event -> {
                    /* TODO: Add data filtering code here */
                });
        TextField filterByLastName = new TextField("Filter By Last Name",
                event -> {
                    /* TODO: Add data filtering code here */
                });

        Button removeFilters = new Button("Remove filters", event -> {
            /* TODO: Add remove filters code here */
        });

        CheckboxGroup<PersonSorting> personSortingComboBox =
                createPersonSortingCheckboxGroup();

        personSortingComboBox.addSelectionListener(event -> {
            /* TODO: Add data sorting code here */
        });

        Button sortById = new Button("Sort By Id", event -> {
            /* TODO: Add data sorting code here */
        });

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setDefaultHorizontalComponentAlignment(
                FlexComponent.Alignment.START);

        verticalLayout.add(grid, filterByFirstName, filterByLastName,
                removeFilters, personSortingComboBox, sortById);

        addCard("Task1", verticalLayout);
    }

    private CheckboxGroup<PersonSorting> createPersonSortingCheckboxGroup() {
        CheckboxGroup<PersonSorting> personsSorting =
                new CheckboxGroup<>();

        personsSorting.setLabel("Person sorting");
        personsSorting.setItemLabelGenerator(PersonSorting::getLabel);
        personsSorting.setItems(
                new PersonSorting((p1, p2) ->
                        p1.getFirstName().compareTo(p2.getFirstName()),
                        "Sort By First Name"),
                new PersonSorting((p1, p2) ->
                        p1.getLastName().compareTo(p2.getLastName()),
                        "Sort By Last Name"));

        return personsSorting;
    }

    private List<GridDemo.Person> getPersons() {
        GridDemo.PersonService personService = new GridDemo.PersonService();
        List<GridDemo.Person> persons = personService.fetchAll().subList(0, 50);
        Collections.shuffle(persons);
        return persons;
    }

    private static class PersonSorting {
        private SerializableComparator<GridDemo.Person> comparator;
        private String label;

        public PersonSorting(SerializableComparator<GridDemo.Person> comparator, String label) {
            this.comparator = comparator;
            this.label = label;
        }

        public SerializableComparator<GridDemo.Person> getComparator() {
            return comparator;
        }

        public String getLabel() {
            return label;
        }
    }
}
