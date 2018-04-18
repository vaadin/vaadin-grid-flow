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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        protected AbstractColumn<?> getColumn() {
            return columnComponent;
        }

        public abstract void setText(String text);

        public abstract void setComponent(Component component);

    }

    protected ColumnLayer layer;
    protected List<CELL> cells;

    private Function<AbstractColumn<?>, CELL> cellCtor;

    AbstractRow(ColumnLayer layer, Function<AbstractColumn<?>, CELL> cellCtor) {
        this.layer = layer;
        this.cells = layer.getColumns().stream().map(cellCtor)
                .collect(Collectors.toList());
        this.cellCtor = cellCtor;
    }

    public List<CELL> getCells() {
        return cells;
    }

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

    public CELL join(CELL... cells) {
        // TODO assert that this is the out-most row and all given cells belong
        // to this row

        List<AbstractColumn<?>> childColumns = new ArrayList<>();
        Stream.of(cells).map(CELL::getColumn).forEach(column -> {
            childColumns.addAll(((ColumnGroup) column).getChildColumns());
        });

        ColumnGroup group = new ColumnGroup(layer.getGrid(), childColumns);
        layer.getGrid().getElement().appendChild(group.getElement());
        // TODO insert to correct place

        // TODO remove empty column groups

        this.cells.removeAll(Arrays.asList(cells));
        CELL cell = cellCtor.apply(group);
        this.cells.add(cell);
        return cell;
    }
}
