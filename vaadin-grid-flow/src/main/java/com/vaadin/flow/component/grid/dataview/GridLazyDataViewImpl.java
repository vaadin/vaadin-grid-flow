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

import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.SizeEstimateCallback;

public class GridLazyDataViewImpl<T> extends AbstractDataView<T>
        implements GridLazyDataView<T> {

    private final DataCommunicator<T> dataCommunicator;

    /**
     * Creates a new instance and verifies the passed data provider is
     * compatible with this data view implementation.
     *
     * @param component
     */
    public GridLazyDataViewImpl(DataCommunicator<T> dataCommunicator,
            Component component) {
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
    public Stream<T> getAllItems() {
        return null;
    }

    @Override
    public int getDataSize() {
        return 0;
    }

    @Override
    public boolean isItemPresent(T item) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Stream<T> getCurrentItems() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public T getItemOnRow(int rowIndex) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void selectItemOnRow(int rowIndex) {
        // TODO
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void setDefinedSize(
            CallbackDataProvider.CountCallback<T, Void> callback) {
        getDataCommunicator().setSizeCallback(callback);
    }

    @Override
    public void setUndefinedSize(int initialSizeEstimate) {
        getDataCommunicator().setInitialSizeEstimate(initialSizeEstimate);
    }

    @Override
    public void setUndefinedSize(
            SizeEstimateCallback<T, Void> callback) {
        getDataCommunicator().setSizeEstimateCallback(callback);
    }

    @Override
    public void setDefinedSize(boolean definedSize) {
        getDataCommunicator().setDefinedSize(definedSize);
    }

    @Override
    public boolean isDefinedSize() {
        return getDataCommunicator().isDefinedSize();
    }
}
