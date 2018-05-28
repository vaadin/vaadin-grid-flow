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
package com.vaadin.flow.component.treegrid.it;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-expand-collapse-recursively")
public class TreeGridExpandCollapseRecursivelyIT extends AbstractComponentIT {

    private static final int itemsPerLevel = 5;
    private static final int rowCount0 = itemsPerLevel;
    private static final int rowCount1 = rowCount0 + rowCount0 * itemsPerLevel;
    private static final int rowCount2 = rowCount1
            + (rowCount1 - rowCount0) * itemsPerLevel;
    private static final int rowCount3 = rowCount2
            + (rowCount2 - rowCount1) * itemsPerLevel;
    private static final int rowCount4 = rowCount3
            + (rowCount3 - rowCount2) * itemsPerLevel;

    private TreeGridElement grid;
    private WebElement depthSelector;
    private WebElement expandButton;
    private WebElement collapseButton;

    @Before
    public void before() {
        open();
        grid = $(TreeGridElement.class).first();
        depthSelector = findElement(By.tagName("vaadin-radio-group"));

        List<WebElement> buttons = findElements(By.tagName("button"));
        expandButton = buttons.get(0);
        collapseButton = buttons.get(1);
    }

    @Test
    public void expandVariousDepth() {
        Assert.assertEquals(rowCount0, grid.getRowCount());

        selectDepth(0);
        expandButton.click();
        
        waitUntil(input -> grid.getRowCount() == rowCount1, 2);
        Assert.assertEquals(itemsPerLevel, grid.getNumberOfExpandedRows());

        selectDepth(1);
        expandButton.click();

        waitUntil(input -> grid.getNumberOfExpandedRows() == rowCount1, 2);

        selectDepth(2);
        expandButton.click();

        waitUntil(input -> grid.getNumberOfExpandedRows() == rowCount2, 2);

        selectDepth(3);
        expandButton.click();

        waitUntil(input -> grid.getNumberOfExpandedRows() == rowCount3, 5);
    }

    @Test /* (timeout = 30000) */
    public void expandAndCollapseAllItems() {
        Assert.assertEquals(rowCount0, grid.getRowCount());

        selectDepth(3);
        expandButton.click();

        waitUntil(input -> grid.getNumberOfExpandedRows() == rowCount3, 15);

        collapseButton.click();

        waitUntil(input -> grid.getNumberOfExpandedRows() == 0, 15);
        Assert.assertEquals(rowCount0, grid.getRowCount());
    }

    @Test
    public void partialCollapse() {
        Assert.assertEquals(rowCount0, grid.getRowCount());

        selectDepth(3);
        expandButton.click();

        final AtomicInteger expandedRows = new AtomicInteger(rowCount3);
        waitUntil(input -> grid.getNumberOfExpandedRows() == expandedRows.get(),
                5);

        selectDepth(1);
        collapseButton.click();

        expandedRows.addAndGet(-rowCount1);
        waitUntil(
                input -> grid.getNumberOfExpandedRows() == expandedRows.get(),
                2);
        Assert.assertEquals(rowCount0, grid.getRowCount());

        selectDepth(0);
        expandButton.click();

        waitUntil(input -> grid.getRowCount() == rowCount1, 2);
        Assert.assertEquals(expandedRows.addAndGet(rowCount0),
                grid.getNumberOfExpandedRows());

        // Open just one subtree to see if it is still fully expanded
        grid.getExpandToggleElement(2, 0).click();

        expandedRows.addAndGet(1);
        waitUntil(input -> grid
                .getNumberOfExpandedRows() == expandedRows.get(), 1);
        Assert.assertEquals(
                (rowCount1 + itemsPerLevel
                        + (itemsPerLevel * itemsPerLevel * itemsPerLevel)),
                grid.getRowCount());
    }

    private void selectDepth(int depth) {
        WebElement radiobutton = depthSelector
                .findElements(By.tagName("vaadin-radio-button")).get(depth);
        executeScript("arguments[0].checked=true", radiobutton);
    }

}
