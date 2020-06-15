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
package com.vaadin.flow.component.grid.it.dataview;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SizeEstimateQuery;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.Range;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;

public abstract class AbstractUndefinedSizeGridPage extends VerticalLayout
        implements BeforeEnterObserver {

    private class LazyLoadingProvider
            extends AbstractBackEndDataProvider<String, Void> {

        @Override
        public Stream<String> fetchFromBackEnd(Query<String, Void> query) {
            int limit = query.getLimit();
            int offset = query.getOffset();
            Div log = new Div();
            log.setId("log-" + fetchQueryCount);
            log.setText(fetchQueryCount + ":"
                    + Range.withLength(query.getOffset(), query.getLimit())
                            .toString());
            Logger.getLogger(getClass().getName()).info(String.format(
                    "DataProvider Query : limit %s offset %s", limit, offset));
            return IntStream.range(offset, offset + limit)
                    .mapToObj(index -> "DataProvider Item " + index);
        }

        @Override
        protected int sizeInBackEnd(Query<String, Void> query) {
            Logger.getLogger(getClass().getName()).info(String
                    .format("DataProvider Query : SIZE: %d", dataProviderSize));
            return dataProviderSize;
        }
    }

    public static final String DATA_COMMUNICATOR_MENU_ITEM_ID = "dc-mi";
    public static final String ESTIMATE_CALLBACK_MENU_ITEM_ID = "ec-mi";
    public static final String DATA_PROVIDER_MENU_ITEM_ID = "dp-mi";
    public static final String UNDEFINED_SIZE_BUTTON_ID = "undefined-size";
    public static final String DEFINED_SIZE_BUTTON_ID = "defined-size";
    public static final String DATA_PROVIDER_BUTTON_ID = "data-provider";
    public static final String INITIAL_ESTIMATE_INPUT_ID = "initial-size-input";
    public static final String SIZE_ESTIMATE_CALLBACK_INPUT_ID = "size-estimate-input";
    public static final String SIZE_ESTIMATE_CALLBACK_BUTTON_ID = "size-estimate-button";
    public static final String DATA_PROVIDER_SIZE_INPUT_ID = "data-provider-size-input";
    public static final String UNDEFINED_SIZE_BACKEND_SIZE_INPUT_ID = "fetchcallback";
    public static final int DEFAULT_INITIAL_SIZE_ESTIMATE = 500;
    public static final int DEFAULT_DATA_PROVIDER_SIZE = 1000;

    private LazyLoadingProvider dataProvider;
    private MenuBar menuBar;
    private Div logPanel;
    protected IntegerField initialEstimateInput;
    protected IntegerField estimateInput;
    protected IntegerField fetchCallbackSizeInput;
    protected IntegerField dataProviderSizeInput;
    protected Grid<String> grid;

    private int fetchQueryCount = 0;
    private int sizeEstimateQueryCount = 0;
    private int dataProviderSize = DEFAULT_DATA_PROVIDER_SIZE;
    private int fetchCallbackSize = -1;

    private int sizeCallbackEstimate = -1;
    private int initialSizeEstimate = -1;

    public AbstractUndefinedSizeGridPage() {
        add(menuBar = new MenuBar());

        initGrid();
        initDataProvider();
        initEstimateOptions();
        initDataCommunicatorOptions();
        initNavigationLinks();

        Div splitPanel = new Div();
        splitPanel.getElement().getStyle().set("display", "flex");
        splitPanel.setSizeFull();
        splitPanel.add(logPanel, grid);
        add(splitPanel);
        setFlexGrow(1, splitPanel);
        setSizeFull();
    }

    private void initNavigationLinks() {
        SubMenu subMenu = menuBar.addItem("Open initially with").getSubMenu();
        subMenu.add(
                new RouterLink("UndefinedSize", UndefinedSizeGridPage.class));
        subMenu.add(new RouterLink("InitialSizeEstimate",
                InitialSizeEstimateGridPage.class));
        subMenu.add(new RouterLink("SizeEstimateCallback",
                SizeEstimateCallbackGridPage.class));
        subMenu.add(new RouterLink("DefinedSize", DefinedSizeCallbackGridPage.class));
    }

    private void initGrid() {
        grid = new Grid<>();
        // it is enough to provide just one callback for fetching data lazyly
        GridLazyDataView<String> lazyDataView = grid
                .setDataSource(this::fakeFetch);
        grid.setHeight("100%");

        grid.addColumn(ValueProvider.identity()).setHeader("Name");
    }

    private void initDataProvider() {
        MenuItem menuItem = menuBar.addItem("Defined / Undefined size");
        menuItem.setId(DATA_PROVIDER_MENU_ITEM_ID);

        dataProvider = new LazyLoadingProvider();

        menuItem.getSubMenu()
                .addItem("withUndefinedSize() -> undefined size",
                        event -> switchToUndefinedSize())
                .setId(UNDEFINED_SIZE_BUTTON_ID);
        menuItem.getSubMenu().add(new Hr());

        menuItem.getSubMenu()
                .addItem("setDataProvider(FetchCallback) -> undefined size",
                        event -> switchToUndefinedSizeCallback())
                .setId("fetchcallback");
        dataProviderSizeInput = new IntegerField("Fixed size backend size");
        menuItem.getSubMenu().add(dataProviderSizeInput, new Hr());

        fetchCallbackSizeInput = new IntegerField("Undefined-size backend size",
                event -> fetchCallbackSize = event.getValue());
        fetchCallbackSizeInput.setId(UNDEFINED_SIZE_BACKEND_SIZE_INPUT_ID);
        fetchCallbackSizeInput.setWidthFull();
        menuItem.getSubMenu().add(fetchCallbackSizeInput, new Hr());

        menuItem.getSubMenu()
                .addItem("setDefinedSize(CountCallback) -> defined size",
                        event -> switchToDefinedSize())
                .setId(DEFINED_SIZE_BUTTON_ID);

        menuItem.getSubMenu()
                .addItem("setDataProvider(DataProvider) -> defined size",
                        event -> switchToDataProvider())
                .setId(DATA_PROVIDER_BUTTON_ID);

        dataProviderSizeInput.setId(DATA_PROVIDER_SIZE_INPUT_ID);
        dataProviderSizeInput.setValue(dataProviderSize);
        dataProviderSizeInput.setWidthFull();
        dataProviderSizeInput.addValueChangeListener(event -> {
            dataProviderSize = event.getValue();
            dataProvider.refreshAll();
        });
        dataProviderSizeInput.setEnabled(false);
        menuItem.getSubMenu().add(dataProviderSizeInput, new Hr());

        logPanel = new Div();
        logPanel.setWidth("200px");
        logPanel.setHeight("400px");
        logPanel.add("Queries:");

        MenuItem showLogs = menuItem.getSubMenu().addItem(
                "Show fetch query logs",
                event -> logPanel.setVisible(event.getSource().isChecked()));
        showLogs.setCheckable(true);
        showLogs.setChecked(true);
    }

    private void initEstimateOptions() {
        MenuItem menuItem = menuBar.addItem("Size Estimate Configuration");
        menuItem.setId(ESTIMATE_CALLBACK_MENU_ITEM_ID);
        menuItem.getSubMenu().addItem(
                "setUndefinedSize(SizeEstimateCallback) -> undefined size",
                event -> switchToSizeEstimateCallback())
                .setId(SIZE_ESTIMATE_CALLBACK_BUTTON_ID);

        initialEstimateInput = new IntegerField(
                "setUndefinedSize(int initialEstimate) -> undefined size:",
                event -> grid.getLazyDataView()
                        .withUndefinedSize(event.getValue()));
        initialEstimateInput.setId(INITIAL_ESTIMATE_INPUT_ID);
        initialEstimateInput.setWidthFull();

        estimateInput = new IntegerField("Next Size Estimate:",
                event -> sizeCallbackEstimate = event.getValue());
        estimateInput.setId(SIZE_ESTIMATE_CALLBACK_INPUT_ID);
        estimateInput.setPlaceholder("Test Default is +20% estimate");
        estimateInput.setWidthFull();
        menuItem.getSubMenu().add(initialEstimateInput, estimateInput);
    }

    private void initDataCommunicatorOptions() {
        IntegerField pageSizeInput = new IntegerField("Page Size", event -> {
            grid.setPageSize(event.getValue());
        });
        pageSizeInput.setValue(grid.getPageSize());
        pageSizeInput.setWidthFull();

        IntegerField bufferPagesInput = new IntegerField(
                "# pages to increase size", event -> grid.getDataCommunicator()
                        .setSizeIncreasePageCount(event.getValue()));
        bufferPagesInput.setValue(
                grid.getDataCommunicator().getSizeIncreasePageCount());
        bufferPagesInput.setWidthFull();

        MenuItem menuItem = menuBar.addItem("DataCommunicator Configuration");
        menuItem.setId(DATA_COMMUNICATOR_MENU_ITEM_ID);
        menuItem.getSubMenu().add(pageSizeInput, bufferPagesInput);
    }

    private int getSizeEstimate(SizeEstimateQuery<String, Void> query) {
        Div log = new Div();
        log.setId("log-" + sizeEstimateQueryCount);
        log.setText(sizeEstimateQueryCount++ + ": Size "
                + query.getPreviousSizeEstimate() + ", "
                + query.getRequestedRangeEnd());
        logPanel.addComponentAsFirst(log);
        if (sizeCallbackEstimate < 1) {
            // using +20% as the default
            int previousSizeEstimate = query.getPreviousSizeEstimate();
            if (previousSizeEstimate == 0) {
                return DEFAULT_INITIAL_SIZE_ESTIMATE;
            }
            return new BigDecimal(previousSizeEstimate)
                    .multiply(new BigDecimal(1.2, MathContext.DECIMAL32))
                    .intValue();
        } else {
            return sizeCallbackEstimate;
        }
    }

    protected void switchToDataProvider() {
        grid.setDataSource(dataProvider);
        dataProviderSizeInput.setEnabled(true);
    }

    protected void switchToDefinedSize() {
        grid.getLazyDataView().withDefinedSize(dataProvider::size);
        dataProviderSizeInput.setEnabled(true);
    }

    protected void switchToUndefinedSizeCallback() {
        grid.setDataSource(this::fakeFetch);
        dataProviderSizeInput.setEnabled(false);
    }

    protected void switchToUndefinedSize() {
        grid.getLazyDataView().withUndefinedSize();
        dataProviderSizeInput.setEnabled(false);
    }

    protected void switchToSizeEstimateCallback() {
        grid.getLazyDataView().withUndefinedSize(this::getSizeEstimate);
    }

    private Stream<String> fakeFetch(Query<String, Void> query) {
        int limit = query.getLimit();
        int offset = query.getOffset();
        int lastItemToFetch = offset + limit;
        if (fetchCallbackSize > 0 && (lastItemToFetch) > fetchCallbackSize) {
            lastItemToFetch = fetchCallbackSize;
        }
        Div log = new Div();
        log.setId("log-" + fetchQueryCount);
        log.setText(fetchQueryCount + ":" + Range
                .withLength(query.getOffset(), query.getLimit()).toString());
        fetchQueryCount++;
        logPanel.addComponentAsFirst(log);
        Logger.getLogger(getClass().getName()).info(String
                .format("Callback Query : limit %s offset %s", limit, offset));
        return IntStream.range(offset, lastItemToFetch)
                .mapToObj(index -> "Callback Item " + index);
    }
}
