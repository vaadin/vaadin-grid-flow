/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.grid.demo;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;

@Route("tasks")
public class Task1 extends VerticalLayout {

    private final ItemGenerator backend = new ItemGenerator();
    private final Grid<GridDemo.Item> grid;

    public Task1() {
        grid = new Grid<>();
        grid.setSizeFull();
        configureColumns();

        // TODO do tasks here


        add(grid);
        setFlexGrow(1, grid);
        setSizeFull();
    }

    private void configureColumns() {
        grid.addColumn(GridDemo.Item::getName).setHeader("Name")
                .setWidth("20px");

        // NumberRenderer to render numbers in general
        grid.addColumn(new NumberRenderer<>(GridDemo.Item::getPrice, "$ %(,.2f",
                Locale.US, "$ 0.00")).setHeader("Price");

        // LocalDateTimeRenderer for date and time
        grid.addColumn(
                new LocalDateTimeRenderer<>(GridDemo.Item::getPurchaseDate,
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT,
                                FormatStyle.MEDIUM)))
                .setHeader("Purchase Date and Time").setFlexGrow(2);

        // LocalDateRenderer for dates
        grid.addColumn(
                new LocalDateRenderer<>(GridDemo.Item::getEstimatedDeliveryDate,
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                .setHeader("Estimated Delivery Date");
    }
}
