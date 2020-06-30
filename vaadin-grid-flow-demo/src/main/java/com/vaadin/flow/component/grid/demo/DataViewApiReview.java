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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("api-review")
public class DataViewApiReview extends DemoView {

    @Override
    protected void initView() {
        noSize();
        noSize_customizingScrollingExperience();
    }

    /**
     * Use Case / Problem 1: https://github.com/vaadin/flow/issues/8052
     * 
     * Unknown size for data provider / optional size query:
     * 
     * When binding list of data into a component, I don't want to provide a
     * size for the data as it costs too much and I would expect the component
     * to just keep fetching more data when user scrolls until there is no more
     * data available.
     * 
     * This was not possible before. Only done for grid, to be done for ComboBox
     * later on. (v18?)
     * 
     * Design goals:
     * 
     * 1) everything is backwards compatible, no breaking changes. Deprecate and
     * remove later when possible instead of breaking.
     * 
     * 2) users should not need to know what is a "data provider" but be able to
     * do things from component API
     * 
     * 3) we should avoid API bloat that doesn't apply and would have to
     * throw and exception in certain cases
     * 
     * 4) DataProviders should remain stateless "providers of data", possible to
     * use in multiple components.
     *
     * 5) A user should not need to learn what is a data provider to be able to
     * add data lazily to grid (or any component)
     * 
     * 6) (partly related) simple operations for in-memory data should be
     * simple. This is related because having the same API for lazy case has
     * prevented adding some convenient API for in-memory cases.
     * 
     * Design based on:
     *
     * 1) Make size query optional and data provider's size() method is not just
     * used by the component.
     * 
     * 2) Do not break existing data providers or application code by:
     * 
     * 2.1) Existing data providers in use are able to switch to automatically
     * extending grid easily.
     *
     * 2.2) Deprecate old setDataProvider methods to allow migrating when
     * suitable and backporting without breaking anything.
     *
     * 2.3) Introduce new methods with different return type for setting the
     * data, returning DataView object, which..
     *
     * 3) ..allows further changes & control through data view API that is
     * returned by data setter method.
     *
     * 3.1) Data View is an API surface that provides a view to the data the
     * component has based on the type of data. Initially support in-memory and
     * lazy with common API available in supertype.
     *
     * 3.2) Plan is to extend further with filtering & hierarchical concepts or
     * supporting e.g. cursor/item based queries
     *
     *
     * Automatically scrolling grid defaults that can be changed based on
     * feedback even during platform beta:
     *
     * Default initial size = 4 * pageSize = 200.
     *
     * Default count increase = 4 * pageSize = 200.
     */
    private void noSize() {
        // begin-source-example
        // source-example-heading: Basic usage
        Grid<GridDemo.Person> grid = new Grid<>();
        GridDemo.PersonService personService = new GridDemo.PersonService();

        grid.setDataSource(query -> personService
                .fetch(query.getOffset(), query.getLimit()).stream());

        // the real callback type used is
        final CallbackDataProvider.FetchCallback<GridDemo.Person, Void> personVoidFetchCallback = query -> personService
                .fetch(query.getOffset(), query.getLimit()).stream();
        // Void implies here the filter type - no API for setting filter for
        // now, should be <?> instead ?

        grid.addColumn(GridDemo.Person::getFirstName).setHeader("First Name");
        grid.addColumn(GridDemo.Person::getLastName).setHeader("Last Name");
        grid.addColumn(GridDemo.Person::getAge).setHeader("Age");
        // end-source-example
        addCard("No Size", "Basic usage", grid);
    }

    /**
     * For a very large data set, it should be possible to let the user scroll
     * down faster without providing the exact size.
     */
    private void noSize_customizingScrollingExperience() {
        final ItemGenerator fakeBackend = new ItemGenerator();
        // begin-source-example
        final Grid<GridDemo.Item> grid = new Grid<>();
        setupItemGridColumns(grid);

        final GridLazyDataView<GridDemo.Item> lazyDataView = grid
                .setDataSource(query -> fakeBackend
                        .fetchItems(query.getOffset(), query.getLimit()));

        lazyDataView.setRowCountEstimate(10000);
        lazyDataView.setRowCountEstimateIncrease(1000);

        // end-source-example
        addCard("No Size", "Customizing scrolling experience", grid);
    }

    /**
     * How it compares to old code or usage.
     */
    private void noSize_backWardsCompatibility_usingDataProvider() {
        Grid<GridDemo.Person> grid = new Grid<>();
        GridDemo.PersonService personService = new GridDemo.PersonService();

        // 1: old way still works, but it is deprecated
        final CallbackDataProvider<GridDemo.Person, Void> dataProvider = DataProvider
                .fromCallbacks(query -> personService
                        .fetch(query.getOffset(), query.getLimit()).stream(),
                        query -> personService.count());

        grid.setDataProvider(dataProvider);

        // 2: replacement uses the same naming as everything else in all
        // components regardless of in-memory or lazy
        grid.setDataSource(dataProvider);

        // 3: the shorthand that allows users to not have to know "what is a
        // data provider"
        grid.setDataSource(
                query -> personService
                        .fetch(query.getOffset(), query.getLimit()).stream(),
                query -> personService.count());
    }

    private void noSize_letUserSwitchBetweenExactAndExtendingGrid() {
        final Grid<GridDemo.Item> grid = new Grid<>();
        setupItemGridColumns(grid);
        final ItemGenerator fakeBackend = new ItemGenerator();

        grid.setDataSource(query -> fakeBackend.fetchItems(query.getOffset(),
                query.getLimit()));
        // toggle between exact size and automatically extending grid
        final Checkbox checkbox = new Checkbox("Exact Size", event -> {
            final GridLazyDataView<GridDemo.Item> lazyDataView = grid
                    .getLazyDataView();
            if (event.getValue()) {
                lazyDataView
                        .setRowCountCallback(query -> fakeBackend.getCount());
            } else {
                lazyDataView.setRowCountUnknown();
            }
        });
    }

    private void noSize_filtering() {
        // begin-source-example
        // source-example-heading: Basic usage
        final ItemGenerator fakeBackend = new ItemGenerator();
        final Grid<GridDemo.Item> grid = new Grid<>();

        final TextField filterField = new TextField("Filter by name",
                event -> grid.getDataProvider().refreshAll());
        filterField.setValueChangeTimeout(1000);

        // This is not perfect and could be improved but to be left for later
        // https://github.com/vaadin/flow/issues/8646
        grid.setDataSource(query -> fakeBackend.fetchItems(query.getOffset(),
                query.getLimit(), filterField.getValue()));

        // end-source-example
        addCard("No Size", "Filtering Lazy", new VerticalLayout(filterField, grid));
    }

    private void noSize_updates() {
        Grid<GridDemo.Person> grid = new Grid<>();
        final GridDemo.PersonService service = new GridDemo.PersonService();

        final GridLazyDataView<GridDemo.Person> dataView = grid
                .setDataSource(query -> service
                        .fetch(query.getOffset(), query.getLimit()).stream());

        /*
         * Being able to customize how the equality of items is determined. When
         * data providers are not needed, it is important that this is possible.
         * Before needed to create a custom data provider and override
         * DataProvider::getId
         */
        dataView.setIdentifierProvider(GridDemo.Person::getId);

        /*
         * Adding, removing and updating items still has to go through data
         * provider. This can be later on changed with more specific API for lazy
         * case. The updateItem(item) method is missing from LazyDataView but
         * there is no reason for this as it simply delegates to data provider.
         */
        new Button("Add new person in lazy case", event -> {
            // backend magic adds the item somewhere ...

            grid.getDataProvider().refreshAll();
        });
        new Button("Update marital status for first item", event -> {
            grid.getDataProvider().refreshItem(dataView.getItemOnRow(0));
        });
    }

    /**
     * The remaining LazyDataView API, partly inherited from DataView.
     */
    private void noSize_lazyDataView_additionalAPI() {
        final Grid<GridDemo.Item> grid = new Grid<>();
        final ItemGenerator fakeBackend = new ItemGenerator();

        final GridLazyDataView<GridDemo.Item> lazyDataView = grid
                .setDataSource(query -> fakeBackend
                        .fetchItems(query.getOffset(), query.getLimit()));
        final Grid.Column<GridDemo.Item> nameColumn = grid
                .addColumn(GridDemo.Item::getName).setHeader("Name");

        // API in grid is exposed by HasLazyDataView
        grid.getLazyDataView(); // throws when data provider not a
                                // BackendDataProvider

        // shorthands for going to data provider exact size
        lazyDataView.setRowCountFromDataProvider();

        // Getting an item based on index
        final GridDemo.Item firstItem = lazyDataView.getItemOnRow(0);
        // can be used for extending/selecting the first item
        grid.setDetailsVisible(firstItem, true);
        // Throws IndexOutOfBoundsException when index not in active range
        // -> Could be changed to return Optional<T> instead for lazy case ?

        lazyDataView.addSizeChangeListener(event -> {
            /*
             * Size can be used with lazy case too to show the number of items
             * to the user. Will get API for determining whether the size is
             * exact / estimate. https://github.com/vaadin/flow/issues/8643
             */
            nameColumn.setFooter("Number of items: " + event.getSize());
        });
        // will be removed for lazy, can be added back as something else if
        // needed (?) https://github.com/vaadin/flow/issues/8643
        lazyDataView.getSize();

        /*
         * Checking if the given item is currently available, or filtered out or
         * not in the active range. "Probably handy" so not removing it for
         * lazy.
         */
        final boolean contains = lazyDataView.contains(firstItem);

        /*
         * Getting all items with current filtering and sorting, for exporting
         * as a report. Can be costly for lazy case but still needed.
         */
        new Button("Export as .pdf",
                event -> createReport(lazyDataView.getItems()));
    }
    
    /*
     * Use case / Problems 2: expose fetched data
     *
     * When showing a listing component to the user, I need to know the current
     * sorting and filtering so I can export exactly the same data.
     *
     * When showing list of data to a user, I want to know the data set fetched
     * by the data provider, so that I can provide controls for the user to
     * navigate to the next/previous item
     *
     * When using a data provider in a component, I want to easily show the size
     * of the data set in another component because that is the UX I want
     *
     * Design principles are the same as for use case 1.
     *
     * Solution follows data view API approach with introducing API in ListDataView
     * that knows that a ListDataProvider is used.
     *
     * Supported by Grid, Select and CheckBoxGroup for now. ComboBox and RadioButtonGroup
     * to be added.
     *
     * In addition to the use cases above, also included convenience methods for
     * adding/removing updating data, specially for drag-and-drop operations
     * with in-memory data.
     */
    public void exposingDataViewFromComponent() {
        Grid<GridDemo.Person> grid = new Grid<>();
        final GridDemo.PersonService service = new GridDemo.PersonService();
        final List<GridDemo.Person> personList = service.fetch(0, 50);

        GridListDataView<GridDemo.Person> dataView;
        // collection
        dataView = grid.setDataSource(personList);

        // from array
        dataView = grid.setDataSource(new GridDemo.Person());

        // list data provider
        final ListDataProvider<GridDemo.Person> dataProvider = new ListDataProvider<>(
                personList);
        dataView = grid.setDataSource(dataProvider);

        // No API for data source Stream<T> as it API bloating and confusing,
        // since the given stream is collected anyway

        dataView = grid.getListDataView(); // will throw for anything not a
                                           // ListDataProvider

        // API added by HasListDataView
    }

    public void oldApi_deprecatedParts() {
        Grid<GridDemo.Person> grid = new Grid<>();
        // only deprecated in the component, not in HasItems for other components
        grid.setItems(new GridDemo.Person());
        grid.setItems(Stream.of());
        grid.setDataProvider(new ListDataProvider<>(Collections.EMPTY_LIST));

        // to be able to do some of the same things before, one would have to
        grid.setItems(Collections.EMPTY_LIST);
        ListDataProvider<GridDemo.Person> dataProvider = (ListDataProvider<GridDemo.Person>) grid
                .getDataProvider();
        // or use static method
        ListDataProvider listDataProvider = DataProvider.ofCollection(Collections.EMPTY_LIST);
    }

    public void exposingFetchedData_inMemory_navigatingOutsideTheGrid() {
        // see demo for traversing to next/previous item
        new GridDemo().createExternalDataNavigationGrid();
    }

    public void exposingFetchedData_filteringAndSorting_possibleThroughDataView() {
        final List<GridDemo.Person> personList = new GridDemo.PersonService()
                .fetch(0, 50);

        Grid<GridDemo.Person> grid = new Grid<>();
        final GridListDataView<GridDemo.Person> dataView = grid
                .setDataSource(personList);

        /*
         * Commonly used filtering and sorting API is added to ListDataView,
         * even though it just passes to DataProvider. The user should not have
         * to get (list) data provider first.
         */
        dataView.setFilter(person -> person.getAge() > 30);
        dataView.addFilter(person -> Objects
                .equals(person.getAddress().getCity(), "New York"));
        new Button("Clear filter", event -> dataView.removeFilters());
        
        // external sorting with grid makes little sense, but the API is there for other components..
        dataView.setSortOrder(GridDemo.Person::getAge, SortDirection.ASCENDING)
                .addSortOrder(GridDemo.Person::getLastName,
                        SortDirection.ASCENDING);
    }
    
    public void exposingFetchedData_inMemory_easyAddingAndRemoving() {
        // most typically used in DnD operations where shown the best:
        new GridDemo().createRowReordering();
        new GridDemo().createContextMenu();
    }




    private void setupItemGridColumns(Grid<GridDemo.Item> grid) {
        // don't look at this
        grid.addColumn(GridDemo.Item::getName).setHeader("Name");
        grid.addColumn(new NumberRenderer<>(GridDemo.Item::getPrice, "$ %(,.2f",
                Locale.US, "$ 0.00")).setHeader("Price");
        grid.addColumn(
                new LocalDateTimeRenderer<>(GridDemo.Item::getPurchaseDate,
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT,
                                FormatStyle.MEDIUM)))
                .setHeader("Purchase date and time").setFlexGrow(2);
        grid.addColumn(
                new LocalDateRenderer<>(GridDemo.Item::getEstimatedDeliveryDate,
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                .setHeader("Estimated delivery date");
    }

    private void createReport(Stream<GridDemo.Item> items) {
        // magic
    }

}
