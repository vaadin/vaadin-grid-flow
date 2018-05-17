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
package com.vaadin.flow.component.treegrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vaadin.data.HasHierarchicalDataProvider;
import org.vaadin.data.provider.HierarchicalDataCommunicator;
import org.vaadin.data.provider.HierarchicalDataProvider;
import org.vaadin.data.provider.HierarchicalQuery;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.ui.ItemCollapseAllowedProvider;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * A grid component for displaying hierarchical tabular data.
 *
 * @author Vaadin Ltd
 * @since 8.1
 *
 * @param <T>
 *            the grid bean type
 */
@HtmlImport("frontend://bower_components/vaadin-grid/src/vaadin-grid-tree-toggle.html")
public class TreeGrid<T> extends Grid<T>
        implements HasHierarchicalDataProvider<T> {

    private ValueProvider<T, String> uniqueKeyProvider;

    /**
     * Creates a new {@code TreeGrid} without support for creating columns based
     * on property names. Use an alternative constructor, such as
     * {@link TreeGrid#TreeGrid(Class)}, to create a {@code TreeGrid} that
     * automatically sets up columns based on the type of presented data.
     */
    public TreeGrid() {
        super();
    }

    /**
     * Creates a new {@code TreeGrid} that uses reflection based on the provided
     * bean type to automatically set up an initial set of columns. All columns
     * will be configured using the same {@link Object#toString()} renderer that
     * is used by {@link #addColumn(ValueProvider)}.
     *
     * @param beanType
     *            the bean type to use, not {@code null}
     */
    public TreeGrid(Class<T> beanType) {
        super(beanType);
        registerTreeGridRpc();
    }

    /**
     * Creates a new {@code TreeGrid} using the given
     * {@code HierarchicalDataProvider}, without support for creating columns
     * based on property names. Use an alternative constructor, such as
     * {@link TreeGrid#TreeGrid(Class)}, to create a {@code TreeGrid} that
     * automatically sets up columns based on the type of presented data.
     *
     * @param dataProvider
     *            the data provider, not {@code null}
     */
    public TreeGrid(HierarchicalDataProvider<T, ?> dataProvider) {
        this();
        setDataProvider(dataProvider);
    }

    private void registerTreeGridRpc() {
        /*-registerRpc((NodeCollapseRpc) (rowKey, rowIndex, collapse,
                userOriginated) -> {
            T item = getDataCommunicator().getKeyMapper().get(rowKey);
            if (collapse && getDataCommunicator().isExpanded(item)) {
                getDataCommunicator().collapse(item, rowIndex);
                fireCollapseEvent(
                        getDataCommunicator().getKeyMapper().get(rowKey),
                        userOriginated);
            } else if (!collapse && !getDataCommunicator().isExpanded(item)) {
                getDataCommunicator().expand(item, rowIndex);
                fireExpandEvent(
                        getDataCommunicator().getKeyMapper().get(rowKey),
                        userOriginated);
            }
        });
        
        registerRpc((FocusParentRpc) (rowKey, cellIndex) -> {
            Integer parentIndex = getDataCommunicator().getParentIndex(
                    getDataCommunicator().getKeyMapper().get(rowKey));
            if (parentIndex != null) {
                getRpcProxy(FocusRpc.class).focusCell(parentIndex, cellIndex);
            }
        });-*/
    }

    @Override
    protected DataCommunicator<T> createDataCommunicator(
            CompositeDataGenerator<T> gridDataGenerator,
            ArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode) {
        uniqueKeyProperty = "uniquekey";
        uniqueKeyProvider = item -> "" + item.hashCode();
        gridDataGenerator
                .addDataGenerator((T item, JsonObject jsonObject) -> jsonObject
                        .put(uniqueKeyProperty, uniqueKeyProvider.apply(item)));
        return new HierarchicalDataCommunicator<>(gridDataGenerator,
                arrayUpdater,
                dataUpdater, stateNode);
    }

    /**
     * Set unique key data provider. Given property name will be added to grid's
     * generated row json data. Default property name is 'uniquekey' and value
     * is bean object's hashCode.
     * 
     * @param propertyName
     *            Property name in json data
     * @param uniqueKeyProvider
     *            Value provider for the target property in json data
     */
    public void setUniqueKeyDataGenerator(String propertyName,
            ValueProvider<T, String> uniqueKeyProvider) {
        this.uniqueKeyProperty = propertyName;
        this.uniqueKeyProvider = uniqueKeyProvider;
        getDataProvider().refreshAll();
    }

    /**
     * Adds an ExpandListener to this TreeGrid.
     *
     * @see ExpandEvent
     *
     * @param listener
     *            the listener to add
     * @return a registration for the listener
     */
    /*-public Registration addExpandListener(ExpandListener<T> listener) {
        return addListener(ExpandEvent.class, listener,
                ExpandListener.EXPAND_METHOD);
    }-*/

    /**
     * Adds a CollapseListener to this TreeGrid.
     *
     * @see CollapseEvent
     *
     * @param listener
     *            the listener to add
     * @return a registration for the listener
     */
    /*-public Registration addCollapseListener(CollapseListener<T> listener) {
        return addListener(CollapseEvent.class, listener,
                CollapseListener.COLLAPSE_METHOD);
    }-*/

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        if (!(dataProvider instanceof HierarchicalDataProvider)) {
            throw new IllegalArgumentException(
                    "TreeGrid only accepts hierarchical data providers");
        }
        // getRpcProxy(TreeGridClientRpc.class).clearPendingExpands();
        super.setDataProvider(dataProvider);
    }

    /**
     * Get the currently set hierarchy column.
     *
     * @return the currently set hierarchy column, or {@code null} if no column
     *         has been explicitly set
     */
    public Column<T> getHierarchyColumn() {
        return getColumns().stream().findFirst().orElse(null); // TODO
        // return getColumnByInternalId(getState(false).hierarchyColumnId);
    }

    /**
     * Set the column that displays the hierarchy of this grid's data. By
     * default the hierarchy will be displayed in the first column.
     * <p>
     * Setting a hierarchy column by calling this method also sets the column to
     * be visible and not hidable.
     * <p>
     * <strong>Note:</strong> Changing the Renderer of the hierarchy column is
     * not supported.
     *
     * @param column
     *            the column to use for displaying hierarchy
     */
    public void setHierarchyColumn(Column<T> column) {
        Objects.requireNonNull(column, "column may not be null");
        if (!getColumns().contains(column)) {
            throw new IllegalArgumentException(
                    "Given column is not a column of this TreeGrid");
        }
        column.setVisible(true);
        // column.setHidable(false);
        // getState().hierarchyColumnId = getInternalIdForColumn(column); //
        // TODO
    }

    /**
     * Adds a new text column to this {@link Grid} with a value provider. The
     * value is converted to String when sent to the client by using
     * {@link String#valueOf(Object)}.
     * <p>
     * <em>NOTE:</em> For displaying components, see
     * {@link #addComponentColumn(ValueProvider)}. For using build-in renderers,
     * see {@link #addColumn(Renderer)}.
     *
     * @param valueProvider
     *            the value provider
     * @return the created column
     * @see #addComponentColumn(ValueProvider)
     * @see #addColumn(Renderer)
     */
    public Column<T> addHierarchyColumn(ValueProvider<T, ?> valueProvider) {
        String template = "<vaadin-grid-tree-toggle leaf=\"[[item.leaf]]\" expanded=\"{{expanded}}\""
                + " level=\"[[level]]\" on-expanded-changed=\"onExpandedChange\">[[item.name]]</vaadin-grid-tree-toggle>";
        Column<T> column = addColumn(TemplateRenderer
                .<T> of(template)
                .withProperty("leaf",
                        item -> !getDataCommunicator().hasChildren(item))
                .withProperty("name",
                        value -> String.valueOf(valueProvider.apply(value)))
                .withEventHandler("onExpandedChange",
                        this::toggleExpandedState));
        column.setComparator(
                ((a, b) -> compareMaybeComparables(valueProvider.apply(a),
                        valueProvider.apply(b))));
        return column;
    }

    /**
     * Set the column that displays the hierarchy of this grid's data. By
     * default the hierarchy will be displayed in the first column.
     * <p>
     * Setting a hierarchy column by calling this method also sets the column to
     * be visible and not hidable.
     * <p>
     * <strong>Note:</strong> Changing the Renderer of the hierarchy column is
     * not supported.
     *
     * @see Column#setId(String)
     *
     * @param id
     *            id of the column to use for displaying hierarchy
     */
    public void setHierarchyColumn(String id) {
        Objects.requireNonNull(id, "id may not be null");
        if (getColumnByKey(id) == null) {
            throw new IllegalArgumentException("No column found for given id");
        }
        setHierarchyColumn(getColumnByKey(id));
    }

    /**
     * Sets the item collapse allowed provider for this TreeGrid. The provider
     * should return {@code true} for any item that the user can collapse.
     * <p>
     * <strong>Note:</strong> This callback will be accessed often when sending
     * data to the client. The callback should not do any costly operations.
     * <p>
     * This method is a shortcut to method with the same name in
     * {@link HierarchicalDataCommunicator}.
     *
     * @param provider
     *            the item collapse allowed provider, not {@code null}
     *
     * @see HierarchicalDataCommunicator#setItemCollapseAllowedProvider(ItemCollapseAllowedProvider)
     */
    public void setItemCollapseAllowedProvider(
            ItemCollapseAllowedProvider<T> provider) {
        getDataCommunicator().setItemCollapseAllowedProvider(provider);
    }

    /*-@ClientCallable
    private void expand(String key) {
        T item = getDataCommunicator().getKeyMapper().get(String.valueOf(key));
        Optional.ofNullable(item).ifPresent(this::expand);
    }-*/

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setParentRequestedRange(int page, int length,
            String parentKey) {
        T item = getDataCommunicator().getKeyMapper()
                .get(String.valueOf(parentKey));
        getDataCommunicator().setParentRequestedRange(page, length, item);
    }

    /**
     * Expands the given items.
     * <p>
     * If an item is currently expanded, does nothing. If an item does not have
     * any children, does nothing.
     *
     * @param items
     *            the items to expand
     */
    public void expand(T... items) {
        expand(Arrays.asList(items));
    }

    /**
     * Expands the given items.
     * <p>
     * If an item is currently expanded, does nothing. If an item does not have
     * any children, does nothing.
     *
     * @param items
     *            the items to expand
     */
    public void expand(Collection<T> items) {
        expand(items, true);
    }

    protected void expand(Collection<T> items, boolean syncAndRefresh) {
        getDataCommunicator().expand(items, getPageSize(),
                syncAndRefresh);
        items.forEach(item -> {
            if (!isExpanded(item) && getDataCommunicator().hasChildren(item)) {
                // fireExpandEvent(item, false); TODO
            }
        });
    }

    /**
     * Expands the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code expandRecursively(items, 0)} expands only
     * the given items while {@code expandRecursively(items, 2)} expands the
     * given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for expanded nodes.
     *
     * @param items
     *            the items to expand recursively
     * @param depth
     *            the maximum depth of recursion
     * @since 8.4
     */
    public void expandRecursively(Stream<T> items, int depth) {
        expandRecursively(items.collect(Collectors.toList()), depth);
    }

    /**
     * Expands the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code expandRecursively(items, 0)} expands only
     * the given items while {@code expandRecursively(items, 2)} expands the
     * given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for expanded nodes.
     *
     * @param items
     *            the items to expand recursively
     * @param depth
     *            the maximum depth of recursion
     * @since 8.4
     */
    public void expandRecursively(Collection<T> items, int depth) {
        getDataCommunicator().expand(
                getItemsWithChildrenRecursively(items, depth),
                getPageSize(), true);
    }

    /**
     * Collapse the given items.
     * <p>
     * For items that are already collapsed, does nothing.
     *
     * @param items
     *            the collection of items to collapse
     */
    public void collapse(T... items) {
        collapse(Arrays.asList(items));
    }

    /**
     * Collapse the given items.
     * <p>
     * For items that are already collapsed, does nothing.
     *
     * @param items
     *            the collection of items to collapse
     */
    public void collapse(Collection<T> items) {
        collapse(items, true);
    }

    protected void collapse(Collection<T> items, boolean syncAndRefresh) {
        getDataCommunicator().collapse(items, syncAndRefresh);
        items.forEach(item -> {
            if (isExpanded(item)) {
                // fireCollapseEvent(item, false); TODO
            }
        });
    }
    /**
     * Collapse the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code collapseRecursively(items, 0)} collapses
     * only the given items while {@code collapseRecursively(items, 2)}
     * collapses the given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for collapsed nodes.
     *
     * @param items
     *            the items to collapse recursively
     * @param depth
     *            the maximum depth of recursion
     * @since 8.4
     */
    public void collapseRecursively(Stream<T> items, int depth) {
        collapseRecursively(items.collect(Collectors.toList()), depth);
    }

    /**
     * Collapse the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code collapseRecursively(items, 0)} collapses
     * only the given items while {@code collapseRecursively(items, 2)}
     * collapses the given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for collapsed nodes.
     *
     * @param items
     *            the items to collapse recursively
     * @param depth
     *            the maximum depth of recursion
     * @since 8.4
     */
    public void collapseRecursively(Collection<T> items, int depth) {
        getDataCommunicator().collapse(getItemsWithChildrenRecursively(items, depth),
                true);
    }

    protected Collection<T> getItemsWithChildrenRecursively(Collection<T> items,
            int depth) {
        List<T> itemsWithChildren = new ArrayList<>();
        if (depth < 0) {
            return itemsWithChildren;
        }
        items.forEach(item -> {
            if (getDataCommunicator().hasChildren(item)) {
                itemsWithChildren.add(item);
                itemsWithChildren
                        .addAll(getItemsWithChildrenRecursively(
                                getDataProvider()
                                        .fetchChildren(new HierarchicalQuery<>(
                                                null, item))
                                        .collect(Collectors.toList()),
                                depth - 1));
            }
        });
        return itemsWithChildren;
    }

    protected void toggleExpandedState(T item) {
        if (isExpanded(item)) {
            collapse(Arrays.asList(item), false);
        } else {
            expand(Arrays.asList(item), false);
        }
    }

    /**
     * Returns whether a given item is expanded or collapsed.
     *
     * @param item
     *            the item to check
     * @return true if the item is expanded, false if collapsed
     */
    public boolean isExpanded(T item) {
        return getDataCommunicator().isExpanded(item);
    }

    @Override
    public HierarchicalDataCommunicator<T> getDataCommunicator() {
        return (HierarchicalDataCommunicator<T>) super.getDataCommunicator();
    }

    @Override
    public HierarchicalDataProvider<T, ?> getDataProvider() {
        if (!(super.getDataProvider() instanceof HierarchicalDataProvider)) {
            return null;
        }
        return (HierarchicalDataProvider<T, ?>) super.getDataProvider();
    }

    /**
     * Emit an expand event.
     *
     * @param item
     *            the item that was expanded
     * @param userOriginated
     *            whether the expand was triggered by a user interaction or the
     *            server
     */
    /*-private void fireExpandEvent(T item, boolean userOriginated) {
        fireEvent(new ExpandEvent<>(this, item, userOriginated));
    }-*/

    /**
     * Emit a collapse event.
     *
     * @param item
     *            the item that was collapsed
     * @param userOriginated
     *            whether the collapse was triggered by a user interaction or
     *            the server
     */
    /*-private void fireCollapseEvent(T item, boolean userOriginated) {
        fireEvent(new CollapseEvent<>(this, item, userOriginated));
    }-*/

    /**
     * Gets the item collapse allowed provider.
     *
     * @return the item collapse allowed provider
     */
    public ItemCollapseAllowedProvider<T> getItemCollapseAllowedProvider() {
        return getDataCommunicator().getItemCollapseAllowedProvider();
    }
}
