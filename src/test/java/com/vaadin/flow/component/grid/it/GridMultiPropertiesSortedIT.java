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

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

@TestPath("grid-multiproperty-memory-sort")
public class GridMultiPropertiesSortedIT extends AbstractComponentIT {


    @Test
    public void testDesc() {
        open();
        // Sorted descending by *Last Name*, *First Name*
        doSortTest("descSortBtn",
                "Catherine Zeta-Jones", "Richard Smith", "John Smith", "Basil Pupkin", "Ali Baba");
    }

    @Test
    public void testAsc() {
        open();
        // Sorted ascending by *Last Name*, *First Name*
        doSortTest("ascSortBtn",
                "Ali Baba", "Basil Pupkin", "John Smith", "Richard Smith", "Catherine Zeta-Jones");
    }

    private void doSortTest(String descSortBtn, String... samples) {
        clickElementWithJs(descSortBtn);
        GridElement grid = $(GridElement.class).first();
        GridColumnElement fullNameColumn = grid.getColumn("Full Name");
        for (int i = 0; i < samples.length; i++) {
            String text = grid.getCell(i, fullNameColumn).getText();
            Assert.assertEquals("Row " + i, samples[i], text);
        }
    }
}
