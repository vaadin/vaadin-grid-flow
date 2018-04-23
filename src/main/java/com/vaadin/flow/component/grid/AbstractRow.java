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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.AbstractRow.AbstractCell;
import com.vaadin.flow.component.grid.Grid.Column;

/**
 * Base class for header and footer rows
 * 
 * @author Vaadin Ltd.
 */
abstract class AbstractRow<CELL extends AbstractCell> {

    /**
     * Base class for header and footer cells.
     * 
     * @author Vaadin Ltd.
     */
    public static abstract class AbstractCell {

        /*
         * This is the <vaadin-grid-column> or <vaadin-grid-column-group> that
         * contains the header or footer template.
         */
        private AbstractColumn<?> columnComponent;

        AbstractCell(AbstractColumn<?> column) {
            this.columnComponent = column;
        }

        protected void setColumn(AbstractColumn<?> column) {
            this.columnComponent = column;
        }

        protected AbstractColumn<?> getColumn() {
            return columnComponent;
        }

        /**
         * Sets the text content of this cell.
         * <p>
         * This will remove a component set with
         * {@link #setComponent(Component)}.
         * 
         * @param text
         *            the text to be shown in this cell
         */
        public abstract void setText(String text);

        /**
         * Sets the component as the content of this cell.
         * <p>
         * This will remove text set with {@link #setText(String)}.
         * 
         * @param component
         *            the component to set
         */
        public abstract void setComponent(Component component);

    }

    protected ColumnLayer layer;
    protected List<CELL> cells;

    private Function<AbstractColumn<?>, CELL> cellCtor;

    AbstractRow(ColumnLayer layer, Function<AbstractColumn<?>, CELL> cellCtor) {
        this.layer = layer;
        this.cellCtor = cellCtor;
        cells = layer.getColumns().stream().map(cellCtor)
                .collect(Collectors.toList());
    }

    protected void setLayer(ColumnLayer layer) {
        this.layer = layer;
    }

    /**
     * Change the cells to wrap the given columns
     * 
     * @param columns
     *            new column components for the cells
     */
    protected void setColumns(List<AbstractColumn<?>> columns) {
        assert columns.size() == cells.size();

        IntStream.range(0, columns.size()).forEach(i -> {
            cells.get(i).setColumn(columns.get(i));
        });
    }

    protected void addCell(AbstractColumn<?> column) {
        cells.add(cellCtor.apply(column));
    }

    protected void addCell(int index, AbstractColumn<?> column) {
        cells.add(index, cellCtor.apply(column));
    }

    /**
     * Gets the cells that belong to this row.
     * 
     * @return the cells on this row
     */
    public List<CELL> getCells() {
        return cells;
    }

    /**
     * Gets the cell on this row that is on the given column.
     * 
     * @param column
     *            the column to find cell for
     * @return the corresponding cell
     */
    public CELL getCell(Column<?> column) {
        return getCellFor(column);
    }

    private CELL getCellFor(AbstractColumn<?> column) {
        return this.cells.stream().filter(cell -> cell.getColumn() == column)
                .findFirst().orElseGet(() -> {
                    Optional<Component> parent = column.getParent();
                    if (parent.isPresent()
                            && parent.get() instanceof AbstractColumn) {
                        return getCellFor((AbstractColumn<?>) parent.get());
                    } else {
                        throw new IllegalArgumentException(
                                "Cannot find a cell from this row that would correspond to the given column");
                    }
                });
    }

    /**
     * Joins the cells corresponding the given columns in the row.
     * 
     * @param columnsToMerge
     *            the columns of the cells that should be merged
     * @return the merged cell
     * @see #join(Collection)
     */
    public CELL join(Column<?>... columnsToMerge) {
        return join(Arrays.stream(columnsToMerge).map(this::getCell)
                .collect(Collectors.toList()));
    }

    /**
     * Replaces the given cells with a new cell that takes the full space of the
     * joined cells.
     * <p>
     * The cells to join must be adjacent cells in this row, and this row must
     * be the out-most row.
     * 
     * @param cells
     *            the cells to join
     * @return the merged cell
     */
    public CELL join(CELL... cells) {
        return join(Arrays.asList(cells));
    }

    /**
     * Replaces the given cells with a new cell that takes the full space of the
     * joined cells.
     * <p>
     * The cells to join must be adjacent cells in this row, and this row must
     * be the out-most row.
     * 
     * @param cells
     *            the cells to join
     * @return the merged cell
     */
    public CELL join(Collection<CELL> cells) {
        Grid<?> grid = layer.getGrid();
        if (!isOutMostRow()) {
            throw new IllegalArgumentException(
                    "Cells can be joined only on the out-most row");
        }
        if (grid.getLayers().indexOf(layer) == 0) {
            throw new IllegalArgumentException(
                    "Cells can not be joined on the bottom header row");
        }
        if (cells.size() < 2) {
            throw new IllegalArgumentException("Cannot join less than 2 cells");
        }
        if (!this.cells.containsAll(cells)) {
            throw new IllegalArgumentException(
                    "Cannot join cells that don't belong to this row");
        }

        List<AbstractColumn<?>> columnsToJoin = cells.stream()
                .map(CELL::getColumn).collect(Collectors.toList());

        int elementInsertIndex = columnsToJoin.stream()
                .mapToInt(
                        col -> grid.getElement().indexOfChild(col.getElement()))
                .min().getAsInt();
        columnsToJoin.forEach(col -> col.getElement().removeFromParent());

        int cellInsertIndex = cells.stream().mapToInt(this.cells::indexOf).min()
                .getAsInt();

        List<AbstractColumn<?>> childColumns = new ArrayList<>();
        columnsToJoin.forEach(col -> childColumns
                .addAll(((ColumnGroup) col).getChildColumns()));

        ColumnGroup group = new ColumnGroup(grid, childColumns);

        layer.getGrid().getElement().insertChild(elementInsertIndex,
                group.getElement());
        layer.addColumn(cellInsertIndex, group);

        layer.getColumns().removeAll(columnsToJoin);

        this.cells.removeAll(cells);

        return this.cells.get(cellInsertIndex);
    }

    private boolean isOutMostRow() {
        List<ColumnLayer> layers = layer.getGrid().getLayers();
        for (int i = layers.size() - 1; i >= 0; i--) {
            ColumnLayer layer = layers.get(i);
            if (this instanceof HeaderRow && layer.isHeaderRow()) {
                return this == layer.asHeaderRow();
            } else if (this instanceof FooterRow && layer.isFooterRow()) {
                return this == layer.asFooterRow();
            }
        }
        return false;
    }
}
