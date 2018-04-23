/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.dom.Element;

/**
 * @author Vaadin Ltd
 *
 */
class ColumnGroupHelpers {

    public static List<AbstractColumn<?>> wrapInSeparateColumnGroups(
            Collection<AbstractColumn<?>> cols, Grid<?> grid) {
        return cols.stream().map(col -> wrapSingleColumn(col, grid))
                .collect(Collectors.toList());
    }

    public static ColumnGroup wrapInColumnGroup(Grid<?> grid,
            AbstractColumn<?>... cols) {
        ColumnGroup group = wrapSingleColumn(cols[0], grid);
        for (int i = 1; i < cols.length; i++) {
            group.getElement().appendChild(cols[i].getElement());
        }
        return group;
    }

    /**
     * Wraps the given column inside a column group and places this wrapper
     * group to the original column's place.
     */
    private static ColumnGroup wrapSingleColumn(AbstractColumn<?> col,
            Grid<?> grid) {

        Element parent = col.getElement().getParent();
        int index = parent.indexOfChild(col.getElement());

        col.getElement().removeFromParent();

        ColumnGroup group = new ColumnGroup(grid, col);
        parent.insertChild(index, group.getElement());

        return group;
    }

}
