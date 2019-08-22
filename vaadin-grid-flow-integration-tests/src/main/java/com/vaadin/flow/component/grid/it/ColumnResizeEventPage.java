/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.bean.Gender;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("column-resize-event")
public class ColumnResizeEventPage extends Div {

    public static final String GRID_ID = "column-resize-event-grid";
    public static final String RESIZE_EVENT_LABEL_ID = "column-resize-event-label";
    public static final String RESIZED_COLUMN_ID = "Resized Column ID";
    
    public ColumnResizeEventPage() {
        Grid<Person> grid = new Grid<>();
        grid.setId(GRID_ID);
        grid.getStyle().set("--lumo-font-family",
                "Arial, Helvetica, sans-serif");
        grid.setItems(new Person("Jorma", "Testaaja", "jorma@testaaja.com",
                2018, Gender.MALE, null));

        grid.addColumn(Person::getFirstName).setHeader("A").setResizable(true)
                .setId("A");
        grid.addColumn(Person::getLastName).setHeader("B").setResizable(true)
                .setId(RESIZED_COLUMN_ID);
        grid.addColumn(Person::getId).setHeader("C").setResizable(true)
                .setId("C");

        grid.addColumnResizeListener(e -> {
            Label label = new Label(e.getResizedColumn().getId().get());
            label.setId(RESIZE_EVENT_LABEL_ID);
            add(label);
        });
        add(grid);
    }

}
