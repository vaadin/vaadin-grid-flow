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
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

@TestPath("treegrid-details-row")
public class TreeGridDetailsRowIT extends AbstractComponentIT {

    @Test
    public void gridRootItemDetailsDisplayedWhenOpen() {
        open();
        TreeGridElement treegrid = $(TreeGridElement.class).first();
        //  detail configured
        assertAmountOfOpenDetails(treegrid,1);

        // each detail contain a button
        List<WebElement> detailsElement = treegrid
                .findElements(By.tagName("flow-component-renderer"));

        Assert.assertEquals(1, detailsElement.size());

        assertElementHasButton(detailsElement.get(0), "root WITH CHILDREN");
    }

    @Test
    public void gridChildItemDetailsDisplayedWhenClicked() {
        open();
        TreeGridElement treegrid = $(TreeGridElement.class).first();
        //  detail configured
        assertAmountOfOpenDetails(treegrid,1);

        clickElementWithJs(getRow(treegrid, 8).findElement(By.tagName("td")));

        List<WebElement> detailsElement = treegrid
                .findElements(By.tagName("flow-component-renderer"));

        Assert.assertEquals(2, detailsElement.size());

        // detail on row 0 is empty
        assertElementHasNoButton(detailsElement.get(0));
        // detail on row 1 contains a button
        assertElementHasButton(detailsElement.get(1),"Drei LEAF");
    }

    @Test
    public void gridChildItemDetailsDisplayedAfterCollapseWhenClicked() {
        open();
        TreeGridElement treegrid = $(TreeGridElement.class).first();
        //  detail configured
        assertAmountOfOpenDetails(treegrid,1);

        treegrid.collapseWithClick(0);
        treegrid.expandWithClick(0);

        clickElementWithJs(getRow(treegrid, 8).findElement(By.tagName("td")));

        List<WebElement> detailsElement = treegrid
                .findElements(By.tagName("flow-component-renderer"));

        Assert.assertEquals(2, detailsElement.size());

        // detail on row 0 is empty
        assertElementHasNoButton(detailsElement.get(0));
        // assertElementHasNoButton(detailsElement.get(0));
        // detail on row 1 contains a button
        assertElementHasButton(detailsElement.get(1),"Drei LEAF");
    }

    private WebElement getRow(WebElement grid, int row) {
        return getInShadowRoot(grid, By.id("items"))
                .findElements(By.cssSelector("tr")).get(row);
    }

    private void assertAmountOfOpenDetails(WebElement grid,
                                           int expectedAmount) {
        waitUntil(driver ->
                grid.findElements(By.className("row-details")).size()
                        == expectedAmount);
        Assert.assertEquals(expectedAmount,
                grid.findElements(By.className("row-details")).size());
    }

    private void assertElementHasButton(WebElement componentRenderer, String content) {

        List<WebElement> children = componentRenderer
                .findElements(By.tagName("vaadin-button"));
        Assert.assertEquals(1, children.size());
        Assert.assertEquals(content, children.get(0).getText());
    }

    private void assertElementHasNoButton(WebElement componentRenderer) {

        List<WebElement> children = componentRenderer
                .findElements(By.tagName("vaadin-button"));
        Assert.assertEquals(0, children.size());

    }
}
