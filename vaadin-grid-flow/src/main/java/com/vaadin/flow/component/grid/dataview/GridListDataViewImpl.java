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

package com.vaadin.flow.component.grid.dataview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.models.auth.In;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SizeChangeEvent;
import com.vaadin.flow.data.provider.SizeChangeListener;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

public class GridListDataViewImpl<T> implements GridListDataView<T> {

    private final Grid grid;
    private final DataCommunicator<T> dataCommunicator;
    private final ListDataProvider<T> dataProvider;

    public GridListDataViewImpl(Grid grid) {
        this.grid = grid;
        dataCommunicator = grid.getDataCommunicator();
        final DataProvider<T, ?> dataProvider = dataCommunicator
                .getDataProvider();
        verifyDataProviderType(ListDataProvider.class, dataProvider.getClass(),
                "ListDataView");
        this.dataProvider = (ListDataProvider) dataProvider;
    }

    // To be moved to an abstract as it's usable for all dataprovider types

    /**
     * Verify that the DataProvider is of the wanted type.
     *
     * @param dataProviderType
     *         required DataProvider
     * @param dataProviderClass
     *         DataProvider to verify
     * @param dataViewName
     *         error message for expected type
     * @throws IllegalArgumentException
     *         for wrong DataProvider type
     */
    protected void verifyDataProviderType(Class dataProviderType,
            Class dataProviderClass, String dataViewName) {
        if (!dataProviderType.isAssignableFrom(dataProviderClass)) {
            final String message = String
                    .format("%s only supports %ss, but was given a '%s'",
                            dataViewName, dataProviderType.getSimpleName(),
                            dataProviderClass.getSuperclass().getSimpleName());
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public Stream<T> getCurrentItems() {
        final DataKeyMapper<T> keyMapper = dataCommunicator.getKeyMapper();
        return dataCommunicator.getActiveKeyOrdering().stream()
                .map(keyMapper::get);
    }

    @Override
    public T getItemOnRow(int rowIndex) {
        validateRowIndex(rowIndex);
        return getItems().get(rowIndex);
    }

    @Override
    public void selectItemOnRow(int rowIndex) {
        grid.select(getItemOnRow(rowIndex));
    }

    private void validateRowIndex(int rowIndex) {
        if (rowIndex < 0) {
            throw new IndexOutOfBoundsException(
                    "Row index can not be negative.");
        }
        if (rowIndex >= getDataSize()) {
            throw new IndexOutOfBoundsException(String.format(
                    "Row index must be inside the data size of '%s'.",
                    getDataSize()));
        }
    }

    @Override
    public boolean hasNextItem(T item) {
        final List<T> items = getItems();
        return items.contains(item) && items.indexOf(item) < items.size() - 1;
    }

    @Override
    public T getNextItem(T item) {
        if (hasNextItem(item)) {
            final List<T> items = new ArrayList<>(getItems());
            return items.get(items.indexOf(item) + 1);
        }
        return null;
    }

    @Override
    public boolean hasPreviousItem(T item) {
        final Collection<T> items = getItems();
        return items.contains(item) && !items.iterator().next().equals(item);
    }

    @Override
    public T getPreviousItem(T item) {
        if (hasPreviousItem(item)) {
            final ArrayList<T> items = new ArrayList<>(getItems());
            return items.get(items.indexOf(item) - 1);
        }
        return null;
    }

    @Override
    public void withFilter(SerializablePredicate<T> filter) {
        dataProvider.setFilter(filter);
        listeners.forEach(listener -> listener
                .sizeChanged(new SizeChangeEvent(grid, getDataSize())));
    }

    private List<T> getItems() {
        return getAllItems().collect(Collectors.toList());
    }

    @Override
    public Stream<T> getAllItems() {
        return dataProvider
                .fetch(dataCommunicator.buildQuery(0, Integer.MAX_VALUE));
    }

    @Override
    public int getDataSize() {
        return dataCommunicator.getDataSize();
    }

    @Override
    public boolean dataContainsItem(T item) {
        return getAllItems().anyMatch(t -> t.equals(item));
    }

    ArrayList<SizeChangeListener> listeners;

    @Override
    public Registration addSizeChangeListener(SizeChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>(0);
        }
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

}
