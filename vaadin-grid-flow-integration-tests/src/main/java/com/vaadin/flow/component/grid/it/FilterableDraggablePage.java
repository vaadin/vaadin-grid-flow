package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;

@Route("filterable-draggable")
public class FilterableDraggablePage extends VerticalLayout {
    Person draggedItem;


    public FilterableDraggablePage() {
        List<Person> personList = new ArrayList<>(Arrays.asList(Person.createTestPerson1(), Person.createTestPerson2()));
        Grid<Person> grid = new Grid<>();
        grid.setRowsDraggable(true);
        ListDataProvider<Person> dataProvider = new ListDataProvider<>(
              personList);
        grid.setDataProvider(dataProvider);

        Grid.Column<Person> firstNameColumn = grid
              .addColumn(Person::getFirstName).setHeader("Name");
        Grid.Column<Person> ageColumn = grid.addColumn(Person::getAge)
              .setHeader("Age");
        Grid.Column<Person> cityColumn = grid
              .addColumn(person -> person.getAddress().getCity())
              .setHeader("City");

        HeaderRow filterRow = grid.appendHeaderRow();
        // First filter
        TextField firstNameField = new TextField();
        firstNameField.addValueChangeListener(event -> dataProvider.addFilter(
              person -> StringUtils.containsIgnoreCase(person.getFirstName(),
                    firstNameField.getValue())));

        firstNameField.setValueChangeMode(ValueChangeMode.EAGER);

        filterRow.getCell(firstNameColumn).setComponent(firstNameField);
        firstNameField.setSizeFull();
        firstNameField.setPlaceholder("Filter");

        // Second filter
        TextField ageField = new TextField();
        ageField.addValueChangeListener(event -> dataProvider.addFilter(person -> StringUtils
              .containsIgnoreCase(String.valueOf(person.getAge()), ageField.getValue())));

        ageField.setValueChangeMode(ValueChangeMode.EAGER);

        filterRow.getCell(ageColumn).setComponent(ageField);
        ageField.setSizeFull();
        ageField.setPlaceholder("Filter");

        // Third filter
        TextField cityField = new TextField();
        cityField.addValueChangeListener(event -> dataProvider.addFilter(person -> StringUtils
              .containsIgnoreCase(person.getAddress().getCity(), cityField.getValue())));

        cityField.setValueChangeMode(ValueChangeMode.EAGER);

        filterRow.getCell(cityColumn).setComponent(cityField);
        cityField.setSizeFull();
        cityField.setPlaceholder("Filter");

        grid.addDragStartListener(event -> {
            draggedItem = event.getDraggedItems().get(0);
            grid.setDropMode(GridDropMode.BETWEEN);
        });

        grid.addDragEndListener(event -> {
            draggedItem = null;
            grid.setDropMode(null);
        });

        grid.addDropListener(event -> {
            Person dropOverItem = event.getDropTargetItem().orElse(null);
            if (dropOverItem == null) {
                return;
            }
            if (!dropOverItem.equals(draggedItem)) {
                personList.remove(draggedItem);
                int dropIndex = personList.indexOf(dropOverItem) + (
                      event.getDropLocation() == GridDropLocation.BELOW ? 1 : 0);
                personList.add(dropIndex, draggedItem);
                grid.getDataProvider().refreshAll();
            }
        });
        add(grid);
    }
}
