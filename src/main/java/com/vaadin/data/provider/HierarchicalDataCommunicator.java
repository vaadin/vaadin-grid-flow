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
package com.vaadin.data.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.vaadin.data.TreeData;
import com.vaadin.flow.component.grid.Grid.UpdateQueue;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.ExecutionContext;
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
    private final StateNode stateNode;
    private HierarchyMapper<T, ?> mapper;
    private DataGenerator<T> dataGenerator;
    private final ValueProvider<T, String> uniqueKeyProvider;

    private KeyMapper keyMapper = new KeyMapper<T>() {

        private T object;

        @Override
        public String key(T o) {
            this.object = o;
            try {
                return super.key(o);
            } finally {
                this.object = null;
            }
        }

        @Override
        protected String createKey() {
            return uniqueKeyProvider.apply(object);
        }
    };

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
     * @param uniqueKeyProvider
     */
    public HierarchicalDataCommunicator(DataGenerator<T> dataGenerator,
            ArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            ValueProvider<T, String> uniqueKeyProvider) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode);
        this.dataGenerator = dataGenerator;
        this.arrayUpdater = arrayUpdater;
        this.stateNode = stateNode;
        this.uniqueKeyProvider = uniqueKeyProvider;
        try {
            // TODO get rid of this reflection
            FieldUtils.writeField(this, "keyMapper", keyMapper, true);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setDataProvider(new TreeDataProvider<>(new TreeData<>()), null);
    }


    private void requestFlush(UpdateQueue update) {
        SerializableConsumer<ExecutionContext> flushRequest = context -> {
            flush(update);
        };
        stateNode.runWhenAttached(ui -> ui.getInternals().getStateTree()
                .beforeClientResponse(stateNode, flushRequest));
    }

    private void flush(UpdateQueue update) {
        update.commit();
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

        update.enqueue("$connector.confirmTreeLevel",
                uniqueKeyProvider.apply(parentItem), page,
                mapper.fetchChildItems(parentItem,
                        Range.withLength(page * length, length))
                        .map(this::generateJson).collect(JsonUtils.asArray()),
                mapper.countChildItems(parentItem));

        requestFlush(update);
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
     * method will have no effect if the row is already collapsed. The index is
     * provided by the client-side or calculated from a full data request.
     * {@code syncAndRefresh} indicates whether the changes should be
     * synchronised to the client and the data provider be notified.
     *
     * @param items
     *            items to collapse
     * @param syncAndRefresh
     *            {@code true} if the changes should be synchronised to the
     *            client and the data provider should be notified of the
     *            changes, {@code false} otherwise.
     */
    public void collapse(T item, boolean syncAndRefresh) {
        doCollapse(Arrays.asList(item), syncAndRefresh);
    }

    public Collection<T> collapse(Collection<T> items, boolean syncAndRefresh) {
        return doCollapse(items, syncAndRefresh);
    }

    private Collection<T> doCollapse(Collection<T> items,
            boolean syncAndRefresh) {
        List<T> collapsedItems = new ArrayList<>();
        items.forEach(item -> {
            if (mapper.collapse(item)) {
                collapsedItems.add(item);
            }
        });
        if (syncAndRefresh) {
            UpdateQueue update = (UpdateQueue) arrayUpdater
                    .startUpdate(getDataProviderSize());
            update.enqueue("$connector.collapseItems", collapsedItems.stream()
                    .map(this::generateJson).collect(JsonUtils.asArray()));
            requestFlush(update);
        }
        return collapsedItems;
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
     * item is already expanded or if it has no children. The index is provided
     * by the client-side or calculated from a full data request.
     * {@code syncAndRefresh} indicates whether the changes should be
     * synchronised to the client and the data provider be notified.
     *
     * @param items
     *            items to expand
     * @param syncAndRefresh
     *            {@code true} if the changes should be synchronised to the
     *            client and the data provider should be notified of the
     *            changes, {@code false} otherwise.
     */
    public void expand(T item, int pageSize,
            boolean syncAndRefresh) {
        doExpand(Arrays.asList(item), pageSize, syncAndRefresh);
    }

    public Collection<T> expand(Collection<T> items, int pageSize,
            boolean syncAndRefresh) {
        return doExpand(items, pageSize, syncAndRefresh);
    }

    private Collection<T> doExpand(Collection<T> items, int pageSize,
            boolean syncAndRefresh) {
        List<T> expandedItems = new ArrayList<>();
        items.forEach(item -> {
            if (mapper.expand(item)) {
                expandedItems.add(item);
            }
        });
        if (syncAndRefresh) {
            UpdateQueue update = (UpdateQueue) arrayUpdater
                    .startUpdate(getDataProviderSize());
            update.enqueue("$connector.expandItems",
                    expandedItems.stream().map(this::generateJson)
                            .collect(JsonUtils.asArray()));
            requestFlush(update);
        }
        return expandedItems;
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

        getDataProvider().refreshAll();
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

    /**
     * Returns index for the row or {@code null}.
     *
     * @param item
     *            the target item
     * @return the index or {@code null} for top-level and non-existing items
     */
    public Integer getIndex(T item) {
        return Optional.ofNullable(mapper.getIndex(item))
                .filter(index -> index >= 0).orElse(null);
    }

    /**
     * Returns parent item for the row or {@code null}.
     *
     * @param item
     *            the item to find the parent of
     * @return the parent item or {@code null} for top-level items
     */
    public T getParentItem(T item) {
        return mapper.getParentOfItem(item);
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
