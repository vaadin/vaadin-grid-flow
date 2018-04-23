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
 * <p>
 * The bottom-most layer contains {@code <vaadin-grid-column>} elements, the
 * second layer their parent {@code <vaadin-grid-column-group>} elements and so
 * on.
 * 
 * @author Vaadin Ltd.
 */
class ColumnLayer {

    private Grid<?> grid;
    private List<AbstractColumn<?>> columns;

    private HeaderRow headerRow;
    private FooterRow footerRow;

    ColumnLayer(Grid<?> grid) {
        this.grid = grid;
        this.columns = new ArrayList<>();
    }

    ColumnLayer(Grid<?> grid, List<AbstractColumn<?>> columns) {
        this.grid = grid;
        this.columns = columns;
    }

    protected void addColumn(AbstractColumn<?> column) {
        addColumn(this.columns.size(), column);
    }

    protected void addColumn(int index, AbstractColumn<?> column) {
        this.columns.add(index, column);
        if (isHeaderRow()) {
            column.setHeaderText("");
            headerRow.addCell(index, column);
        }
        if (isFooterRow()) {
            column.setFooterText("");
            footerRow.addCell(index, column);
        }
    }

    protected HeaderRow asHeaderRow() {
        if (headerRow == null) {
            headerRow = new HeaderRow(this);
            columns.forEach(col -> col.setHeaderText(""));
        }
        return headerRow;
    }

    protected FooterRow asFooterRow() {
        if (footerRow == null) {
            footerRow = new FooterRow(this);
            columns.forEach(col -> col.setFooterText(""));
        }
        return footerRow;
    }

    protected void setHeaderRow(HeaderRow headerRow) {
        this.headerRow = headerRow;
        if (headerRow != null) {
            headerRow.setLayer(this);
        }
    }

    protected void setFooterRow(FooterRow footerRow) {
        this.footerRow = footerRow;
        if (footerRow != null) {
            footerRow.setLayer(this);
        }
    }

    protected boolean isHeaderRow() {
        return headerRow != null;
    }

    protected boolean isFooterRow() {
        return footerRow != null;
    }

    protected Grid<?> getGrid() {
        return grid;
    }

    protected void setColumns(List<AbstractColumn<?>> columns) {
        this.columns = columns;
        if (headerRow != null) {
            headerRow.setColumns(columns);
        }
        if (footerRow != null) {
            footerRow.setColumns(columns);
        }
    }

    protected List<AbstractColumn<?>> getColumns() {
        return columns;
    }

}
