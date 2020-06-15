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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.SizeEstimateCallback;

/**
 * Implementation of the lazy data view for grid.
 * 
 * @param <T>
 *            the type of the items in grid
 */
public class GridLazyDataViewImpl<T> extends AbstractDataView<T>
        implements GridLazyDataView<T> {

    private final DataCommunicator<T> dataCommunicator;

    /**
     * Creates a new instance and verifies the passed data provider is
     * compatible with this data view implementation.
     * 
     * @param dataCommunicator
     *            the data communicator of the component
     * @param component
     *            the grid component
     */
    public GridLazyDataViewImpl(DataCommunicator<T> dataCommunicator,
            Grid<T> component) {
        super(dataCommunicator::getDataProvider, component);
        this.dataCommunicator = dataCommunicator;
    }

    private DataCommunicator<T> getDataCommunicator() {
        // verify that the data provider hasn't been changed to an incompatible
        // type
        if (dataCommunicator.getDataProvider().isInMemory()) {
            throw new IllegalStateException(String.format(
                    "LazyDataView cannot be created for Grid with an in-memory data provider. Existing data provider type was %s",
                    getDataCommunicator().getDataProvider().getClass()
                            .getSimpleName()));
        }
        return dataCommunicator;
    }

    @Override
    protected Class<?> getSupportedDataProviderType() {
        return BackEndDataProvider.class;
    }

    @Override
    public void withDefinedSize(
            CallbackDataProvider.CountCallback<T, Void> callback) {
        getDataCommunicator().setSizeCallback(callback);
    }

    @Override
    public void withUndefinedSize(int initialSizeEstimate) {
        getDataCommunicator().setInitialSizeEstimate(initialSizeEstimate);
    }

    @Override
    public void withUndefinedSize(SizeEstimateCallback<T, Void> callback) {
        getDataCommunicator().setSizeEstimateCallback(callback);
    }

    @Override
    public void withDefinedSize() {
        getDataCommunicator().setDefinedSize(true);
    }

    @Override
    public void withUndefinedSize() {
        getDataCommunicator().setDefinedSize(false);
    }

    @Override
    public boolean isDefinedSize() {
        return getDataCommunicator().isDefinedSize();
    }

    @Override
    public boolean contains(T item) {
        // TODO implement this
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public T getItemOnRow(int rowIndex) {
        // TODO use fetch query to get the item
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
