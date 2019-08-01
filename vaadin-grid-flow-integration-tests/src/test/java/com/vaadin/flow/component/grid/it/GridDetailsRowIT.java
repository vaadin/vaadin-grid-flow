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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Locale;

@TestPath("grid-details-row")
public class GridDetailsRowIT extends AbstractComponentIT {

    @Test
    public void gridNullValuesRenderedAsEmptyStrings() {
        open();
        GridElement grid = $(GridElement.class).first();
        // 1 details configured
        assertAmountOfOpenDetails(grid,1);

        // each detail contain a button
        List<WebElement> detailsElement = grid
                .findElements(By.tagName("flow-component-renderer"));

        Assert.assertEquals(2, detailsElement.size());

        assertElementHasButton(detailsElement.get(0));
        assertElementHasButton(detailsElement.get(1));

    }


    private void assertAmountOfOpenDetails(WebElement grid,
                                           int expectedAmount) {
        waitUntil(driver ->
                grid.findElements(By.className("row-details")).size()
                        == expectedAmount);
        Assert.assertEquals(expectedAmount,
                grid.findElements(By.className("row-details")).size());
    }

    private void assertElementHasButton(WebElement componentRenderer) {

        List<WebElement> children = componentRenderer
                .findElements(By.tagName("vaadin-button"));
        Assert.assertEquals(1, children.size());

    }

}
