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

package com.vaadin.flow.component.grid;

import java.util.Collection;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataViewImpl;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.ListDataView;

/**
 * This is the new Grid when Grid is made into AbstractGrid so that TreeGrid is
 * not
 * dependent on all the Grid features that would not apply for it.
 *
 * @param <T>
 *         item type
 */
public class DataGrid<T> extends Grid<T>
        implements HasListDataView<T, GridListDataView<T>> {
    GridDataView<T> dataView;

    public DataGrid() {
    }

    public DataGrid(int pageSize) {
        super(pageSize);
    }

    public DataGrid(Class<T> beanType, boolean autoCreateColumns) {
        super(beanType, autoCreateColumns);
    }

    public DataGrid(Class<T> beanType) {
        super(beanType);
    }

    @Override
    public GridListDataView<T> setDataProvider(
            ListDataProvider<T> dataProvider) {
        super.setDataProvider(dataProvider);
        return getListDataView();
    }

    @Override
    public GridListDataView<T> setDataProvider(Collection<T> items) {
        super.setDataProvider(DataProvider.ofCollection(items));
        return getListDataView();
    }

    @Override
    public GridListDataView<T> setDataProvider(Stream<T> items) {
        super.setDataProvider(DataProvider.fromStream(items));
        return getListDataView();
    }

    @Override
    public GridListDataView<T> setDataProvider(T... items) {
        super.setDataProvider(DataProvider.ofItems(items));
        return getListDataView();
    }

    @Override
    public GridListDataView<T> getListDataView() {
        if (getDataProvider() instanceof ListDataProvider) {
            if (dataView == null || !(dataView instanceof ListDataView)) {
                dataView = new GridListDataViewImpl<>(this);
            }
            return (GridListDataView) dataView;
        }
        throw new IllegalArgumentException(
                "Required ListDataProvider, but got " + getDataCommunicator()
                        .getClass().getSuperclass().getSimpleName());
    }
}
