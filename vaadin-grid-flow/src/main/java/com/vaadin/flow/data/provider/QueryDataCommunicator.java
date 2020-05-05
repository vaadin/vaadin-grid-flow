package com.vaadin.flow.data.provider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.Range;
import com.vaadin.flow.internal.StateNode;

import elemental.json.JsonArray;

public class QueryDataCommunicator<T> extends DataCommunicator<T> {

    private SizeChangeHandler sizeChangeHandler = size -> {
    };

    private ActiveDataChangeHandler dataChangeHandler = () -> {};

    /**
     * Creates a new instance.
     *
     * @param dataGenerator
     *         the data generator function
     * @param arrayUpdater
     *         array updater strategy
     * @param dataUpdater
     *         data updater strategy
     * @param stateNode
     */
    public QueryDataCommunicator(DataGenerator<T> dataGenerator,
            ArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode);
    }

    public Query buildQuery() {
        return buildQuery(0, Integer.MAX_VALUE);
    }

    public Query buildQuery(int offset, int items) {
        return new QueryTrace(offset, items, getBackEndSorting(),
                getInMemorySorting(), getFilter());
    }

    public List<String> getActiveKeyOrder() {
        return Collections.unmodifiableList(getActiveKeyOrdering());
    }

    public Range getRange() {
        return Range.between(getRequestedRange().getStart(),
                getRequestedRange().getEnd());
    }

    @Override
    protected int getDataProviderSize() {
        int size = super.getDataProviderSize();
        sizeChangeHandler.sizeEvent(size);
        return size;
    }

    public void setSizeChangeHandler(SizeChangeHandler sizeChangeHandler) {
        this.sizeChangeHandler = sizeChangeHandler;
        // send latest assumed size as this might be gotten after first
        // get data provider size calls
        sizeChangeHandler.sizeEvent(getAssumedSize());
    }

    @Override
    protected Stream<T> fetchFromProvider(int offset, int limit) {
        Stream<T> fetch  =  super.fetchFromProvider(offset, limit);
        dataChangeHandler.activeDataChanged();
        return fetch;
    }

    public void setActiveDataChangeHandler(ActiveDataChangeHandler dataChangeHandler) {
        this.dataChangeHandler = dataChangeHandler;
    }
}
