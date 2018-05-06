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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.AbstractRow.AbstractCell;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.dom.Element;

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

    /**
     * Change this row to wrap the given layer
     * 
     * @param layer
     *            the layer to wrap
     */
    protected void setLayer(ColumnLayer layer) {
        this.layer = layer;
        setColumns(layer.getColumns());
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

    protected void removeCell(AbstractColumn<?> columnComponent) {
        CELL cellToRemove = cells.stream()
                .filter(cell -> cell.getColumn().equals(columnComponent))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "removeCell() should never be called for a column component "
                                + "that doesn't have a corresponding cell in this row."));
        cells.remove(cellToRemove);
    }

    /**
     * Gets the cells that belong to this row as an unmodifiable list.
     * 
     * @return the cells on this row
     */
    public List<CELL> getCells() {
        return Collections.unmodifiableList(cells);
    }

    /**
     * Gets the cell on this row that is on the given column.
     * 
     * @param column
     *            the column to find cell for
     * @return the corresponding cell
     * @throws IllegalArgumentException
     *             if the column does not belong to the same grid as this row
     */
    public CELL getCell(Column<?> column) {
        return getCellFor(column);
    }

    private CELL getCellFor(AbstractColumn<?> column) {
        return getCells().stream().filter(cell -> cell.getColumn() == column)
                .findFirst().orElseGet(() -> {
                    Optional<Component> parent = column.getParent();
                    if (parent.isPresent()
                            && parent.get() instanceof AbstractColumn) {
                        return getCellFor((AbstractColumn<?>) parent.get());
                    } else {
                        throw new IllegalArgumentException(
                                "Cannot find a cell from this row that would "
                                        + "correspond to the given column");
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
        if (!isOutmostRow()) {
            throw new IllegalArgumentException(
                    "Cells can be joined only on the out-most row");
        }
        if (cells.size() < 2) {
            throw new IllegalArgumentException("Cannot join less than 2 cells");
        }
        if (!this.cells.containsAll(cells)) {
            throw new IllegalArgumentException(
                    "Cannot join cells that don't belong to this row");
        }

        List<CELL> sortedCells = cells.stream().sorted((c1, c2) -> Integer
                .compare(this.cells.indexOf(c1), this.cells.indexOf(c2)))
                .collect(Collectors.toList());

        int cellInsertIndex = this.cells.indexOf(sortedCells.get(0));
        IntStream.range(0, sortedCells.size()).forEach(i -> {
            if (this.cells.indexOf(sortedCells.get(i)) != cellInsertIndex + i) {
                throw new IllegalArgumentException(
                        "Cannot join cells that are not adjacent");
            }
        });

        List<AbstractColumn<?>> columnsToJoin = sortedCells.stream()
                .map(CELL::getColumn).collect(Collectors.toList());

        List<Column<?>> bottomColumnsToJoin = columnsToJoin.stream()
                .flatMap(col -> col.getBottomChildColumns().stream())
                .collect(Collectors.toList());

        List<ColumnLayer> layers = grid.getColumnLayers();

        if (layers.indexOf(layer) != layers.size() - 1) {
            // This is not the out-most layer. We might need to move the layer
            // upwards in the hierarchy.

            int layerInsertIndex = findFirstPossibleInsertIndex(
                    bottomColumnsToJoin, layers);

            if (layerInsertIndex == layers.indexOf(layer) + 1) {
                System.out.println("### NO need to move layer");
            } else {
                System.out.println("#### need to move layer");
                grid.removeColumnLayer(layer);
                layerInsertIndex--;

                ColumnLayer lowerLayer = layers.get(layerInsertIndex - 1);
                List<AbstractColumn<?>> childColumns = lowerLayer.getColumns()
                        .stream()
                        .filter(col -> bottomColumnsToJoin
                                .containsAll(col.getBottomChildColumns()))
                        .collect(Collectors.toList());

                List<AbstractColumn<?>> newColumns = new ArrayList<AbstractColumn<?>>();
                ColumnGroup groupForNewCell = null;

                Iterator<AbstractColumn<?>> leftColumns = layer.getColumns()
                        .stream()
                        .filter(column -> !columnsToJoin.contains(column))
                        .iterator();

                for (int i = 0; i < lowerLayer.getColumns().size(); i++) {
                    AbstractColumn<?> col = lowerLayer.getColumns().get(i);
                    if (childColumns.contains(col)) {
                        if (groupForNewCell == null) {
                            groupForNewCell = ColumnGroupHelpers
                                    .wrapInColumnGroup(grid, childColumns);
                            newColumns.add(groupForNewCell);
                        }
                    } else {
                        ColumnGroup group = ColumnGroupHelpers
                                .wrapInColumnGroup(grid, col);
                        AbstractColumn<?> oldGroup = leftColumns.next();
                        group.setHeaderRenderer(oldGroup.getHeaderRenderer());
                        group.setFooterRenderer(oldGroup.getFooterRenderer());
                        newColumns.add(group);
                    }
                }
                ColumnLayer newLayer = grid.insertColumnLayer(layerInsertIndex,
                        newColumns);
                if (layer.isHeaderRow()) {
                    newLayer.setHeaderRow(layer.asHeaderRow());
                }
                if (layer.isFooterRow()) {
                    newLayer.setFooterRow(layer.asFooterRow());
                }

                addCell(cellInsertIndex, groupForNewCell);
                this.cells.removeAll(cells);
                return this.cells.get(cellInsertIndex);
            }
        }

        Element parent = columnsToJoin.get(0).getElement().getParent();
        int elementInsertIndex = columnsToJoin.stream()
                .mapToInt(col -> parent.indexOfChild(col.getElement())).min()
                .getAsInt();
        columnsToJoin.forEach(col -> col.getElement().removeFromParent());

        List<AbstractColumn<?>> childColumns = new ArrayList<>();
        columnsToJoin.forEach(col -> childColumns
                .addAll(((ColumnGroup) col).getChildColumns()));

        ColumnGroup group = new ColumnGroup(grid, childColumns);

        parent.insertChild(elementInsertIndex, group.getElement());
        layer.addColumn(cellInsertIndex, group);

        layer.getColumns().removeAll(columnsToJoin);

        this.cells.removeAll(cells);

        return this.cells.get(cellInsertIndex);
    }

    /*
     * Finds a place in the column-layers where the layer corresponding to this
     * row could be inserted with the given columns joined.
     */
    private int findFirstPossibleInsertIndex(
            List<Column<?>> bottomColumnsToJoin, List<ColumnLayer> layers) {

        for (int i = layers.indexOf(layer) + 1; i < layers.size(); i++) {
            ColumnLayer possibleParentLayer = layers.get(i);

            boolean hasCommonParentColumnForColumnsToJoin = possibleParentLayer
                    .getColumns().stream()
                    .anyMatch(column -> column.getBottomChildColumns()
                            .containsAll(bottomColumnsToJoin));
            if (hasCommonParentColumnForColumnsToJoin) {
                return i;
            }

            List<AbstractColumn<?>> joinedColumns = possibleParentLayer
                    .getColumns().stream().filter(col -> ((ColumnGroup) col)
                            .getChildColumns().size() > 1)
                    .collect(Collectors.toList());
            boolean otherColumnsJoined = joinedColumns.stream()
                    .flatMap(col -> col.getBottomChildColumns().stream())
                    .anyMatch(col -> !bottomColumnsToJoin.contains(col));

            if (otherColumnsJoined) {
                throw new IllegalArgumentException(
                        "This set of cells can not be joined because of the hierarchical "
                                + "column group structure of the client-side web component.");
            }
        }
        return layers.size();
    }

    /**
     * Gets whether this is the top-most HeaderRow or the bottom-most FooterRow.
     * 
     * @return whether this is the outmost row
     */
    protected abstract boolean isOutmostRow();
}
