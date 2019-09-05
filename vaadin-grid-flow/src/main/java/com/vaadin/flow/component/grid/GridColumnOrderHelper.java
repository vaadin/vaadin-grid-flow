package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements the logic necessary for proper column reordering:
 * {@link Grid#setColumnOrder(List)}.
 * @author mavi
 */
class GridColumnOrderHelper<T> {
    private final Grid<T> grid;

    GridColumnOrderHelper(Grid<T> grid) {
        this.grid = grid;
    }

    /**
     * See {@link Grid#setColumnOrder(List)}.
     *
     * @param columns
     *              the new column order, not null.
     */
    void setColumnOrder(List<Grid.Column<T>> columns) {
        Objects.requireNonNull(columns, "columns");
        final Set<Grid.Column<T>> newColumns = new HashSet<>(columns);
        if (newColumns.size() < columns.size()) {
            throw new IllegalArgumentException("A column is present multiple times in the list of columns: " +
                    columns.stream().map(Grid.Column::getKey).collect(Collectors.joining(", ")));
        }
        final List<Grid.Column<T>> currentColumns = grid.getColumns();
        if (newColumns.size() < currentColumns.size()) {
            final String missingColumnKeys = currentColumns.stream()
                    .filter(col -> !newColumns.contains(col))
                    .map(Grid.Column::getKey)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("The 'columns' list is missing the following columns: "
                    + missingColumnKeys);
        }
        for (Grid.Column<T> column : newColumns) {
            grid.checkPartOfThisGrid(column);
        }

        // The column may be potentially wrapped in ColumnGroup if multiple headers
        // or footers are used. If that's the case, we need to reorder the
        // top-level columns.
        // If two columns share same top-level column then they're joined in header
        // or footer. LinkedHashSet will allow us to effectively detect this case
        // and fail. LinkedHashSet also preserves the order so we'll still be able
        // to sort the columns properly.
        final LinkedHashSet<ColumnBase<?>> topLevelColumns = new LinkedHashSet<>();
        for (Grid.Column<T> column : columns) {
            final ColumnBase<?> topLevelColumn = findTopLevelColumn(column);
            if (!topLevelColumns.add(topLevelColumn)) {
                throw new IllegalArgumentException("Grid contains joined header/footer cells; see column '" + column.getKey() + "'");
            }
        }

        for (ColumnBase<?> topLevelColumn : topLevelColumns) {
            topLevelColumn.getElement().removeFromParent();
        }
        for (ColumnBase<?> topLevelColumn : topLevelColumns) {
            grid.getElement().appendChild(topLevelColumn.getElement());
        }

        final List<ColumnBase<?>> columnsPreOrder = getColumnsPreOrder();
        for (ColumnLayer columnLayer : grid.getColumnLayers()) {
            columnLayer.updateColumnOrder(columnsPreOrder);
        }
    }

    /**
     * Computes a total order of all columns and column groups, in pre-order
     * order.
     *
     * @return a list of all columns and column groups, ordered with preorder
     */
    private List<ColumnBase<?>> getColumnsPreOrder() {
        return getColumnsPreOrder(grid);
    }

    private List<ColumnBase<?>> getColumnsPreOrder(Component parent) {
        final List<ColumnBase<?>> list = new ArrayList<>();
        if (parent instanceof AbstractColumn) {
            list.add((ColumnBase<?>) parent);
        }
        parent.getChildren()
                .filter(col -> col instanceof AbstractColumn)
                .forEach(parent1 -> list.addAll(getColumnsPreOrder(parent1)));
        return list;
    }

    private ColumnBase<?> findTopLevelColumn(AbstractColumn<?> column) {
        final Component parent = column.getParent().get();
        if (parent.equals(grid)) {
            return column;
        } else {
            return findTopLevelColumn((AbstractColumn<?>) parent);
        }
    }
}
