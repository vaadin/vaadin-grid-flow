package com.vaadin.flow.component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QueryDataCommunicator;
import com.vaadin.flow.data.provider.SizeChangeHandler;
import com.vaadin.flow.data.provider.SizeChangeListener;
import com.vaadin.flow.internal.Range;
import com.vaadin.flow.shared.Registration;

public class GridDataViewer<T> implements SizeChangeHandler {

    private Grid<T> grid;
    int latestSize = 0;
    Set<SizeChangeListener> sizeChangeListeners;

    public GridDataViewer(Grid<T> grid) {
        this.grid = grid;
        grid.getQueryDataCommunicator().setSizeChangeHandler(this);
    }

    public Registration addSizeChangeListener(SizeChangeListener listener) {
        if (sizeChangeListeners == null) {
            sizeChangeListeners = new HashSet<>();
        }
        sizeChangeListeners.add(listener);
        return () -> sizeChangeListeners.remove(listener);
    }

    // get all items with given filters and sort ordering.
    public Stream<T> getAllItems() {
        return grid.getDataCommunicator().getDataProvider()
                .fetch(grid.getQueryDataCommunicator().buildQuery());
    }

    // This is the last total size sent to the client.
    public int getDataSize() {
        return latestSize;
    }

    // Could be getActiveItems() or getRequestedItems()
    public Stream<T> getCurrentItemSet() {
        final DataKeyMapper<T> keyMapper = grid.getDataCommunicator()
                .getKeyMapper();
        return grid.getQueryDataCommunicator().getActiveKeyOrder().stream()
                .map(keyMapper::get);
    }

    public T getItemOnRow(int index) {
        if (index > getDataSize()) {
            throw new IndexOutOfBoundsException(
                    "index needs to be less than the full data size");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException(
                    "index needs to be zero or greater");
        }
        Range currentRange = grid.getQueryDataCommunicator().getRange();
        // getting new range is a problem as it is done on beforeClientResponse.
        // Also it would break the what we have on client info.
        // instead getting directly from dataprovider
        if (currentRange.getStart() > index || currentRange.getEnd() <= index) {
            final DataProvider<T, ?> dataProvider = grid.getDataCommunicator()
                    .getDataProvider();
            return (T) dataProvider
                    .fetch(grid.getQueryDataCommunicator().buildQuery(index, 1))
                    .findFirst().get();
        }
        return grid.getDataCommunicator().getKeyMapper()
                .get(grid.getQueryDataCommunicator().getActiveKeyOrder()
                        .get(index - currentRange.getStart()));
    }

    public void selectItemOnRow(int index) {
        T item = getItemOnRow(index);
        grid.select(item);
    }

    public T getNextÏtem(T item) {
        QueryDataCommunicator<T> dataCommunicator = grid.getQueryDataCommunicator();
        if (dataCommunicator.getKeyMapper().has(item)) {
            final List<String> activeKeyOrder = dataCommunicator
                    .getActiveKeyOrder();
            final int itemIndex = activeKeyOrder
                    .indexOf(dataCommunicator.getKeyMapper().key(item));
            if (itemIndex < activeKeyOrder.size() - 1) {
                return dataCommunicator.getKeyMapper()
                        .get(activeKeyOrder.get(itemIndex + 1));
            } else if (getDataSize() - 1 > itemIndex) {
                Range currentRange = dataCommunicator.getRange();
                dataCommunicator.setRequestedRange(currentRange.getStart(),
                        currentRange.getEnd() + 1);
                return dataCommunicator.getKeyMapper()
                        .get(activeKeyOrder.get(itemIndex + 1));
            }
        }
        return null;
    }

    public T getPreviousÏtem(T item) {
        QueryDataCommunicator<T> dataCommunicator = grid.getQueryDataCommunicator();
        if (dataCommunicator.getKeyMapper().has(item)) {
            final List<String> activeKeyOrder = dataCommunicator
                    .getActiveKeyOrder();
            final int itemIndex = activeKeyOrder
                    .indexOf(dataCommunicator.getKeyMapper().key(item));
            if (itemIndex > 0) {
                return dataCommunicator.getKeyMapper()
                        .get(activeKeyOrder.get(itemIndex - 1));
            } else if (dataCommunicator.getRange().getStart() > 0) {
                Range currentRange = dataCommunicator.getRange();
                dataCommunicator.setRequestedRange(currentRange.getStart() - 1,
                        currentRange.getEnd());
                return dataCommunicator.getKeyMapper()
                        .get(activeKeyOrder.get(itemIndex - 1));
            }
        }
        return null;
    }

    @Override
    public void sizeEvent(int size) {
        if (size != latestSize && sizeChangeListeners != null) {
            sizeChangeListeners.forEach(listener -> listener.sizeChanged(
                    new SizeChangeListener.SizeChangeEvent(grid, size)));
        }
        latestSize = size;
    }
}
