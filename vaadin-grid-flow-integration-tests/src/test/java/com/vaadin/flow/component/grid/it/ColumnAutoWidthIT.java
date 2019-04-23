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

@TestPath("column-auto-width")
public class ColumnAutoWidthIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id(ColumnAutoWidthPage.GRID_ID));
        grid = $(GridElement.class).id(ColumnAutoWidthPage.GRID_ID);
        waitUntil(driver -> (Boolean) executeScript(
                "return arguments[0]._getColumns()[0].width !== '100px'",
                grid));
    }

    @Test
    public void columnWidthsAreSetCorrectly() {
        List<String> colWidths = (List<String>) executeScript(
                "return arguments[0]._getColumns().map(col => col.width)",
                grid);

        Assert.assertEquals("55px", colWidths.get(0));
        Assert.assertEquals("432px", colWidths.get(2));
        Assert.assertEquals("256px", colWidths.get(3));
    }
}
