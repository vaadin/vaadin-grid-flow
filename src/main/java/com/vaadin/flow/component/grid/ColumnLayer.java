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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of {@code <vaadin-grid-column>} or
 * {@code <vaadin-grid-column-group>} components that are on the same hierarchy
 * level. It can be used for a header row or a footer row or both.
 * 
 * @author Vaadin Ltd.
 */
class ColumnLayer {

    private Grid<?> grid;
    private List<AbstractColumn<?>> columns;

    private HeaderRow headerRow;
    private FooterRow footerRow;

    public ColumnLayer(Grid<?> grid) {
        this.grid = grid;
        this.columns = new ArrayList<>();
    }

    public ColumnLayer(Grid<?> grid, List<AbstractColumn<?>> columns) {
        this.grid = grid;
        this.columns = columns;
    }

    public void addColumn(AbstractColumn<?> column) {
        this.columns.add(column);
        if (isHeaderRow()) {
            column.setHeader("");
        }
        if (isFooterRow()) {
            column.setFooter("");
        }
    }

    public HeaderRow asHeaderRow() {
        if (headerRow == null) {
            headerRow = new HeaderRow(this);
            columns.forEach(col -> col.setHeader(""));
        }
        return headerRow;
    }

    public FooterRow asFooterRow() {
        if (footerRow == null) {
            footerRow = new FooterRow(this);
            columns.forEach(col -> col.setFooter(""));
        }
        return footerRow;
    }

    public boolean isHeaderRow() {
        return headerRow != null;
    }

    public boolean isFooterRow() {
        return footerRow != null;
    }

    public Grid<?> getGrid() {
        return grid;
    }

    public List<AbstractColumn<?>> getColumns() {
        return columns;
    }

}
