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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.vaadin.data.TreeData;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.internal.Range;
import com.vaadin.flow.internal.StateNode;

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

    private final TreeGridArrayUpdater arrayUpdater;
    private final StateNode stateNode;
    private HierarchyMapper<T, ?> mapper;
    private DataGenerator<T> dataGenerator;
    private final ValueProvider<T, String> uniqueKeyProvider;

    private final Map<String, CommunicationController<T>> dataControllers = new HashMap<>();

    private KeyMapper<T> uniqueKeyMapper = new KeyMapper<T>() {

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
            return Optional.ofNullable(uniqueKeyProvider)
                    .map(provider -> provider.apply(object))
                    .orElse(super.createKey());
        }
    };

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
     *            Unique key provider for a row. If null, then using Grid's
     *            default key generator.
     */
    public HierarchicalDataCommunicator(DataGenerator<T> dataGenerator,
            TreeGridArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            ValueProvider<T, String> uniqueKeyProvider) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode);
        this.dataGenerator = dataGenerator;
        this.arrayUpdater = arrayUpdater;
        this.stateNode = stateNode;
        this.uniqueKeyProvider = uniqueKeyProvider;
        try {
            // TODO get rid of this reflection
            FieldUtils.writeField(this, "keyMapper", uniqueKeyMapper, true);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setDataProvider(new TreeDataProvider<>(new TreeData<>()), null);
    }


    private void requestFlush(TreeUpdate update) {
        SerializableConsumer<ExecutionContext> flushRequest = context -> {
            update.commit();
        };
        stateNode.runWhenAttached(ui -> ui.getInternals().getStateTree()
                .beforeClientResponse(stateNode, flushRequest));
    }

    private void requestFlush(CommunicationController<T> update) {
        SerializableConsumer<ExecutionContext> flushRequest = context -> {
            update.flush();
        };
        stateNode.runWhenAttached(ui -> ui.getInternals().getStateTree()
                .beforeClientResponse(stateNode, flushRequest));
    }

    /**
     * Resets all the data.
     * <p>
     * It effectively resends all available data.
     */
    @Override
    public void reset() {
        super.reset();

        if (!dataControllers.isEmpty()) {
            dataControllers.values()
                    .forEach(CommunicationController::unregisterPassivatedKeys);
            dataControllers.clear();

            TreeUpdate update = arrayUpdater
                    .startUpdate(getHierarchyMapper().getRootSize());
            update.enqueue("$connector.ensureHierarchy");
            requestFlush(update);
        }
    }

    @Override
    public Stream<T> fetchFromProvider(int offset, int limit) {
        // Instead of adding logic to this class, delegate request to the
        // separate object handling hierarchies.
        return mapper.fetchRootItems(Range.withLength(offset, limit));
    }

    public void setParentRequestedRange(int page, int start, int length,
            T parentItem) {
        String parentKey = uniqueKeyProvider.apply(parentItem);

        CommunicationController<T> controller = dataControllers.get(parentKey);
        if (controller == null) {
            controller = new CommunicationController<>(parentKey,
                    getKeyMapper(), mapper, dataGenerator,
                    size -> arrayUpdater.startUpdate(getDataProviderSize()),
                    (pkey, range) -> mapper
                            .fetchChildItems(getKeyMapper().get(pkey),
                            range));
            dataControllers.put(parentKey, controller);
        }

        controller.setRequestRange(start, length);
        requestFlush(controller);
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
        }
        mapper = createHierarchyMapper(dataProvider);

        // Set up mapper for requests
        mapper.setBackEndSorting(getBackEndSorting());
        mapper.setInMemorySorting(getInMemorySorting());
        mapper.setFilter(getFilter());

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

    public void confirmUpdate(int id, String parentKey) {
        Optional.ofNullable(dataControllers.get(parentKey)).ifPresent(controller -> {
            controller.confirmUpdate(id);

            // Not absolutely necessary, but doing it right away to release
            // memory earlier
            requestFlush(controller);
        });
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
                CommunicationController<T> controller = dataControllers
                        .remove(getKeyMapper().key(item));
                if (controller != null) {
                    controller.unregisterPassivatedKeys();
                }
            }
        });
        if (syncAndRefresh) {
            TreeUpdate update = arrayUpdater
                    .startUpdate(getHierarchyMapper().getRootSize());
            update.enqueue("$connector.collapseItems",
                        collapsedItems.stream().map(this::generateJson)
                                .collect(JsonUtils.asArray()));
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
     * @param pageSize
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
     * @param pageSize
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
            TreeUpdate update = arrayUpdater
                    .startUpdate(getHierarchyMapper().getRootSize());
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

    /**
     * Returns depth of item in the tree starting from zero representing a root.
     * 
     * @param item
     *            Target item
     * @return depth of item in the tree or -1 if item is null
     */
    public int getDepth(T item) {
        return mapper.getDepth(item);
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

    public void setInMemorySorting(SerializableComparator<T> comparator) {
        if (mapper != null) {
            mapper.setInMemorySorting(comparator);
        }
        super.setInMemorySorting(comparator);
    }

    protected <F> void setFilter(F filter) {
        if (mapper != null) {
            mapper.setFilter(filter);
        }
        // super.setFilter(filter);
    }

    public boolean hasExpandedItems() {
        return mapper.hasExpandedItems();
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
