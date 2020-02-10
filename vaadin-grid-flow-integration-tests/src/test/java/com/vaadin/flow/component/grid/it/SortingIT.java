/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.openqa.selenium.WebElement;

@TestPath("sorting")
public class SortingIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void setInitialSortOrder_dataSorted() {
        findElement(By.id("single-sort")).click();
        Assert.assertEquals("A", grid.getCell(0, 0).getText());
        Assert.assertEquals("B", grid.getCell(1, 0).getText());
    }

    @Test
    public void setInitialSortOrder_sortIndicatorsUpdated() {
        findElement(By.id("single-sort")).click();
        assertAscendingSorter("Name");
    }

    @Test
    public void setInitialSortOrder_changeOrderFromServer_dataSorted() {
        findElement(By.id("single-sort")).click();
        findElement(By.id("sort-by-age")).click();
        Assert.assertEquals("B", grid.getCell(0, 0).getText());
        Assert.assertEquals("A", grid.getCell(1, 0).getText());
    }

    @Test
    public void setInitialSortOrder_changeOrderFromServer_sortIndicatorsUpdated() {
        findElement(By.id("single-sort")).click();
        findElement(By.id("sort-by-age")).click();
        assertAscendingSorter("Age");
    }
    @Test
    public void keepSortStatesAfterReAttach() {
        findElement(By.id("multi-sort")).click();
        WebElement gridElementBefore = findElement(By.id("sorting-grid"));
        WebElement btnAttach = findElement(By.id("btn-attach"));
        WebElement btnRemove = findElement(By.id("btn-rm"));
        Assert.assertEquals("30", grid.getCell(0, 1).getText());
        Assert.assertEquals("asc", gridElementBefore.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("direction"));
        findElement(By.id("sort-by-age")).click();
        Assert.assertEquals("20", grid.getCell(0, 1).getText());
        btnRemove.click();
        btnAttach.click();

        WebElement gridElementAfter = findElement(By.id("sorting-grid"));
        waitForElementPresent(By.id("sorting-grid"));
        gridElementAfter.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("direction");
        boolean isNameColumnDirectionEmpty =
                gridElementAfter.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("direction") == null;

        Assert.assertTrue("Direction attribute should not be in name column", isNameColumnDirectionEmpty);
        Assert.assertEquals("asc", gridElementAfter.findElements(By.tagName("vaadin-grid-sorter")).get(1).getAttribute("direction"));
        GridElement gridAfterAttach;
        gridAfterAttach = $(GridElement.class).first();
        Assert.assertEquals("20", gridAfterAttach.getCell(0, 1).getText());
        Assert.assertEquals("30", gridAfterAttach.getCell(1, 1).getText());

        // Continue to click Attach, without Deattach grid, it should keep the sort states
        btnAttach.click();
        gridElementAfter.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("direction");
        isNameColumnDirectionEmpty =
                gridElementAfter.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("direction") == null;

        Assert.assertTrue("Direction attribute should not be in name column", isNameColumnDirectionEmpty);
        Assert.assertEquals("asc", gridElementAfter.findElements(By.tagName("vaadin-grid-sorter")).get(1).getAttribute("direction"));
        gridAfterAttach = $(GridElement.class).first();
        Assert.assertEquals("20", gridAfterAttach.getCell(0, 1).getText());
        Assert.assertEquals("30", gridAfterAttach.getCell(1, 1).getText());

        // Multi-sort
        findElement(By.id("multi-sort")).click();
        final WebElement firstColumn = findElements(By.tagName("vaadin-grid-sorter")).get(0);
        firstColumn.click();
        Assert.assertEquals("30", gridAfterAttach.getCell(0, 1).getText());
        Assert.assertEquals("20", gridAfterAttach.getCell(1, 1).getText());
        Assert.assertEquals("A", gridAfterAttach.getCell(0, 0).getText());
        Assert.assertEquals("B", gridAfterAttach.getCell(1, 0).getText());
    }

    private void assertAscendingSorter(String expectedColumnHeader) {
        List<TestBenchElement> sorters = grid.$("vaadin-grid-sorter")
                .hasAttribute("direction").all();
        Assert.assertEquals("Only one column should be sorted. "
                + "Expected a single instance of <vaadin-grid-sorter> with 'direction' attribute.",
                1, sorters.size());
        TestBenchElement sorter = sorters.get(0);
        Assert.assertEquals("Expected ascending sort order.", "asc",
                sorter.getAttribute("direction"));
        Assert.assertEquals(expectedColumnHeader, sorter.getText());
    }

}
