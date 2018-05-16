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
package org.vaadin.data.provider;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.vaadin.data.TreeData;

import com.vaadin.flow.component.grid.Grid.UpdateQueue;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.internal.Range;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.ui.ItemCollapseAllowedProvider;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * Data communicator that handles requesting hierarchical data from
 * {@link HierarchicalDataProvider} and sending it to client side.
 *
 * @param <T>
 *            the bean type
 * @author Vaadin Ltd
 * @since 8.1
 */
public class HierarchicalDataCommunicator<T> extends DataCommunicator<T> {

    private final ArrayUpdater arrayUpdater;
    private HierarchyMapper<T, ?> mapper;
    private DataGenerator<T> dataGenerator;

    /**
     * Collapse allowed provider used to allow/disallow collapsing nodes.
     */
    private ItemCollapseAllowedProvider<T> itemCollapseAllowedProvider = t -> true;

    /**
     * Construct a new hierarchical data communicator backed by a
     * {@link TreeDataProvider}.
     * 
     * @param dataGenerator
     *            the data generator function
     * @param arrayUpdater
     *            array updater strategy
     * @param dataUpdater
     *            data updater strategy
     * @param stateNode
     *            the state node used to communicate for
     */
    public HierarchicalDataCommunicator(DataGenerator<T> dataGenerator,
            ArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode);
        this.dataGenerator = dataGenerator;
        this.arrayUpdater = arrayUpdater;
        setDataProvider(new TreeDataProvider<>(new TreeData<>()), null);
    }

    @Override
    public Stream<T> fetchFromProvider(int offset, int limit) {
        // Instead of adding logic to this class, delegate request to the
        // separate object handling hierarchies.
        return mapper.fetchRootItems(Range.withLength(offset, limit));
    }

    public void setParentRequestedRange(int page, int length, T parentItem) {
        UpdateQueue update = (UpdateQueue) arrayUpdater
                .startUpdate(getDataProviderSize());

        update.enqueue("$connector.confirmLevel",
                getKeyMapper().key(parentItem), page,
                mapper.fetchChildItems(parentItem,
                        Range.withLength(page * length, length))
                        .map(this::generateJson).collect(JsonUtils.asArray()),
                mapper.countChildItems(parentItem));

        int updateId;
        try {
            // TODO make nextUpdateId protected
            updateId = (int) FieldUtils.readField(this, "nextUpdateId", true);
            FieldUtils.writeField(this, "nextUpdateId", updateId + 1, true);

            update.commit(updateId);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public HierarchicalDataProvider<T, ?> getDataProvider() {
        return (HierarchicalDataProvider<T, ?>) super.getDataProvider();
    }

    /**
     * Set the current hierarchical data provider for this communicator.
     *
     * @param dataProvider
     *            the data provider to set, not <code>null</code>
     * @param initialFilter
     *            the initial filter value to use, or <code>null</code> to not
     *            use any initial filter value
     *
     * @param <F>
     *            the filter type
     *
     * @return a consumer that accepts a new filter value to use
     */
    public <F> SerializableConsumer<F> setDataProvider(
            HierarchicalDataProvider<T, F> dataProvider, F initialFilter) {
        SerializableConsumer<F> consumer = super.setDataProvider(dataProvider,
                initialFilter);

        // Remove old mapper
        if (mapper != null) {
            mapper.destroyAllData();
            // removeDataGenerator(mapper); TODO clear old mapper properly
        }
        mapper = createHierarchyMapper(dataProvider);

        // Set up mapper for requests
        mapper.setBackEndSorting(getBackEndSorting());
        mapper.setInMemorySorting(getInMemorySorting());
        mapper.setFilter(getFilter());
        mapper.setItemCollapseAllowedProvider(getItemCollapseAllowedProvider());

        // Provide hierarchy data to json
//        addDataGenerator(mapper);

        return consumer;
    }

    /**
     * Create new {@code HierarchyMapper} for the given data provider. May be
     * overridden in subclasses.
     *
     * @param dataProvider
     *            the data provider
     * @param <F>
     *            Query type
     * @return new {@link HierarchyMapper}
     */
    protected <F> HierarchyMapper<T, F> createHierarchyMapper(
            HierarchicalDataProvider<T, F> dataProvider) {
        return new HierarchyMapper<>(dataProvider);
    }

    /**
     * Set the current hierarchical data provider for this communicator.
     *
     * @param dataProvider
     *            the data provider to set, must extend
     *            {@link HierarchicalDataProvider}, not <code>null</code>
     * @param initialFilter
     *            the initial filter value to use, or <code>null</code> to not
     *            use any initial filter value
     *
     * @param <F>
     *            the filter type
     *
     * @return a consumer that accepts a new filter value to use
     */
    @Override
    public <F> SerializableConsumer<F> setDataProvider(
            DataProvider<T, F> dataProvider, F initialFilter) {
        if (dataProvider instanceof HierarchicalDataProvider) {
            return setDataProvider(
                    (HierarchicalDataProvider<T, F>) dataProvider,
                    initialFilter);
        }
        throw new IllegalArgumentException(
                "Only " + HierarchicalDataProvider.class.getName()
                        + " and subtypes supported.");
    }

    /**
     * Collapses the given item and removes its sub-hierarchy. Calling this
     * method will have no effect if the row is already collapsed.
     *
     * @param item
     *            the item to collapse
     */
    public void collapse(T item) {
        collapse(item, true);
    }

    /**
     * Collapses the given item and removes its sub-hierarchy. Calling this
     * method will have no effect if the row is already collapsed.
     * {@code syncAndRefresh} indicates whether the changes should be
     * synchronised to the client and the data provider be notified.
     *
     * @param item
     *            the item to collapse
     * @param syncAndRefresh
     *            {@code true} if the changes should be synchronised to the
     *            client and the data provider should be notified of the
     *            changes, {@code false} otherwise.
     */
    public void collapse(T item, boolean syncAndRefresh) {
        Integer index = syncAndRefresh ? mapper.getIndexOf(item).orElse(null)
                : null;
        doCollapse(item, index, syncAndRefresh);
    }

    /**
     * Collapses the given item and removes its sub-hierarchy. Calling this
     * method will have no effect if the row is already collapsed.
     *
     * @param item
     *            the item to collapse
     * @param index
     *            the index of the item
     */
    public void collapse(T item, Integer index) {
        doCollapse(item, index, true);
    }

    /**
     * Collapses given item and removes its sub-hierarchy. Calling this method
     * will have no effect if the row is already collapsed. The index is
     * provided by the client-side or calculated from a full data request.
     *
     *
     * @param item
     *            the item to collapse
     * @param index
     *            the index of the item
     * @deprecated Use {@link #collapse(Object, Integer)} instead.
     */
    @Deprecated
    public void doCollapse(T item, Optional<Integer> index) {
        doCollapse(item, index.orElse(null), true);
    }

    /**
     * Collapses the given item and removes its sub-hierarchy. Calling this
     * method will have no effect if the row is already collapsed. The index is
     * provided by the client-side or calculated from a full data request.
     * {@code syncAndRefresh} indicates whether the changes should be
     * synchronised to the client and the data provider be notified.
     *
     * @param item
     *            the item to collapse
     * @param index
     *            the index of the item
     * @param syncAndRefresh
     *            {@code true} if the changes should be synchronised to the
     *            client and the data provider should be notified of the
     *            changes, {@code false} otherwise.
     */
    private void doCollapse(T item, Integer index, boolean syncAndRefresh) {
        Range removedRows = mapper.collapse(item, index);
        if (syncAndRefresh) {
            /*-if (!reset && !removedRows.isEmpty()) {
                getClientRpc().removeRows(removedRows.getStart(),
                        removedRows.length());
            }-*/
            refresh(item);
        }
    }

    /**
     * Expands the given item. Calling this method will have no effect if the
     * item is already expanded or if it has no children.
     *
     * @param item
     *            the item to expand
     */
    public void expand(T item, int pageSize) {
        expand(item, pageSize, true);
    }

    /**
     * Expands the given item. Calling this method will have no effect if the
     * item is already expanded or if it has no children. {@code syncAndRefresh}
     * indicates whether the changes should be synchronised to the client and
     * the data provider be notified.
     *
     * @param item
     *            the item to expand
     * @param syncAndRefresh
     *            {@code true} if the changes should be synchronised to the
     *            client and the data provider should be notified of the
     *            changes, {@code
     *         false} otherwise.
     */
    public void expand(T item, int pageSize, boolean syncAndRefresh) {
        Integer index = syncAndRefresh ? mapper.getIndexOf(item).orElse(null)
                : null;
        doExpand(item, index, pageSize, syncAndRefresh);
    }

    /**
     * Expands the given item at the given index. Calling this method will have
     * no effect if the item is already expanded.
     *
     * @param item
     *            the item to expand
     * @param index
     *            the index of the item
     */
    public void expand(T item, Integer index, int pageSize) {
        doExpand(item, index, pageSize, true);
    }

    /**
     * Expands the given item. Calling this method will have no effect if the
     * item is already expanded or if it has no children. The index is provided
     * by the client-side or calculated from a full data request.
     * {@code syncAndRefresh} indicates whether the changes should be
     * synchronised to the client and the data provider be notified.
     *
     * @param item
     *            the item to expand
     * @param index
     *            the index of the item
     * @param syncAndRefresh
     *            {@code true} if the changes should be synchronised to the
     *            client and the data provider should be notified of the
     *            changes, {@code false} otherwise.
     */
    private void doExpand(T item, Integer index, int pageSize,
            boolean syncAndRefresh) {
        Range addedRows = mapper.expand(item, index);
        /*-if (syncAndRefresh) {
            if (!reset && !addedRows.isEmpty()) {
                getClientRpc().insertRows(addedRows.getStart(),
                        addedRows.length());
                Stream<T> children = mapper.fetchItems(item,
                        Range.withLength(0, addedRows.length()));
                pushData(addedRows.getStart(),
                        children.collect(Collectors.toList()));
            }
        }-*/
    }

    /**
     * Expands the given item at given index. Calling this method will have no
     * effect if the row is already expanded. The index is provided by the
     * client-side or calculated from a full data request.
     *
     * @param item
     *            the item to expand
     * @param index
     *            the index of the item
     * @see #expand(Object)
     * @deprecated use {@link #expand(Object, Integer)} instead
     */
    @Deprecated
    public void doExpand(T item, Optional<Integer> index) {
        expand(item, index.orElse(null));
    }

    /**
     * Returns whether given item has children.
     *
     * @param item
     *            the item to test
     * @return {@code true} if item has children; {@code false} if not
     */
    public boolean hasChildren(T item) {
        return mapper.hasChildren(item);
    }

    /**
     * Returns whether given item is expanded.
     *
     * @param item
     *            the item to test
     * @return {@code true} if item is expanded; {@code false} if not
     */
    public boolean isExpanded(T item) {
        return mapper.isExpanded(item);
    }

    /**
     * Sets the item collapse allowed provider for this
     * HierarchicalDataCommunicator. The provider should return {@code true} for
     * any item that the user can collapse.
     * <p>
     * <strong>Note:</strong> This callback will be accessed often when sending
     * data to the client. The callback should not do any costly operations.
     *
     * @param provider
     *            the item collapse allowed provider, not {@code null}
     */
    public void setItemCollapseAllowedProvider(
            ItemCollapseAllowedProvider<T> provider) {
        Objects.requireNonNull(provider, "Provider can't be null");
        itemCollapseAllowedProvider = provider;
        // Update hierarchy mapper
        mapper.setItemCollapseAllowedProvider(provider);

        // getActiveDataHandler().getActiveData().values().forEach(this::refresh);
        // TODO
    }

    /**
     * Returns parent index for the row or {@code null}.
     *
     * @param item
     *            the item to find the parent of
     * @return the parent index or {@code null} for top-level items
     */
    public Integer getParentIndex(T item) {
        return mapper.getParentIndex(item);
    }

    // TODO javadoc
    public int getDepth(T item) {
        return mapper.getDepth(item);
    }

    /**
     * Gets the item collapse allowed provider.
     *
     * @return the item collapse allowed provider
     */
    public ItemCollapseAllowedProvider<T> getItemCollapseAllowedProvider() {
        return itemCollapseAllowedProvider;
    }

    @Override
    public int getDataProviderSize() {
        return mapper.getRootSize();
    }

    @Override
    public void setBackEndSorting(List<QuerySortOrder> sortOrder) {
        if (mapper != null) {
            mapper.setBackEndSorting(sortOrder);
        }
        super.setBackEndSorting(sortOrder);
    }

    public void setInMemorySorting(Comparator<T> comparator) {
        if (mapper != null) {
            mapper.setInMemorySorting(comparator);
        }
        // super.setInMemorySorting(comparator);
    }

    protected <F> void setFilter(F filter) {
        if (mapper != null) {
            mapper.setFilter(filter);
        }
        // super.setFilter(filter);
    }

    /**
     * Returns the {@code HierarchyMapper} used by this data communicator.
     *
     * @return the hierarchy mapper used by this data communicator
     */
    protected HierarchyMapper<T, ?> getHierarchyMapper() {
        return mapper;
    }

    private JsonValue generateJson(T item) {
        JsonObject json = Json.createObject();
        json.put("key", getKeyMapper().key(item));
        dataGenerator.generateData(item, json);
        return json;
    }
}