/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.testbench;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * TestBench Element API for TreeGrid.
 *
 */
public class TreeGridElement extends GridElement {

    /**
     * Expands the row at the given index in the grid. This expects the first
     * column to have the hierarchy data.
     *
     * @param rowIndex
     *            0-based row index to expand
     * @see #expandWithClick(int, int)
     */
    public void expandWithClick(int rowIndex) {
        expandWithClick(rowIndex, 0);
    }

    /**
     * Expands the row at the given index in the grid with the given
     * hierarchical column index.
     *
     * @param rowIndex
     *            0-based row index to expand
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     */
    public void expandWithClick(int rowIndex, int hierarchyColumnIndex) {
        if (isRowExpanded(rowIndex, hierarchyColumnIndex)) {
            throw new IllegalStateException(
                    "The element at row " + rowIndex + " was expanded already");
        }
        getExpandToggleElement(rowIndex, hierarchyColumnIndex).click();
    }

    /**
     * Collapses the row at the given index in the grid. This expects the first
     * column to have the hierarchy data.
     *
     * @param rowIndex
     *            0-based row index to collapse
     * @see #collapseWithClick(int, int)
     */
    public void collapseWithClick(int rowIndex) {
        collapseWithClick(rowIndex, 0);
    }

    /**
     * Collapses the row at the given index in the grid with the given
     * hierarchical column index.
     *
     * @param rowIndex
     *            0-based row index to collapse
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     */
    public void collapseWithClick(int rowIndex, int hierarchyColumnIndex) {
        if (isRowCollapsed(rowIndex, hierarchyColumnIndex)) {
            throw new IllegalStateException("The element at row " + rowIndex
                    + " was collapsed already");
        }
        getExpandToggleElement(rowIndex, hierarchyColumnIndex).click();
    }

    /**
     * Returns whether the row at the given index is expanded or not.
     *
     * @param rowIndex
     *            0-based row index
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     * @return {@code true} if expanded, {@code false} if collapsed
     */
    public boolean isRowExpanded(int rowIndex, int hierarchyColumnIndex) {
        WebElement expandElement = getExpandToggleElement(rowIndex,
                hierarchyColumnIndex);
        return expandElement != null
                && !"false".equals(expandElement.getAttribute("expanded"));
    }

    /**
     * Returns whether the row at the given index is collapsed or not.
     *
     * @param rowIndex
     *            0-based row index
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     * @return {@code true} if collapsed, {@code false} if expanded
     */
    public boolean isRowCollapsed(int rowIndex, int hierarchyColumnIndex) {
        return !isRowExpanded(rowIndex, hierarchyColumnIndex);
    }

    /**
     * Check whether the given indices correspond to a cell that contains a
     * visible hierarchy toggle element.
     *
     * @param rowIndex
     *            0-based row index
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     * @return {@code true} if this cell has the expand toggle visible
     */
    public boolean hasExpandToggle(int rowIndex, int hierarchyColumnIndex) {
        try {
            WebElement expandElement = getExpandToggleElement(rowIndex,
                    hierarchyColumnIndex);
            return expandElement != null && expandElement.isDisplayed()
                    && "false".equals(expandElement.getAttribute("leaf"));
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Gets the 'vaadin-grid-tree-toggle' element for the given row.
     *
     * @param rowIndex
     *            0-based row index
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     * @return the {@code span} element that is clicked for expanding/collapsing
     *         a rows
     * @throws NoSuchElementException
     *             if there is no expand element for this row
     */
    public WebElement getExpandToggleElement(int rowIndex, int hierarchyColumnIndex) {
        return getCell(rowIndex, hierarchyColumnIndex)
                .$("vaadin-grid-tree-toggle").first();

    }

    /**
     * Returns a number of expanded rows in the grid element. Notice that
     * returned number does not mean that grid has yet finished rendering all
     * visible expanded rows.
     * 
     * @return the number of expanded rows
     */
    public long getNumberOfExpandedRows() {
        return (long) executeScript("return arguments[0].expandedItems.length;",
                this);
    }

    /**
     * Returns {@code true} if details are open or the given row index.
     * 
     * @param rowIndex
     *            the 0-based row index
     * @return {@code true} if details are shown in the target row
     */
    public boolean isDetailsOpen(int rowIndex) {
        try {
            return getRow(rowIndex).getDetails().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

}
