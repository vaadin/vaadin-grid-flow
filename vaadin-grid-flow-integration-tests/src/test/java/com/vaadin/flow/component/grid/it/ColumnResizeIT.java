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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("column-resize-event")
public class ColumnResizeIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id(ColumnResizeEventPage.GRID_ID));
        grid = $(GridElement.class).id(ColumnResizeEventPage.GRID_ID);
    }

    @Test
    public void columnWidthsAreSetCorrectly() {
        TestBenchElement resizeHandle = grid.getHeaderCell(1)
                .findElement(By.tagName("div"));

        Actions actions = new Actions(driver);
        actions.clickAndHold(resizeHandle);
        actions.moveByOffset(-100, 0);
        actions.release(resizeHandle);
        actions.perform();

        waitForElementPresent(
                By.id(ColumnResizeEventPage.RESIZE_EVENT_LABEL_ID));
        WebElement eventLabel = findElement(
                By.id(ColumnResizeEventPage.RESIZE_EVENT_LABEL_ID));
        Assert.assertEquals("ID of resized column did not match expected one.",
                ColumnResizeEventPage.RESIZED_COLUMN_ID, eventLabel.getText());
    }
}
