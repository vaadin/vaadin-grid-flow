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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-multi-selection-column")
public class GridMultiSelectionColumnPageIT extends AbstractComponentIT {

    @Test
    public void selectAllCheckbox() {
        open();
        WebElement lazyGrid = findElement(By.id("lazy-grid"));
        Assert.assertEquals(
                "lazy grid selectAllCheckbox should be hidden by default",
                "true", lazyGrid.findElement(By.id("selectAllCheckbox"))
                        .getAttribute("hidden"));

        WebElement grid = findElement(By.id("in-memory-grid"));
        WebElement selectAllCheckbox = grid
                .findElement(By.id("selectAllCheckbox"));
        Assert.assertNull(
                "in-memory grid selectAllCheckbox should be visible by default",
                selectAllCheckbox.getAttribute("hidden"));

        selectAllCheckbox.click();
        WebElement message = findElement(By.id("selected-item-count"));
        Assert.assertEquals(
                "Selected item count: "
                        + GridMultiSelectionColumnPage.ITEM_COUNT,
                message.getText());
        Assert.assertEquals("true", selectAllCheckbox.getAttribute("checked"));

        WebElement selectCheckbox = grid
                .findElements(By.tagName("vaadin-checkbox")).get(5);
        Assert.assertEquals("true", selectCheckbox.getAttribute("checked"));
        selectCheckbox.click();
        Assert.assertNull(selectCheckbox.getAttribute("checked"));
        Assert.assertNull(selectAllCheckbox.getAttribute("checked"));
        Assert.assertEquals(
                "Selected item count: "
                        + (GridMultiSelectionColumnPage.ITEM_COUNT),
                message.getText());
    }

    @Test
    public void noSelectOnRowItemClick() {
        open();
        WebElement grid = findElement(By.id("in-memory-grid"));
        // click the first row's cell that corresponds to the text column
        grid.findElements(By.tagName("vaadin-grid-cell-content")).stream()
                .filter(element -> "0".equals(element.getText())).findFirst()
                .get().click();
        Assert.assertEquals("No selection event should be fired", "",
                findElement(By.id("selected-item-count")).getText());
    }
}
