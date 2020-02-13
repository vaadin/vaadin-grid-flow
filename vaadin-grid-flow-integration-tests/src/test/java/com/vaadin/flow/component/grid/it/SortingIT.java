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
        Assert.assertEquals("A", grid.getCell(0, 0).getText());
        Assert.assertEquals("B", grid.getCell(1, 0).getText());
    }

    @Test
    public void setInitialSortOrder_sortIndicatorsUpdated() {
        assertAscendingSorter("Name");
    }

    @Test
    public void setInitialSortOrder_changeOrderFromServer_dataSorted() {
        findElement(By.id("sort-by-age")).click();
        Assert.assertEquals("B", grid.getCell(0, 0).getText());
        Assert.assertEquals("A", grid.getCell(1, 0).getText());
    }

    @Test
    public void setInitialSortOrder_changeOrderFromServer_sortIndicatorsUpdated() {
        findElement(By.id("sort-by-age")).click();
        assertAscendingSorter("Age");
    }
    @Test
    public void keepSortStatesAfterReAttach() {
        WebElement btnAttach = findElement(By.id("btn-attach"));
        WebElement btnRemove = findElement(By.id("btn-deattach"));

        // Attach grid
        btnAttach.click();

        WebElement secondGrid = findElement(By.id("second-grid"));
        GridElement secondGridElement = $(GridElement.class).get(1);
        // Assert sort from server side before reattach
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));

        // Deattach
        btnRemove.click();
        // Reattach
        btnAttach.click();

        secondGrid = findElement(By.id("second-grid"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));

        // Assert sort reattach without deattach and sort from server
        // Continue reattach
        secondGridElement = $(GridElement.class).get(1);
        String textFirstStringColumnBeforeReattch = secondGridElement.getCell(0, 1).getText();
        String textFirstIndexColumnBeforeReattch = secondGridElement.getCell(0, 2).getText();
        String textFirstRandomColumnBeforeReattch = secondGridElement.getCell(0, 3).getText();

        btnAttach.click();

        secondGridElement = $(GridElement.class).get(1);
        secondGrid = findElement(By.id("second-grid"));
        String textFirstStringColumnAfterReattch = secondGridElement.getCell(0, 1).getText();
        String textFirstIndexColumnAfterReattch = secondGridElement.getCell(0, 2).getText();
        String textFirstRandomColumnAfterReattch = secondGridElement.getCell(0, 3).getText();
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));
        Assert.assertEquals(textFirstStringColumnBeforeReattch, textFirstStringColumnAfterReattch);
        Assert.assertEquals(textFirstIndexColumnBeforeReattch, textFirstIndexColumnAfterReattch);
        Assert.assertEquals(textFirstRandomColumnBeforeReattch, textFirstRandomColumnAfterReattch);

        // Assert sort with rather than 1 column, handle from client
        // Sort with first and third columns
        secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(0).click();
        secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(2).click();

        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(0).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(2).getAttribute("direction"));
        secondGridElement = $(GridElement.class).get(1);
        textFirstStringColumnBeforeReattch = secondGridElement.getCell(0, 1).getText();
        textFirstIndexColumnBeforeReattch = secondGridElement.getCell(0, 2).getText();
        textFirstRandomColumnBeforeReattch = secondGridElement.getCell(0, 3).getText();

        // Deattach
        btnRemove.click();
        // Reattach
        btnAttach.click();

        secondGridElement = $(GridElement.class).get(1);
        textFirstStringColumnAfterReattch = secondGridElement.getCell(0, 1).getText();
        textFirstIndexColumnAfterReattch = secondGridElement.getCell(0, 2).getText();
        textFirstRandomColumnAfterReattch = secondGridElement.getCell(0, 3).getText();
        Assert.assertEquals(textFirstStringColumnBeforeReattch, textFirstStringColumnAfterReattch);
        Assert.assertEquals(textFirstIndexColumnBeforeReattch, textFirstIndexColumnAfterReattch);
        Assert.assertEquals(textFirstRandomColumnBeforeReattch, textFirstRandomColumnAfterReattch);

        // Assert sort reattach without deattach and sort from client, more than 1 sorters columns
        // Continue reattach
        secondGridElement = $(GridElement.class).get(1);
        textFirstStringColumnBeforeReattch = secondGridElement.getCell(0, 1).getText();
        textFirstIndexColumnBeforeReattch = secondGridElement.getCell(0, 2).getText();
        textFirstRandomColumnBeforeReattch = secondGridElement.getCell(0, 3).getText();

        btnAttach.click();

        secondGridElement = $(GridElement.class).get(1);
        secondGrid = findElement(By.id("second-grid"));
        textFirstStringColumnAfterReattch = secondGridElement.getCell(0, 1).getText();
        textFirstIndexColumnAfterReattch = secondGridElement.getCell(0, 2).getText();
        textFirstRandomColumnAfterReattch = secondGridElement.getCell(0, 3).getText();
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));
        Assert.assertEquals(textFirstStringColumnBeforeReattch, textFirstStringColumnAfterReattch);
        Assert.assertEquals(textFirstIndexColumnBeforeReattch, textFirstIndexColumnAfterReattch);
        Assert.assertEquals(textFirstRandomColumnBeforeReattch, textFirstRandomColumnAfterReattch);
    }

    @Test
    public void indicatorsSortStateNumbers() {
        WebElement btnAttach = findElement(By.id("btn-attach"));
        WebElement btnRemove = findElement(By.id("btn-deattach"));

        // Attach grid
        btnAttach.click();

        WebElement secondGrid = findElement(By.id("second-grid"));
        GridElement secondGridElement = $(GridElement.class).get(1);

        secondGrid = findElement(By.id("second-grid"));

        // Sort with first and third columns
        secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(0).click();
        secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(2).click();

        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(0).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(2).getAttribute("direction"));
        String sortStateNumberFirstColumn
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("_order");
        String sortStateNumberSecondColumn
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(1).getAttribute("_order");
        String sortStateNumberThirdColumn
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(2).getAttribute("_order");

        // Deattach
        btnRemove.click();
        // Reattach
        btnAttach.click();
        secondGrid = findElement(By.id("second-grid"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(0).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(2).getAttribute("direction"));
        String sortStateNumberFirstColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("_order");
        String sortStateNumberSecondColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(1).getAttribute("_order");
        String sortStateNumberThirdColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(2).getAttribute("_order");
        Assert.assertEquals(sortStateNumberFirstColumn, sortStateNumberFirstColumnAfterDeattach);
        Assert.assertEquals(sortStateNumberSecondColumn, sortStateNumberSecondColumnAfterDeattach);
        Assert.assertEquals(sortStateNumberThirdColumn, sortStateNumberThirdColumnAfterDeattach);

        // Continue to reattach
        btnAttach.click();
        secondGrid = findElement(By.id("second-grid"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(0).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(2).getAttribute("direction"));
        sortStateNumberFirstColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("_order");
        sortStateNumberSecondColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(1).getAttribute("_order");
        sortStateNumberThirdColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(2).getAttribute("_order");
        Assert.assertEquals(sortStateNumberFirstColumn, sortStateNumberFirstColumnAfterDeattach);
        Assert.assertEquals(sortStateNumberSecondColumn, sortStateNumberSecondColumnAfterDeattach);
        Assert.assertEquals(sortStateNumberThirdColumn, sortStateNumberThirdColumnAfterDeattach);

        // Continue to reattach again
        btnAttach.click();
        secondGrid = findElement(By.id("second-grid"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(0).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(1).getAttribute("direction"));
        Assert.assertEquals("asc", secondGrid.findElements(By.tagName("vaadin-grid-sorter"))
                .get(2).getAttribute("direction"));
        sortStateNumberFirstColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(0).getAttribute("_order");
        sortStateNumberSecondColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(1).getAttribute("_order");
        sortStateNumberThirdColumnAfterDeattach
                = secondGrid.findElements(By.tagName("vaadin-grid-sorter")).get(2).getAttribute("_order");
        Assert.assertEquals(sortStateNumberFirstColumn, sortStateNumberFirstColumnAfterDeattach);
        Assert.assertEquals(sortStateNumberSecondColumn, sortStateNumberSecondColumnAfterDeattach);
        Assert.assertEquals(sortStateNumberThirdColumn, sortStateNumberThirdColumnAfterDeattach);
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
