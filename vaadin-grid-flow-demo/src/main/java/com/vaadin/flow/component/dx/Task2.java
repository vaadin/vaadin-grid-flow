package com.vaadin.flow.component.dx;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.demo.GridDemo;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("dx-test2-task2")
public class Task2 extends DemoView {

    @Override
    protected void initView() {
        // Show the provided list of data in the grid
        List<GridDemo.Person> persons = getPersons();

        Grid<GridDemo.Person> grid = new Grid<>();

        /* TODO: Add data binding code here */

        grid.addColumn(GridDemo.Person::getFirstName).setHeader("First Name");
        grid.addColumn(GridDemo.Person::getLastName).setHeader("Last Name");
        grid.addColumn(GridDemo.Person::getAge).setHeader("Age");

        grid.addItemClickListener(
                event -> openDialog(event.getItem(), grid));

        addCard("Task3", grid);
    }

    private void openDialog(GridDemo.Person person,
                            Grid<GridDemo.Person> grid) {
        new PersonDialog(grid, person).open();
    }

    private List<GridDemo.Person> getPersons() {
        GridDemo.PersonService personService = new GridDemo.PersonService();
        return personService.fetchAll().subList(0, 10);
    }

    private static class PersonDialog extends Dialog {
        private Button next;
        private Button previous;
        private GridDemo.Person currentPerson;
        private final Grid<GridDemo.Person> grid;
        private Span data = new Span();

        private TextField firstName;
        private TextField lastName;
        private IntegerField age;
        private Binder<GridDemo.Person> binder;

        public PersonDialog(Grid<GridDemo.Person> grid,
                            GridDemo.Person person) {
            this.grid = grid;
            GridListDataView<GridDemo.Person> dataView = grid.getListDataView();

            // Add buttons to the provided PersonDialog that switch and select
            // the next and previous items from the grid.
            // Disable the buttons respectively when the first or last row is
            // selected

            next = new Button("Next", event -> {
                /* TODO: Add next button code here */
            });

            previous = new Button("Previous", event -> {
                /* TODO: Add previous button code here */
            });

            initPersonForm();
            setItem(person);

            // Add a button to the PersonDialog that deletes the row from grid and closes
            // the dialog

            Button delete = new Button("Remove", event -> {
                /* TODO: Add delete button code here */
            });

            // Make the saveButton in the dialog add the current Person to the grid in
            // case it is not there yet, otherwise just update the existing item

            Button save = new Button("Save", event -> {
                /* TODO: Add save button code here */
            });

            setModal(true);
            HorizontalLayout layout = new HorizontalLayout(previous, data, next);
            layout.expand(data);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setWidth("400px");
            FormLayout formLayout = new FormLayout(firstName, lastName, age);
            HorizontalLayout actions = new HorizontalLayout();
            actions.add(delete, save);

            add(new VerticalLayout(new Span("Click outside to close"), layout,
                    formLayout, actions));
        }

        private void initPersonForm() {
            firstName = new TextField("First Name");
            lastName = new TextField("Last Name");
            age = new IntegerField("Age");
            binder = new Binder<>();
            binder.forField(firstName).bind(GridDemo.Person::getFirstName,
                    GridDemo.Person::setFirstName);
            binder.forField(lastName).bind(GridDemo.Person::getLastName,
                    GridDemo.Person::setLastName);
            binder.forField(age).bind(GridDemo.Person::getAge,
                    GridDemo.Person::setAge);
        }

        private void setItem(GridDemo.Person person) {
            currentPerson = person;
            binder.readBean(currentPerson);
            data.setText(person.toString());
            grid.select(person);
        }
    }
}
