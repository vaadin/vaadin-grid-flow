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

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("context-menu-grid")
public class ContextMenuGridIT extends AbstractComponentIT {

    private static final String OVERLAY_TAG = "vaadin-context-menu-overlay";
    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void contextClickOnRow_itemClickGetsTargetItem() {
        grid.getCell(56, 1).contextClick();
        $("vaadin-item").first().click();
        assertMessage("Person 56");
    }

    @Test
    public void contextClickOnRow_itemClickGetsGrid() {
        grid.getCell(56, 1).contextClick();
        $("vaadin-item").get(1).click();
        assertMessage("Grid id: grid-with-context-menu");
    }

    @Test
    public void contextClickOnHeader_targetItemReturnsNull() {
        grid.getHeaderCell(0).contextClick();
        $("vaadin-item").first().click();
        assertMessage("no target item");
    }

    @Test
    public void setOpenOnClick_clickOnRow_itemClickGetsTargetItem() {
        $("button").id("toggle-open-on-click").click();
        grid.getCell(14, 0).click();
        $("vaadin-item").first().click();
        assertMessage("Person 14");
    }

    @Test
    public void setOpenOnClick_contextClickOnRow_noContextMenuOpen() {
        $("button").id("toggle-open-on-click").click();
        grid.getCell(22, 0).contextClick();
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

    @Test
    public void gridInATemplateWithContextMenu_itemClickGetsTargetItem() {
        GridElement gridInATemplate = $("grid-in-a-template").first()
                .$(GridElement.class).first();
        gridInATemplate.getCell(18, 0).contextClick();
        $("vaadin-item").first().click();
        assertMessage("Item 18");
    }

    @Test
    public void removeContextMenu_menuIsNotShown() {
        GridElement grid = $(GridElement.class).id("grid-with-context-menu");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        grid.getCell(0, 0).contextClick();
        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 1);
        $(OVERLAY_TAG).get(0).$("vaadin-item").first().click();

        verifyClosed();

        $(TestBenchElement.class).id("remove-context-menu").click();

        grid.getCell(0, 0).contextClick();

        getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        Assert.assertFalse(isElementPresent(By.tagName(OVERLAY_TAG)));
    }

    private void assertMessage(String expected) {
        Assert.assertEquals(expected, $("label").id("message").getText());
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

}
