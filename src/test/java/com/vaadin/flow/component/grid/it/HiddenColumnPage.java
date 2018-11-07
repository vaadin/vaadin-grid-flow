/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import java.util.Arrays;
import java.util.Collections;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.demo.GridView.Person;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("hidden-column")
public class HiddenColumnPage extends Div {

    public HiddenColumnPage() {
        Grid<Person> grid = new Grid<>();

        Person person = createPerson("foo", "bar@gmail.com");
        grid.setItems(Collections.singletonList(person));

        Column<Person> nameColumn = grid.addColumn(Person::getName)
                .setHeader("Name");
        grid.addColumn(Person::getEmail).setHeader("E-mail");

        NativeButton hideUnhide = new NativeButton("Hide/unhide Name column",
                event -> nameColumn.setVisible(!nameColumn.isVisible()));
        hideUnhide.setId("hide-inhide");

        NativeButton addItem = new NativeButton("Add an item", event -> {
            grid.setItems(Arrays.asList(person,
                    createPerson("bar", "baz@example.com")));
        });

        add(grid, hideUnhide, addItem);
    }

    private Person createPerson(String name, String email) {
        Person person = new Person();
        person.setName(name);
        person.setEmail(email);
        return person;
    }
}
