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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-scrolling")
public class TreeGridScrollingIT extends AbstractComponentIT {

    @Test
    public void testScrollingTree_expandCollapseFromBeginning_correctItemsShown() {
        // TODO refactor this test to verify each row against a model, e.g. a
        // InMemoryDataProvider, or the used lazy hierarchical data provider
        open();

        TreeGridElement grid = $(TreeGridElement.class).id("treegrid");

        Assert.assertEquals(grid.getRowCount(),
                TreeGridScrollingPage.DEFAULT_NODES);

        verifyRow(0, 0, 0);
        verifyRow(10, 0, 10);
        verifyRow(19, 0, 19);
        verifyRow(10, 0, 10);
        verifyRow(0, 0, 0);

        grid.expandWithClick(0);

        verifyRow(0, 0, 0);
        verifyRow(1, 1, 0);
        verifyRow(11, 1, 10);
        verifyRow(20, 1, 19);
        verifyRow(39, 0, 19);

        // verifying in reverse order causes scrolling up
        verifyRow(20, 1, 19);
        verifyRow(11, 1, 10);
        verifyRow(1, 1, 0);
        verifyRow(0, 0, 0);

        grid.expandWithClick(3);

        verifyRow(0, 0, 0);

        verifyRow(1, 1, 0);
        verifyRow(2, 1, 1);
        verifyRow(3, 1, 2);

        verifyRow(4, 2, 0);

        verifyRow(14, 2, 10);
        verifyRow(23, 2, 19);
        verifyRow(24, 1, 3);
        verifyRow(40, 1, 19);
        verifyRow(59, 0, 19);

        // scroll back up

        verifyRow(40, 1, 19);
        verifyRow(24, 1, 3);
        verifyRow(23, 2, 19);
        verifyRow(14, 2, 10);

        verifyRow(4, 2, 0);
        verifyRow(2, 1, 1);
        verifyRow(3, 1, 2);
        verifyRow(1, 1, 0);
        verifyRow(0, 0, 0);

        grid.expandWithClick(2);

        verifyRow(0, 0, 0);

        verifyRow(1, 1, 0);
        verifyRow(2, 1, 1);
        verifyRow(3, 2, 0);
        verifyRow(22, 2, 19);

        verifyRow(23, 1, 2);
        verifyRow(24, 2, 0);

        verifyRow(43, 2, 19);
        verifyRow(44, 1, 3);
        verifyRow(60, 1, 19);
        verifyRow(79, 0, 19);

        // scroll back up
        verifyRow(60, 1, 19);
        verifyRow(44, 1, 3);
        verifyRow(43, 2, 19);

        verifyRow(24, 2, 0);
        verifyRow(23, 1, 2);

        verifyRow(22, 2, 19);
        verifyRow(3, 2, 0);
        verifyRow(2, 1, 1);
        verifyRow(1, 1, 0);

        verifyRow(0, 0, 0);

        grid.collapseWithClick(2);

        verifyRow(0, 0, 0);

        verifyRow(1, 1, 0);
        verifyRow(2, 1, 1);
        verifyRow(3, 1, 2);

        verifyRow(4, 2, 0);

        verifyRow(14, 2, 10);
        verifyRow(23, 2, 19);
        verifyRow(24, 1, 3);
        verifyRow(40, 1, 19);
        verifyRow(59, 0, 19);

        // scroll back up

        verifyRow(40, 1, 19);
        verifyRow(24, 1, 3);
        verifyRow(23, 2, 19);
        verifyRow(14, 2, 10);

        verifyRow(4, 2, 0);
        verifyRow(2, 1, 1);
        verifyRow(3, 1, 2);
        verifyRow(1, 1, 0);
        verifyRow(0, 0, 0);

        grid.collapseWithClick(3);

        verifyRow(0, 0, 0);
        verifyRow(1, 1, 0);
        verifyRow(11, 1, 10);
        verifyRow(20, 1, 19);
        verifyRow(39, 0, 19);

        // scroll back up

        verifyRow(20, 1, 19);
        verifyRow(11, 1, 10);
        verifyRow(1, 1, 0);
        verifyRow(0, 0, 0);

        grid.collapseWithClick(0);

        verifyRow(0, 0, 0);
        verifyRow(10, 0, 10);
        verifyRow(19, 0, 19);
        verifyRow(10, 0, 10);
        verifyRow(0, 0, 0);
    }

    private void verifyRow(int rowActualIndex, int depth, int levelIndex) {
        TreeGridElement grid = $(TreeGridElement.class).first();

        Assert.assertEquals("Invalid row at index " + rowActualIndex,
                depth + " | " + levelIndex,
                grid.getCell(rowActualIndex, 0).getText());
    }
}
