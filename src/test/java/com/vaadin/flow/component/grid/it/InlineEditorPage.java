/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.Person;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;

@Route("inline-editor")
public class InlineEditorPage extends Div {

    private Grid<Person> grid;

    public InlineEditorPage() {
        grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.NONE);
        grid.setItems(IntStream.range(0, 93)
                .mapToObj(i -> new Person("Person " + i, 1900 + i)));

        Binder<Person> binder = new Binder<>(Person.class);

        // Simple value provider with text field editor
        Column<Person> nameCol = grid.addColumn(Person::getName)
                .setHeader("Name");
        TextField textField = new TextField();
        nameCol.setEditorComponent(textField);
        binder.bind(textField, Person::getName, Person::setName);

        // Column with no editor component
        Column<Person> ageCol = grid.addColumn(Person::getBorn)
                .setHeader("Born");

        // Column with component renderer and a checkbox for editor
        Column<Person> marriedCol = grid
                .addComponentColumn(
                        person -> new Label(person.isMarried() ? "yes" : "no"))
                .setHeader("Married");
        Checkbox checkbox = new Checkbox("married?");
        marriedCol.setEditorComponent(checkbox);
        binder.bind(checkbox, Person::isMarried, Person::setMarried);

        Column<Person> editorColumn = grid.addComponentColumn(person -> {
            Button button = new Button("open editor");
            button.addClickListener(e -> {
                binder.readBean(person);
                grid.editItem(person);
            });
            return button;
        });

        Button save = new Button("save", e -> {
            try {
                binder.writeBean(grid.getEditedItem());
                grid.editItem(null);
            } catch (ValidationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        Button cancel = new Button("cancel", e -> grid.editItem(null));
        Div buttons = new Div(save, cancel);

        // Show save and cancel buttons when editing the row
        editorColumn.setEditorComponent(buttons);

        add(grid);
    }
}
