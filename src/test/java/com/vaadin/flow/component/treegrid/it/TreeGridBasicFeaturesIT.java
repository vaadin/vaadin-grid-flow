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

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@TestPath("treegrid-basic-features")
public class TreeGridBasicFeaturesIT extends AbstractComponentIT {
    private TreeGridElement grid;

    public void setDataProvider(String dataProviderString) {
        findElement(By.id(dataProviderString)).click();
    }

    @Parameters
    public static Collection<String> getDataProviders() {
        return Arrays.asList("LazyHierarchicalDataProvider",
                "TreeDataProvider");
    }

    @Before
    public void before() {
        open();
        grid = $(TreeGridElement.class).first();
    }

    private String id(String id) {
        return id.replace(" ", "");
    }

    @Test
    public void toggle_collapse_server_side() {
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        findElementByText("Expand 0 | 0").click();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // expanding already expanded item should have no effect
        findElementByText("Expand 0 | 0").click();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        findElementByText("Collapse 0 | 0").click();
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // collapsing the same item twice should have no effect
        findElementByText("Collapse 0 | 0").click();
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        findElementByText("Expand 1 | 1").click();
        // 1 | 1 not yet visible, shouldn't immediately expand anything
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        findElementByText("Expand 0 | 0").click();
        // 1 | 1 becomes visible and is also expanded
        Assert.assertEquals(9, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "2 | 0", "2 | 1",
                "2 | 2", "1 | 2" });

        // collapsing a leaf should have no effect
        findElementByText("Collapse 2 | 1").click();
        Assert.assertEquals(9, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "2 | 0", "2 | 1",
                "2 | 2", "1 | 2" });

        // collapsing 0 | 0 should collapse the expanded 1 | 1
        findElementByText("Collapse 0 | 0").click();
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // expand 0 | 0 recursively
        findElementByText("Expand 0 | 0 recursively").click();
        Assert.assertEquals(15, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "1 | 0", "2 | 0" });

        // collapse 0 | 0 recursively
        findElementByText("Collapse 0 | 0 recursively").click();
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // expanding 0 | 0 should result in 3 additional nodes after recursive
        // collapse
        findElementByText("Expand 0 | 0").click();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        Assert.assertFalse(
                getLogEntries(Level.SEVERE).stream().findAny().isPresent());
    }

    @Test
    public void pending_expands_cleared_when_data_provider_set() {
        findElementByText("Expand 1 | 1").click();
        findElement(By.id("LazyHierarchicalDataProvider")).click();

        grid.expandWithClick(0);
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });
    }

    @Test
    public void non_leaf_collapse_on_click() {
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });

        // Should expand "0 | 0"
        grid.expandWithClick(0);
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // Should collapse "0 | 0"
        grid.collapseWithClick(0);
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });
    }

    @Test
    public void keyboard_navigation() {
        grid.getCell(0, 0).click();

        // Should expand "0 | 0" without moving focus
        new Actions(getDriver()).sendKeys(Keys.RIGHT).perform();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });
        assertTrue(grid.getRow(0).hasClassName("v-treegrid-row-focused"));
        assertFalse(grid.getRow(1).hasClassName("v-treegrid-row-focused"));

        // Should navigate 2 times down to "1 | 1"
        new Actions(getDriver()).sendKeys(Keys.DOWN, Keys.DOWN).perform();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });
        assertFalse(grid.getRow(0).hasClassName("v-treegrid-row-focused"));
        assertFalse(grid.getRow(1).hasClassName("v-treegrid-row-focused"));
        assertTrue(grid.getRow(2).hasClassName("v-treegrid-row-focused"));

        // Should expand "1 | 1" without moving focus
        new Actions(getDriver()).sendKeys(Keys.RIGHT).perform();
        Assert.assertEquals(9, grid.getRowCount());
        assertCellTexts(2, 0,
                new String[] { "1 | 1", "2 | 0", "2 | 1", "2 | 2", "1 | 2" });
        assertTrue(grid.getRow(2).hasClassName("v-treegrid-row-focused"));

        // Should collapse "1 | 1"
        new Actions(getDriver()).sendKeys(Keys.LEFT).perform();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(2, 0, new String[] { "1 | 1", "1 | 2", "0 | 1" });
        assertTrue(grid.getRow(2).hasClassName("v-treegrid-row-focused"));

        // Should navigate to "0 | 0"
        new Actions(getDriver()).sendKeys(Keys.LEFT).perform();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(0, 0,
                new String[] { "0 | 0", "1 | 0", "1 | 1", "1 | 2", "0 | 1" });
        assertTrue(grid.getRow(0).hasClassName("v-treegrid-row-focused"));

        // Should collapse "0 | 0"
        new Actions(getDriver()).sendKeys(Keys.LEFT).perform();
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });
        assertTrue(grid.getRow(0).hasClassName("v-treegrid-row-focused"));

        // Nothing should happen
        new Actions(getDriver()).sendKeys(Keys.LEFT).perform();
        Assert.assertEquals(3, grid.getRowCount());
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "0 | 2" });
        assertTrue(grid.getRow(0).hasClassName("v-treegrid-row-focused"));

        checkLogsForErrors();
    }

    @Test
    public void keyboard_selection() {
        grid.getCell(0, 0).click();

        // Should expand "0 | 0" without moving focus
        new Actions(getDriver()).sendKeys(Keys.RIGHT).perform();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });

        // Should navigate 2 times down to "1 | 1"
        new Actions(getDriver()).sendKeys(Keys.DOWN, Keys.DOWN).perform();
        Assert.assertEquals(6, grid.getRowCount());
        assertCellTexts(1, 0, new String[] { "1 | 0", "1 | 1", "1 | 2" });
        assertFalse(grid.getRow(0).hasClassName("v-treegrid-row-focused"));
        assertFalse(grid.getRow(1).hasClassName("v-treegrid-row-focused"));
        assertTrue(grid.getRow(2).hasClassName("v-treegrid-row-focused"));

        // Should select "1 | 1" without moving focus
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        assertTrue(grid.getRow(2).hasClassName("v-treegrid-row-selected"));

        // Should move focus but not selection
        new Actions(getDriver()).sendKeys(Keys.UP).perform();
        assertTrue(grid.getRow(1).hasClassName("v-treegrid-row-focused"));
        assertFalse(grid.getRow(2).hasClassName("v-treegrid-row-focused"));
        assertFalse(grid.getRow(1).hasClassName("v-treegrid-row-selected"));
        assertTrue(grid.getRow(2).hasClassName("v-treegrid-row-selected"));

        // Should select "1 | 0" without moving focus
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        assertTrue(grid.getRow(1).hasClassName("v-treegrid-row-focused"));
        assertFalse(grid.getRow(2).hasClassName("v-treegrid-row-focused"));
        assertTrue(grid.getRow(1).hasClassName("v-treegrid-row-selected"));
        assertFalse(grid.getRow(2).hasClassName("v-treegrid-row-selected"));

        checkLogsForErrors();
    }

    @Test
    public void changing_hierarchy_column() {
        assertTrue(grid.hasExpandToggle(0, 0));
        assertFalse(grid.hasExpandToggle(0, 1));

        findElementByText("Set hierarchy column - depth").click();

        assertFalse(grid.hasExpandToggle(0, 0));
        assertTrue(grid.hasExpandToggle(0, 1));

        findElementByText("Set hierarchy column - id").click();

        assertTrue(grid.hasExpandToggle(0, 0));
        assertFalse(grid.hasExpandToggle(0, 1));
    }

    @Test
    public void expand_and_collapse_listeners() {
        findElementByText("Expand listener").click();
        findElementByText("Collapse listener").click();

        assertFalse(logContainsText(
                "Item(s) expanded (from client: true): 0 | 0"));
        assertFalse(logContainsText(
                "Item(s) collapsed (from client: true): 0 | 0"));

        grid.expandWithClick(0);

        assertTrue(logContainsText(
                "Item(s) expanded (from client: true): 0 | 0"));
        assertFalse(logContainsText(
                "Item(s) collapsed (from client: true): 0 | 0"));

        grid.collapseWithClick(0);

        assertTrue(logContainsText(
                "Item(s) expanded (from client: true): 0 | 0"));
        assertTrue(logContainsText(
                "Item(s) collapsed (from client: true): 0 | 0"));

        findElementByText("Expand 0 | 0").click();

        assertTrue(logContainsText(
                "Item(s) expanded (from client: false): 0 | 0"));
        assertFalse(logContainsText(
                "Item(s) collapsed (from client: false): 0 | 0"));

        findElementByText("Collapse 0 | 0").click();

        assertTrue(logContainsText(
                "Item(s) expanded (from client: false): 0 | 0"));
        assertTrue(logContainsText(
                "Item(s) collapsed (from client: false): 0 | 0"));
    }

    @Test
    public void expanded_nodes_stay_expanded_when_parent_expand_state_is_toggled() {
        grid.expandWithClick(0);
        grid.expandWithClick(1);
        grid.collapseWithClick(0);
        grid.expandWithClick(0);
        assertCellTexts(0, 0, new String[] { "0 | 0", "1 | 0", "2 | 0", "2 | 1",
                "2 | 2", "1 | 1", "1 | 2", "0 | 1", "0 | 2" });
        Assert.assertEquals(9, grid.getRowCount());

        grid.expandWithClick(7);
        grid.expandWithClick(8);
        grid.collapseWithClick(7);
        grid.collapseWithClick(0);
        grid.expandWithClick(1);
        assertCellTexts(0, 0, new String[] { "0 | 0", "0 | 1", "1 | 0", "2 | 0",
                "2 | 1", "2 | 2", "1 | 1", "1 | 2", "0 | 2" });
        Assert.assertEquals(9, grid.getRowCount());
    }

    private void assertCellTexts(int startRowIndex, int cellIndex,
            String[] cellTexts) {
        int index = startRowIndex;
        for (String cellText : cellTexts) {
            Assert.assertEquals(cellText,
                    grid.getCell(index, cellIndex).getText());
            index++;
        }
    }

    private boolean logContainsText(String txt) {
        String value = (String) executeScript("return arguments[0].value",
                findElement(By.id("log")));
        return value != null && value.contains(txt);
    }

    private WebElement findElementByText(String text) {
        return findElement(By.id(id(text)));
    }
}
