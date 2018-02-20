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
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

/**
 * Integration tests for the GridPageSizePage view.
 */
@TestPath("grid-page-size")
public class GridPageSizePageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-grid"));
    }

    @Test
    public void gridWithPageSize10() {
        WebElement grid = findElement(By.tagName("vaadin-grid"));

        assertPageSize(grid, 10);
    }

    @Test
    public void gridWithPageSize10_changeTo80_revertBackTo10() {
        WebElement grid = findElement(By.tagName("vaadin-grid"));

        assertPageSize(grid, 10);

        WebElement input = findElement(By.id("size-input"));
        WebElement button = findElement(By.id("size-submit"));

        input.sendKeys("80");
        blur();
        button.click();

        assertPageSize(grid, 80);

        input.clear();
        input.sendKeys("10");
        blur();
        button.click();

        assertPageSize(grid, 10);
    }

    private void assertPageSize(WebElement grid, int pageSize) {
        Object pageSizeFromGrid = executeScript("return arguments[0].pageSize",
                grid);
        Assert.assertEquals(
                "The pageSize of the webcomponent should be " + pageSize,
                pageSize, Integer.parseInt(String.valueOf(pageSizeFromGrid)));
    }

}
