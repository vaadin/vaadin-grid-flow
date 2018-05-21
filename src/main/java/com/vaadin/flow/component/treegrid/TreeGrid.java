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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.data.HasHierarchicalDataProvider;
import com.vaadin.data.provider.HierarchicalDataCommunicator;
import com.vaadin.data.provider.HierarchicalDataProvider;
import com.vaadin.data.provider.HierarchicalQuery;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
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
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.shared.Registration;

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

    private final ValueProvider<T, String> defaultUniqueKeyProvider = item -> ""
            + item.hashCode();

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
     * Creates a new {@code TreeGrid} with an initial set of columns for each of
     * the bean's properties. The property-values of the bean will be converted
     * to Strings. Full names of the properties will be used as the
     * {@link Column#setKey(String) column keys} and the property captions will
     * be used as the {@link Column#setHeader(String) column headers}.
     * 
     * @param beanType
     *            the bean type to use, not {@code null}
     */
    public TreeGrid(Class<T> beanType) {
        super(beanType);
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

    @Override
    protected DataCommunicator<T> createDataCommunicator(
            CompositeDataGenerator<T> gridDataGenerator,
            ArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode) {
        uniqueKeyProperty = "uniquekey";
        gridDataGenerator
                .addDataGenerator((T item, JsonObject jsonObject) -> {
                    jsonObject.put(uniqueKeyProperty,
                            getUniqueKeyProvider().apply(item));
                    Optional.ofNullable(
                            getDataCommunicator().getParentItem(item))
                            .ifPresent(
                                    parent -> jsonObject.put("parentUniqueKey",
                                            getUniqueKeyProvider()
                                                    .apply(parent)));
                    Optional.ofNullable(getDataCommunicator().getIndex(item))
                            .ifPresent(
                                    idx -> jsonObject.put("scaledIndex", idx));

                });
        return new HierarchicalDataCommunicator<>(gridDataGenerator,
                arrayUpdater, dataUpdater, stateNode,
                item -> getUniqueKeyProvider().apply(item));
    }

    /**
     * Sets property name and value provider for unique key in row's generated
     * JSON.
     * <p>
     * Default property name is 'uniquekey' and value is generated by bean's
     * hashCode method.
     * </p>
     * 
     * @param propertyName
     *            Property name in JSON data
     * @param uniqueKeyProvider
     *            Value provider for the target property in JSON data
     */
    public void setUniqueKeyDataGenerator(String propertyName,
            ValueProvider<T, String> uniqueKeyProvider) {
        this.uniqueKeyProperty = propertyName;
        this.uniqueKeyProvider = uniqueKeyProvider;

        getDataProvider().refreshAll();
    }

    /**
     * Gets value provider for unique key in row's generated JSON.
     * 
     * @return ValueProvider for unique key for row
     */
    public ValueProvider<T, String> getUniqueKeyProvider() {
        return Optional.ofNullable(uniqueKeyProvider)
                .orElse(defaultUniqueKeyProvider);
    }

    /**
     * Sets value provider for unique key in row's generated JSON.
     * <p>
     * <code>null</code> reverts to default. Default value is generated by
     * bean's hashCode method.
     * 
     * @param uniqueKeyProvider
     *            ValueProvider for unique key for row
     */
    public void setUniqueKeyProvider(
            ValueProvider<T, String> uniqueKeyProvider) {
        this.uniqueKeyProvider = uniqueKeyProvider;
    }

    /**
     * Adds an ExpandEvent listener to this TreeGrid.
     *
     * @see ExpandEvent
     *
     * @param listener
     *            the listener to add
     * @return a registration for the listener
     */
    public Registration addExpandListener(
            ComponentEventListener<ExpandEvent<T, TreeGrid<T>>> listener) {
        return ComponentUtil.addListener(this, ExpandEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Adds a CollapseEvent listener to this TreeGrid.
     *
     * @see CollapseEvent
     *
     * @param listener
     *            the listener to add
     * @return a registration for the listener
     */
    public Registration addCollapseListener(
            ComponentEventListener<CollapseEvent<T, TreeGrid<T>>> listener) {
        return ComponentUtil.addListener(this, CollapseEvent.class,
                (ComponentEventListener) listener);
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        if (!(dataProvider instanceof HierarchicalDataProvider)) {
            throw new IllegalArgumentException(
                    "TreeGrid only accepts hierarchical data providers");
        }
        super.setDataProvider(dataProvider);
    }

    /**
     * Adds a new Hierarchy column to this {@link Grid} with a value provider.
     * The value is converted to String when sent to the client by using
     * {@link String#valueOf(Object)}.
     * <p>
     * Hierarchy column is rendered by using 'vaadin-grid-tree-toggle' web
     * component.
     * <p>
     * <em>NOTE:</em> For displaying components, see
     * {@link #addComponentColumn(ValueProvider)}. For using build-in renderers,
     * see {@link #addColumn(Renderer)}.
     *
     * @param valueProvider
     *            the value provider
     * @return the created hierarchy column
     * @see #addComponentColumn(ValueProvider)
     * @see #addColumn(Renderer)
     */
    public Column<T> addHierarchyColumn(ValueProvider<T, ?> valueProvider) {
        String template = "<vaadin-grid-tree-toggle leaf=\"[[item.leaf]]\" expanded=\"{{expanded}}\""
                + " level=\"[[level]]\">[[item.name]]</vaadin-grid-tree-toggle>";
        Column<T> column = addColumn(TemplateRenderer
                .<T> of(template)
                .withProperty("leaf",
                        item -> !getDataCommunicator().hasChildren(item))
                .withProperty("name",
                        value -> String.valueOf(valueProvider.apply(value)))
        // .withEventHandler("onExpandedChange",
        // this::toggleExpandedState)
        );
        column.setComparator(
                ((a, b) -> compareMaybeComparables(valueProvider.apply(a),
                        valueProvider.apply(b))));

        return column;
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setParentRequestedRange(int page, int length,
            String parentKey) {
        T item = getDataCommunicator().getKeyMapper()
                .get(String.valueOf(parentKey));
        getDataCommunicator().setParentRequestedRange(page, length, item);
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void updateExpandedState(String key, boolean expanded) {
        T item = getDataCommunicator().getKeyMapper().get(String.valueOf(key));
        if (expanded) {
            expand(Arrays.asList(item), false, true);
        } else {
            collapse(Arrays.asList(item), false, true);
        }
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
        expand(items, true, false);
    }

    protected void expand(Collection<T> items, boolean syncAndRefresh,
            boolean userOriginated) {
        Collection<T> expandedItems = getDataCommunicator().expand(items,
                getPageSize(), syncAndRefresh);
        fireEvent(new ExpandEvent<T, TreeGrid<T>>(this, userOriginated,
                expandedItems));
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
        collapse(items, true, false);
    }

    protected void collapse(Collection<T> items, boolean syncAndRefresh,
            boolean userOriginated) {
        Collection<T> collapsedItems = getDataCommunicator().collapse(items,
                syncAndRefresh);
        fireEvent(new CollapseEvent<T, TreeGrid<T>>(this, userOriginated,
                collapsedItems));
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

    /**
     * Gets given items and their children recursively until the given depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that
     * {@code getItemsWithChildrenRecursively(items, 0)} gets only the given
     * items while {@code getItemsWithChildrenRecursively(items, 2)} gets the
     * given items as well as their children and grandchildren.
     * </p>
     * 
     * @param items
     * @param depth
     * @return
     */
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

    /** Expands or collapses given item depending its current state. */
    protected void toggleExpandedState(T item) {
        if (isExpanded(item)) {
            collapse(Arrays.asList(item), false, true);
        } else {
            expand(Arrays.asList(item), false, true);
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
    public HierarchicalDataProvider<T, SerializablePredicate<T>> getDataProvider() {
        if (!(super.getDataProvider() instanceof HierarchicalDataProvider)) {
            return null;
        }
        return (HierarchicalDataProvider<T, SerializablePredicate<T>>) super.getDataProvider();
    }
}
