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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.data.HasHierarchicalDataProvider;
import com.vaadin.data.provider.HierarchicalDataCommunicator;
import com.vaadin.data.provider.HierarchicalDataProvider;
import com.vaadin.data.provider.HierarchicalQuery;
import com.vaadin.data.provider.TreeGridArrayUpdater;
import com.vaadin.data.provider.TreeUpdate;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;
import elemental.json.JsonValue;

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

    public final class UpdateQueue implements TreeUpdate {
        private List<SerializableRunnable> queue = new ArrayList<>();

        private UpdateQueue(int size) {
            // 'size' property is not synchronized by the web component since
            // there are no events for it, but we
            // need to sync it otherwise server will overwrite client value with
            // the old server one
            enqueue("$connector.updateSize", size);
            if (uniqueKeyProperty != null) {
                enqueue("$connector.updateUniqueItemIdPath", uniqueKeyProperty);
            }
            getElement().setProperty("size", size);
        }

        @Override
        public void set(int start, List<JsonValue> items) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()));
        }

        @Override
        public void set(int start, List<JsonValue> items,
                String parentKey) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()),
                    parentKey);
        }

        @Override
        public void clear(int start, int length) {
            if (!getDataCommunicator().hasExpandedItems()) {
                enqueue("$connector.clearExpanded");
            }
            enqueue("$connector.clear", start, length);
        }

        @Override
        public void clear(int start, int length, String parentKey) {
            enqueue("$connector.clear", start, length, parentKey);
        }

        @Override
        public void commit(int updateId) {
            enqueue("$connector.confirm", updateId);
            commit();
        }

        @Override
        public void commit(int updateId, String parentKey, int levelSize) {
            enqueue("$connector.confirmParent", updateId, parentKey,
                    levelSize);
            commit();
        }

        @Override
        public void commit() {
            queue.forEach(Runnable::run);
            queue.clear();
        }

        @Override
        public void enqueue(String name, Serializable... arguments) {
            queue.add(() -> getElement().callFunction(name, arguments));
        }

    }

    private final ValueProvider<T, String> defaultUniqueKeyProvider = item -> String
            .valueOf(item.hashCode());

    private ValueProvider<T, String> uniqueKeyProvider;

    private String uniqueKeyProperty;

    /**
     * Creates a new {@code TreeGrid} without support for creating columns based
     * on property names. Use an alternative constructor, such as
     * {@link TreeGrid#TreeGrid(Class)}, to create a {@code TreeGrid} that
     * automatically sets up columns based on the type of presented data.
     */
    public TreeGrid() {
        super(50, new TreeDataCommunicatorBuilder<T>());
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
        super(beanType, new TreeDataCommunicatorBuilder<T>());
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
    
    private static class TreeDataCommunicatorBuilder<T>
            extends DataCommunicatorBuilder<TreeGrid<T>, T> {

        @Override
        protected DataCommunicator<T> build(TreeGrid<T> grid) {
            withArrayUpdater(grid.createArrayUpdater());
            grid.uniqueKeyProperty = "key";
            getDataGenerator().addDataGenerator(grid::generateTreeData);


            return new HierarchicalDataCommunicator<>(getDataGenerator(),
                    (TreeGridArrayUpdater) getArrayUpdater(),
                    data -> grid.getElement()
                            .callFunction("$connector.updateData", data),
                    grid.getElement().getNode(),
                    item -> grid.getUniqueKeyProvider().apply(item));
        }
    }

    /**
     * Creates Array update strategy aware object that handles initialization
     * and creation of update of array on client side.
     * 
     * @return new TreeGridArrayUpdater object
     */
    protected TreeGridArrayUpdater createArrayUpdater() {
        return new TreeGridArrayUpdater() {
            @Override
            public UpdateQueue startUpdate(int sizeChange) {
                return new UpdateQueue(sizeChange);
            }

            @Override
            public void initialize() {
                initConnector();
                updateSelectionModeOnClient();
            }
        };
    }

    /**
     * Generates TreeGrid specific data for row sent to client.
     * 
     * @param item
     *            Target item
     * @param jsonObject
     *            Target json object to fill with TreeGrid specific data
     */
    protected void generateTreeData(T item, JsonObject jsonObject) {
        if (!jsonObject.hasKey(uniqueKeyProperty)) {
            jsonObject.put(uniqueKeyProperty,
                    getUniqueKeyProvider().apply(item));
        }
        Optional.ofNullable(getDataCommunicator().getParentItem(item))
                .ifPresent(parent -> jsonObject.put("parentUniqueKey",
                        getUniqueKeyProvider().apply(parent)));
    }

    /**
     * Sets property name and value provider for unique key in row's generated
     * JSON.
     * <p>
     * Default property name is 'key' and value is generated by bean's hashCode
     * method.
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
     *
     * @param valueProvider
     *            the value provider
     * @return the created hierarchy column
     */
    public Column<T> addHierarchyColumn(ValueProvider<T, ?> valueProvider) {
        Column<T> column = addColumn(TemplateRenderer
                .<T> of("<vaadin-grid-tree-toggle "
                        + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>[[item.name]]"
                        + "</vaadin-grid-tree-toggle>")
                .withProperty("leaf",
                        item -> !getDataCommunicator().hasChildren(item))
                .withProperty("name",
                        value -> String.valueOf(valueProvider.apply(value))));
        column.setComparator(
                ((a, b) -> compareMaybeComparables(valueProvider.apply(a),
                        valueProvider.apply(b))));

        return column;
    }

    /**
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     * <p>
     * Resets columns and their order based on bean properties.
     * <p>
     * This is a shortcut for removing all columns and then calling
     * {@link #addColumn(String)} for each property except hierarchy column in
     * the bean and {@link #addHierarchyColumn(String)} for the given
     * propertyName.
     * <p>
     * Previous column order is preserved.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * Note that this also resets the headers and footers.
     * 
     * @param propertyName
     *            a target hierarchy column property name
     * @return the created hierarchy column
     */
    public Column<T> setHierarchyColumn(String propertyName) {
        return setHierarchyColumn(propertyName, null);
    }

    /**
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     * <p>
     * Resets columns and their order based on bean properties.
     * <p>
     * This is a shortcut for removing all columns and then calling
     * {@link #addColumn(String)} for each property except hierarchy column in
     * the bean and {@link #addHierarchyColumn(String)} or
     * {@link #addHierarchyColumn(ValueProvider)} for the given propertyName.
     * <p>
     * Previous column order is preserved.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * Note that this also resets the headers and footers.
     * 
     * @param propertyName
     *            a target hierarchy column property name
     * @param valueProvider
     *            optional value provider
     * @return the created hierarchy column
     */
    public Column<T> setHierarchyColumn(String propertyName,
            ValueProvider<T, ?> valueProvider) {
        List<String> currentPropertyList = getColumns().stream()
                .map(Column::getKey).filter(Objects::nonNull)
                .collect(Collectors.toList());
        resetColumns(propertyName, valueProvider, currentPropertyList);
        return getColumnByKey(propertyName);
    }

    /**
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     * <p>
     * Sets the columns and their order based on the given properties.
     * <p>
     * This is a shortcut for removing all columns and then calling
     * {@link #addColumn(String)} for each property except hierarchy property in
     * the bean and {@link #addHierarchyColumn(String)} for the given
     * hierarchyPropertyName.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * Note that this also resets the headers and footers.
     * 
     * @param hierarchyPropertyName
     *            a target hierarchy column property name
     * @param valueProvider
     *            optional value provider
     * @param propertyNames
     *            set of properties to create columns for. Including given
     *            hierarchyPropertyName
     */
    public Column<T> setColumns(String hierarchyPropertyName,
            ValueProvider<T, ?> valueProvider,
            Collection<String> propertyNames) {
        if (getPropertySet() == null) {
            throw new UnsupportedOperationException(
                    "This method can't be used for a Grid that isn't constructed from a bean type");
        }
        resetColumns(hierarchyPropertyName, valueProvider, propertyNames);
        return getColumnByKey(hierarchyPropertyName);
    }

    private void resetColumns(String hierarchyPropertyName,
            ValueProvider<T, ?> valueProvider,
            Collection<String> propertyList) {
        getColumns().forEach(this::removeColumn);
        propertyList.stream().distinct().forEach(key -> {
            if (key.equals(hierarchyPropertyName)) {
                if (valueProvider != null) {
                    addHierarchyColumn(valueProvider)
                            .setKey(hierarchyPropertyName);
                } else {
                    addHierarchyColumn(key);
                }
            } else {
                addColumn(key);
            }
        });
    }

    private Column<T> addHierarchyColumn(String propertyName) {
        if (getPropertySet() == null) {
            throw new UnsupportedOperationException(
                    "This method can't be used for a Grid that isn't constructed from a bean type");
        }
        Objects.requireNonNull(propertyName,
                "Hierarchy Property name can't be null");

        PropertyDefinition<T, ?> property;
        try {
            property = getPropertySet().getProperty(propertyName).get();
        } catch (NoSuchElementException | IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Can't resolve hierarchy property name '" + propertyName
                            + "' from '" + getPropertySet() + "'");
        }
        return addHierarchyColumn(property);
    }

    private Column<T> addHierarchyColumn(PropertyDefinition<T, ?> property) {
        Column<T> column = addHierarchyColumn(
                item -> String.valueOf(property.getGetter().apply(item)))
                        .setHeader(property.getCaption());
        try {
            return column.setKey(property.getName());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Multiple columns for the same property: "
                            + property.getName());
        }
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setParentRequestedRange(int start, int length,
            String parentKey) {
        T item = getDataCommunicator().getKeyMapper().get(parentKey);
        if (item != null) {
            getDataCommunicator().setParentRequestedRange(start, length,
                    item);
        }
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void updateExpandedState(String key, boolean expanded) {
        T item = getDataCommunicator().getKeyMapper().get(key);
        if (item != null) {
            if (expanded) {
                expand(Arrays.asList(item), false, true);
            } else {
                collapse(Arrays.asList(item), false, true);
            }
        }
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void confirmParentUpdate(int id, String parentKey) {
        getDataCommunicator().confirmUpdate(id, parentKey);
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

    /**
     * Expands the given items.
     * 
     * @param items
     *            the items to expand
     * @param syncClient
     *            {@code true} if the changes should be synchronised to the
     *            client, {@code false} otherwise.
     * @param userOriginated
     *            {@code true} if a {@link ExpandEvent} triggered by this
     *            operation is user originated, {@code false} otherwise.
     */
    protected void expand(Collection<T> items, boolean syncClient,
            boolean userOriginated) {
        Collection<T> expandedItems = getDataCommunicator().expand(items,
                syncClient);
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
                getItemsWithChildrenRecursively(items, depth), true);
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

    /**
     * Collapse the given items.
     * 
     * @param items
     *            the collection of items to collapse
     * @param syncClient
     *            {@code true} if the changes should be synchronized to the
     *            client, {@code false} otherwise.
     * @param userOriginated
     *            {@code true} if a {@link CollapseEvent} triggered by this
     *            operation is user originated, {@code false} otherwise.
     */
    protected void collapse(Collection<T> items, boolean syncClient,
            boolean userOriginated) {
        Collection<T> collapsedItems = getDataCommunicator().collapse(items,
                syncClient);
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
        getDataCommunicator()
                .collapse(getItemsWithChildrenRecursively(items, depth), true);
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
     *            the items to expand recursively
     * @param depth
     *            the maximum depth of recursion
     * @return collection of given items and their children recursively until
     *         the given depth
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

    /**
     * Expands or collapses given item depending on its current state.
     * 
     * @param item
     *            the target item to expand or collapse
     */
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
